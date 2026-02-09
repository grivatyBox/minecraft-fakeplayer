package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;

public abstract class EntityPotion extends EntityProjectileThrowable {

    public static final double SPLASH_RANGE = 4.0D;
    protected static final double SPLASH_RANGE_SQ = 16.0D;
    public static final Predicate<EntityLiving> WATER_SENSITIVE_OR_ON_FIRE = (entityliving) -> {
        return entityliving.isSensitiveToWater() || entityliving.isOnFire();
    };

    public EntityPotion(EntityTypes<? extends EntityPotion> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityPotion(EntityTypes<? extends EntityPotion> entitytypes, World world, EntityLiving entityliving, ItemStack itemstack) {
        super(entitytypes, entityliving, world, itemstack);
    }

    public EntityPotion(EntityTypes<? extends EntityPotion> entitytypes, World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(entitytypes, d0, d1, d2, world, itemstack);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            EnumDirection enumdirection = movingobjectpositionblock.getDirection();
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            PotionContents potioncontents = (PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (potioncontents.is(Potions.WATER)) {
                this.dowseFire(blockposition1);
                this.dowseFire(blockposition1.relative(enumdirection.getOpposite()));

                for (EnumDirection enumdirection1 : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                    this.dowseFire(blockposition1.relative(enumdirection1));
                }
            }

        }
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            ItemStack itemstack = this.getItem();
            PotionContents potioncontents = (PotionContents) itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (potioncontents.is(Potions.WATER)) {
                this.onHitAsWater(worldserver);
            } else if (potioncontents.hasEffects()) {
                this.onHitAsPotion(worldserver, itemstack, movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY ? ((MovingObjectPositionEntity) movingobjectposition).getEntity() : null);
            }

            int i = potioncontents.potion().isPresent() && ((PotionRegistry) ((Holder) potioncontents.potion().get()).value()).hasInstantEffects() ? 2007 : 2002;

            worldserver.levelEvent(i, this.blockPosition(), potioncontents.getColor());
            this.discard();
        }
    }

    private void onHitAsWater(WorldServer worldserver) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);

        for (EntityLiving entityliving : this.level().getEntitiesOfClass(EntityLiving.class, axisalignedbb, EntityPotion.WATER_SENSITIVE_OR_ON_FIRE)) {
            double d0 = this.distanceToSqr((Entity) entityliving);

            if (d0 < 16.0D) {
                if (entityliving.isSensitiveToWater()) {
                    entityliving.hurtServer(worldserver, this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
                }

                if (entityliving.isOnFire() && entityliving.isAlive()) {
                    entityliving.extinguishFire();
                }
            }
        }

        for (Axolotl axolotl : this.level().getEntitiesOfClass(Axolotl.class, axisalignedbb)) {
            axolotl.rehydrate();
        }

    }

    protected abstract void onHitAsPotion(WorldServer worldserver, ItemStack itemstack, @Nullable Entity entity);

    private void dowseFire(BlockPosition blockposition) {
        IBlockData iblockdata = this.level().getBlockState(blockposition);

        if (iblockdata.is(TagsBlock.FIRE)) {
            this.level().destroyBlock(blockposition, false, this);
        } else if (AbstractCandleBlock.isLit(iblockdata)) {
            AbstractCandleBlock.extinguish((EntityHuman) null, iblockdata, this.level(), blockposition);
        } else if (BlockCampfire.isLitCampfire(iblockdata)) {
            this.level().levelEvent((Entity) null, 1009, blockposition, 0);
            BlockCampfire.dowse(this.getOwner(), this.level(), blockposition, iblockdata);
            this.level().setBlockAndUpdate(blockposition, (IBlockData) iblockdata.setValue(BlockCampfire.LIT, false));
        }

    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(EntityLiving entityliving, DamageSource damagesource) {
        double d0 = entityliving.position().x - this.position().x;
        double d1 = entityliving.position().z - this.position().z;

        return DoubleDoubleImmutablePair.of(d0, d1);
    }
}
