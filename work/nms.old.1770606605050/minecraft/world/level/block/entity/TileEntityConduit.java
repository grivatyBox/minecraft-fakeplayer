package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.monster.IMonster;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import net.minecraft.server.level.WorldServer;
// CraftBukkit end

public class TileEntityConduit extends TileEntity {

    private static final int BLOCK_REFRESH_RATE = 2;
    private static final int EFFECT_DURATION = 13;
    private static final float ROTATION_SPEED = -0.0375F;
    private static final int MIN_ACTIVE_SIZE = 16;
    private static final int MIN_KILL_SIZE = 42;
    private static final int KILL_RANGE = 8;
    private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
    public int tickCount;
    private float activeRotation;
    private boolean isActive;
    private boolean isHunting;
    public final List<BlockPosition> effectBlocks = Lists.newArrayList();
    @Nullable
    public EntityReference<EntityLiving> destroyTarget;
    private long nextAmbientSoundActivation;

    public TileEntityConduit(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.CONDUIT, blockposition, iblockdata);
    }

    @Override
    protected void loadAdditional(ValueInput valueinput) {
        super.loadAdditional(valueinput);
        this.destroyTarget = EntityReference.<EntityLiving>read(valueinput, "Target");
    }

    @Override
    protected void saveAdditional(ValueOutput valueoutput) {
        super.saveAdditional(valueoutput);
        EntityReference.store(this.destroyTarget, valueoutput, "Target");
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public static void clientTick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityConduit tileentityconduit) {
        ++tileentityconduit.tickCount;
        long i = world.getGameTime();
        List<BlockPosition> list = tileentityconduit.effectBlocks;

        if (i % 40L == 0L) {
            tileentityconduit.isActive = updateShape(world, blockposition, list);
            updateHunting(tileentityconduit, list);
        }

        EntityLiving entityliving = EntityReference.getLivingEntity(tileentityconduit.destroyTarget, world);

        animationTick(world, blockposition, list, entityliving, tileentityconduit.tickCount);
        if (tileentityconduit.isActive()) {
            ++tileentityconduit.activeRotation;
        }

    }

    public static void serverTick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityConduit tileentityconduit) {
        ++tileentityconduit.tickCount;
        long i = world.getGameTime();
        List<BlockPosition> list = tileentityconduit.effectBlocks;

        if (i % 40L == 0L) {
            boolean flag = updateShape(world, blockposition, list);

            if (flag != tileentityconduit.isActive) {
                SoundEffect soundeffect = flag ? SoundEffects.CONDUIT_ACTIVATE : SoundEffects.CONDUIT_DEACTIVATE;

                world.playSound((Entity) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            tileentityconduit.isActive = flag;
            updateHunting(tileentityconduit, list);
            if (flag) {
                applyEffects(world, blockposition, list);
                updateAndAttackTarget((WorldServer) world, blockposition, iblockdata, tileentityconduit, list.size() >= 42);
            }
        }

        if (tileentityconduit.isActive()) {
            if (i % 80L == 0L) {
                world.playSound((Entity) null, blockposition, SoundEffects.CONDUIT_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            if (i > tileentityconduit.nextAmbientSoundActivation) {
                tileentityconduit.nextAmbientSoundActivation = i + 60L + (long) world.getRandom().nextInt(40);
                world.playSound((Entity) null, blockposition, SoundEffects.CONDUIT_AMBIENT_SHORT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }

    }

    private static void updateHunting(TileEntityConduit tileentityconduit, List<BlockPosition> list) {
        tileentityconduit.setHunting(list.size() >= 42);
    }

    private static boolean updateShape(World world, BlockPosition blockposition, List<BlockPosition> list) {
        list.clear();

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    BlockPosition blockposition1 = blockposition.offset(i, j, k);

                    if (!world.isWaterAt(blockposition1)) {
                        return false;
                    }
                }
            }
        }

        for (int l = -2; l <= 2; ++l) {
            for (int i1 = -2; i1 <= 2; ++i1) {
                for (int j1 = -2; j1 <= 2; ++j1) {
                    int k1 = Math.abs(l);
                    int l1 = Math.abs(i1);
                    int i2 = Math.abs(j1);

                    if ((k1 > 1 || l1 > 1 || i2 > 1) && (l == 0 && (l1 == 2 || i2 == 2) || i1 == 0 && (k1 == 2 || i2 == 2) || j1 == 0 && (k1 == 2 || l1 == 2))) {
                        BlockPosition blockposition2 = blockposition.offset(l, i1, j1);
                        IBlockData iblockdata = world.getBlockState(blockposition2);

                        for (Block block : TileEntityConduit.VALID_BLOCKS) {
                            if (iblockdata.is(block)) {
                                list.add(blockposition2);
                            }
                        }
                    }
                }
            }
        }

        return list.size() >= 16;
    }

    private static void applyEffects(World world, BlockPosition blockposition, List<BlockPosition> list) {
        // CraftBukkit start
        applyEffects(world, blockposition, getRange(list));
    }

    public static int getRange(List<BlockPosition> list) {
        // CraftBukkit end
        int i = list.size();
        int j = i / 7 * 16;
        // CraftBukkit start
        return j;
    }

    private static void applyEffects(World world, BlockPosition blockposition, int j) { // j = effect range in blocks
        // CraftBukkit end
        int k = blockposition.getX();
        int l = blockposition.getY();
        int i1 = blockposition.getZ();
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB((double) k, (double) l, (double) i1, (double) (k + 1), (double) (l + 1), (double) (i1 + 1))).inflate((double) j).expandTowards(0.0D, (double) world.getHeight(), 0.0D);
        List<EntityHuman> list1 = world.<EntityHuman>getEntitiesOfClass(EntityHuman.class, axisalignedbb);

        if (!list1.isEmpty()) {
            for (EntityHuman entityhuman : list1) {
                if (blockposition.closerThan(entityhuman.blockPosition(), (double) j) && entityhuman.isInWaterOrRain()) {
                    entityhuman.addEffect(new MobEffect(MobEffects.CONDUIT_POWER, 260, 0, true, true), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONDUIT); // CraftBukkit
                }
            }

        }
    }

    private static void updateAndAttackTarget(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, TileEntityConduit tileentityconduit, boolean flag) {
        // CraftBukkit start - add "damageTarget" boolean
        updateAndAttackTarget(worldserver, blockposition, iblockdata, tileentityconduit, flag, true);
    }

    public static void updateAndAttackTarget(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, TileEntityConduit tileentityconduit, boolean flag, boolean damageTarget) {
        // CraftBukkit end
        EntityReference<EntityLiving> entityreference = updateDestroyTarget(tileentityconduit.destroyTarget, worldserver, blockposition, flag);
        EntityLiving entityliving = EntityReference.getLivingEntity(entityreference, worldserver);

        // CraftBukkit start
        if (damageTarget && entityliving != null) {
            if (entityliving.hurtServer(worldserver, worldserver.damageSources().magic().directBlock(worldserver, blockposition), 4.0F)) {
                worldserver.playSound((Entity) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), SoundEffects.CONDUIT_ATTACK_TARGET, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            // CraftBukkit end
        }

        if (!Objects.equals(entityreference, tileentityconduit.destroyTarget)) {
            tileentityconduit.destroyTarget = entityreference;
            worldserver.sendBlockUpdated(blockposition, iblockdata, iblockdata, 2);
        }

    }

    @Nullable
    private static EntityReference<EntityLiving> updateDestroyTarget(@Nullable EntityReference<EntityLiving> entityreference, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        if (!flag) {
            return null;
        } else if (entityreference == null) {
            return selectNewTarget(worldserver, blockposition);
        } else {
            EntityLiving entityliving = EntityReference.getLivingEntity(entityreference, worldserver);

            return entityliving != null && entityliving.isAlive() && blockposition.closerThan(entityliving.blockPosition(), 8.0D) ? entityreference : null;
        }
    }

    @Nullable
    private static EntityReference<EntityLiving> selectNewTarget(WorldServer worldserver, BlockPosition blockposition) {
        List<EntityLiving> list = worldserver.<EntityLiving>getEntitiesOfClass(EntityLiving.class, getDestroyRangeAABB(blockposition), (entityliving) -> {
            return entityliving instanceof IMonster && entityliving.isInWaterOrRain();
        });

        return list.isEmpty() ? null : EntityReference.of((EntityLiving) SystemUtils.getRandom(list, worldserver.random));
    }

    public static AxisAlignedBB getDestroyRangeAABB(BlockPosition blockposition) {
        return (new AxisAlignedBB(blockposition)).inflate(8.0D);
    }

    private static void animationTick(World world, BlockPosition blockposition, List<BlockPosition> list, @Nullable Entity entity, int i) {
        RandomSource randomsource = world.random;
        double d0 = (double) (MathHelper.sin((float) (i + 35) * 0.1F) / 2.0F + 0.5F);

        d0 = (d0 * d0 + d0) * (double) 0.3F;
        Vec3D vec3d = new Vec3D((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 1.5D + d0, (double) blockposition.getZ() + 0.5D);

        for (BlockPosition blockposition1 : list) {
            if (randomsource.nextInt(50) == 0) {
                BlockPosition blockposition2 = blockposition1.subtract(blockposition);
                float f = -0.5F + randomsource.nextFloat() + (float) blockposition2.getX();
                float f1 = -2.0F + randomsource.nextFloat() + (float) blockposition2.getY();
                float f2 = -0.5F + randomsource.nextFloat() + (float) blockposition2.getZ();

                world.addParticle(Particles.NAUTILUS, vec3d.x, vec3d.y, vec3d.z, (double) f, (double) f1, (double) f2);
            }
        }

        if (entity != null) {
            Vec3D vec3d1 = new Vec3D(entity.getX(), entity.getEyeY(), entity.getZ());
            float f3 = (-0.5F + randomsource.nextFloat()) * (3.0F + entity.getBbWidth());
            float f4 = -1.0F + randomsource.nextFloat() * entity.getBbHeight();
            float f5 = (-0.5F + randomsource.nextFloat()) * (3.0F + entity.getBbWidth());
            Vec3D vec3d2 = new Vec3D((double) f3, (double) f4, (double) f5);

            world.addParticle(Particles.NAUTILUS, vec3d1.x, vec3d1.y, vec3d1.z, vec3d2.x, vec3d2.y, vec3d2.z);
        }

    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isHunting() {
        return this.isHunting;
    }

    private void setHunting(boolean flag) {
        this.isHunting = flag;
    }

    public float getActiveRotation(float f) {
        return (this.activeRotation + f) * -0.0375F;
    }
}
