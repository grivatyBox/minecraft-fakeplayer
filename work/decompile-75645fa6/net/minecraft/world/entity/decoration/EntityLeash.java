package net.minecraft.world.entity.decoration;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class EntityLeash extends BlockAttachedEntity {

    public static final double OFFSET_Y = 0.375D;

    public EntityLeash(EntityTypes<? extends EntityLeash> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityLeash(World world, BlockPosition blockposition) {
        super(EntityTypes.LEASH_KNOT, world, blockposition);
        this.setPos((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {}

    @Override
    protected void recalculateBoundingBox() {
        this.setPosRaw((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.375D, (double) this.pos.getZ() + 0.5D);
        double d0 = (double) this.getType().getWidth() / 2.0D;
        double d1 = (double) this.getType().getHeight();

        this.setBoundingBox(new AxisAlignedBB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0, this.getY() + d1, this.getZ() + d0));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        return d0 < 1024.0D;
    }

    @Override
    public void dropItem(WorldServer worldserver, @Nullable Entity entity) {
        this.playSound(SoundEffects.LEAD_UNTIED, 1.0F, 1.0F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {}

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {}

    @Override
    public EnumInteractionResult interact(EntityHuman entityhuman, EnumHand enumhand) {
        if (this.level().isClientSide()) {
            return EnumInteractionResult.SUCCESS;
        } else {
            if (entityhuman.getItemInHand(enumhand).is(Items.SHEARS)) {
                EnumInteractionResult enuminteractionresult = super.interact(entityhuman, enumhand);

                if (enuminteractionresult instanceof EnumInteractionResult.d) {
                    EnumInteractionResult.d enuminteractionresult_d = (EnumInteractionResult.d) enuminteractionresult;

                    if (enuminteractionresult_d.wasItemInteraction()) {
                        return enuminteractionresult;
                    }
                }
            }

            boolean flag = false;

            for (Leashable leashable : Leashable.leashableLeashedTo(entityhuman)) {
                if (leashable.canHaveALeashAttachedTo(this)) {
                    leashable.setLeashedTo(this, true);
                    flag = true;
                }
            }

            boolean flag1 = false;

            if (!flag && !entityhuman.isSecondaryUseActive()) {
                for (Leashable leashable1 : Leashable.leashableLeashedTo(this)) {
                    if (leashable1.canHaveALeashAttachedTo(entityhuman)) {
                        leashable1.setLeashedTo(entityhuman, true);
                        flag1 = true;
                    }
                }
            }

            if (!flag && !flag1) {
                return super.interact(entityhuman, enumhand);
            } else {
                this.gameEvent(GameEvent.BLOCK_ATTACH, entityhuman);
                this.playSound(SoundEffects.LEAD_TIED);
                return EnumInteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public void notifyLeasheeRemoved(Leashable leashable) {
        if (Leashable.leashableLeashedTo(this).isEmpty()) {
            this.discard();
        }

    }

    @Override
    public boolean survives() {
        return this.level().getBlockState(this.pos).is(TagsBlock.FENCES);
    }

    public static EntityLeash getOrCreateKnot(World world, BlockPosition blockposition) {
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();

        for (EntityLeash entityleash : world.getEntitiesOfClass(EntityLeash.class, new AxisAlignedBB((double) i - 1.0D, (double) j - 1.0D, (double) k - 1.0D, (double) i + 1.0D, (double) j + 1.0D, (double) k + 1.0D))) {
            if (entityleash.getPos().equals(blockposition)) {
                return entityleash;
            }
        }

        EntityLeash entityleash1 = new EntityLeash(world, blockposition);

        world.addFreshEntity(entityleash1);
        return entityleash1;
    }

    public void playPlacementSound() {
        this.playSound(SoundEffects.LEAD_TIED, 1.0F, 1.0F);
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket(EntityTrackerEntry entitytrackerentry) {
        return new PacketPlayOutSpawnEntity(this, 0, this.getPos());
    }

    @Override
    public Vec3D getRopeHoldPosition(float f) {
        return this.getPosition(f).add(0.0D, 0.2D, 0.0D);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.LEAD);
    }
}
