package net.minecraft.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.TargetColorParticleOption;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.creaking.CreakingTransient;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class CreakingHeartBlockEntity extends TileEntity {

    private static final int PLAYER_DETECTION_RANGE = 32;
    public static final int CREAKING_ROAMING_RADIUS = 32;
    private static final int DISTANCE_CREAKING_TOO_FAR = 34;
    private static final int SPAWN_RANGE_XZ = 16;
    private static final int SPAWN_RANGE_Y = 8;
    private static final int ATTEMPTS_PER_SPAWN = 5;
    private static final int UPDATE_TICKS = 20;
    private static final int HURT_CALL_TOTAL_TICKS = 100;
    private static final int NUMBER_OF_HURT_CALLS = 10;
    private static final int HURT_CALL_INTERVAL = 10;
    private static final int HURT_CALL_PARTICLE_TICKS = 50;
    @Nullable
    private CreakingTransient creaking;
    private int ticker;
    private int emitter;
    @Nullable
    private Vec3D emitterTarget;
    private int outputSignal;

    public CreakingHeartBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.CREAKING_HEART, blockposition, iblockdata);
    }

    public static void serverTick(World world, BlockPosition blockposition, IBlockData iblockdata, CreakingHeartBlockEntity creakingheartblockentity) {
        int i = creakingheartblockentity.computeAnalogOutputSignal();

        if (creakingheartblockentity.outputSignal != i) {
            creakingheartblockentity.outputSignal = i;
            world.updateNeighbourForOutputSignal(blockposition, Blocks.CREAKING_HEART);
        }

        WorldServer worldserver;

        if (creakingheartblockentity.emitter > 0) {
            if (creakingheartblockentity.emitter > 50) {
                creakingheartblockentity.emitParticles((WorldServer) world, 1, true);
                creakingheartblockentity.emitParticles((WorldServer) world, 1, false);
            }

            if (creakingheartblockentity.emitter % 10 == 0 && world instanceof WorldServer) {
                worldserver = (WorldServer) world;
                if (creakingheartblockentity.emitterTarget != null) {
                    if (creakingheartblockentity.creaking != null) {
                        creakingheartblockentity.emitterTarget = creakingheartblockentity.creaking.getBoundingBox().getCenter();
                    }

                    Vec3D vec3d = Vec3D.atCenterOf(blockposition);
                    float f = 0.2F + 0.8F * (float) (100 - creakingheartblockentity.emitter) / 100.0F;
                    Vec3D vec3d1 = vec3d.subtract(creakingheartblockentity.emitterTarget).scale((double) f).add(creakingheartblockentity.emitterTarget);
                    BlockPosition blockposition1 = BlockPosition.containing(vec3d1);
                    float f1 = (float) creakingheartblockentity.emitter / 2.0F / 100.0F + 0.5F;

                    worldserver.playSound((EntityHuman) null, blockposition1, SoundEffects.CREAKING_HEART_HURT, SoundCategory.BLOCKS, f1, 1.0F);
                }
            }

            --creakingheartblockentity.emitter;
        }

        if (creakingheartblockentity.ticker-- < 0) {
            creakingheartblockentity.ticker = 20;
            if (creakingheartblockentity.creaking != null) {
                if (CreakingHeartBlock.canSummonCreaking(world) && creakingheartblockentity.distanceToCreaking() <= 34.0D) {
                    if (creakingheartblockentity.creaking.isRemoved()) {
                        creakingheartblockentity.creaking = null;
                    }

                    if (!CreakingHeartBlock.hasRequiredLogs(iblockdata, world, blockposition) && creakingheartblockentity.creaking == null) {
                        world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.DISABLED), 3);
                    }

                } else {
                    creakingheartblockentity.removeProtector((DamageSource) null);
                }
            } else if (!CreakingHeartBlock.hasRequiredLogs(iblockdata, world, blockposition)) {
                world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.DISABLED), 3);
            } else {
                if (!CreakingHeartBlock.canSummonCreaking(world)) {
                    if (iblockdata.getValue(CreakingHeartBlock.CREAKING) == CreakingHeartBlock.a.ACTIVE) {
                        world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.DORMANT), 3);
                        return;
                    }
                } else if (iblockdata.getValue(CreakingHeartBlock.CREAKING) == CreakingHeartBlock.a.DORMANT) {
                    world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.ACTIVE), 3);
                    return;
                }

                if (iblockdata.getValue(CreakingHeartBlock.CREAKING) == CreakingHeartBlock.a.ACTIVE) {
                    if (world.getDifficulty() != EnumDifficulty.PEACEFUL) {
                        if (world instanceof WorldServer) {
                            worldserver = (WorldServer) world;
                            if (!worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                                return;
                            }
                        }

                        EntityHuman entityhuman = world.getNearestPlayer((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 32.0D, false);

                        if (entityhuman != null) {
                            creakingheartblockentity.creaking = spawnProtector((WorldServer) world, creakingheartblockentity);
                            if (creakingheartblockentity.creaking != null) {
                                creakingheartblockentity.creaking.makeSound(SoundEffects.CREAKING_SPAWN);
                                world.playSound((EntityHuman) null, creakingheartblockentity.getBlockPos(), SoundEffects.CREAKING_HEART_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            }
                        }

                    }
                }
            }
        }
    }

    private double distanceToCreaking() {
        return this.creaking == null ? 0.0D : Math.sqrt(this.creaking.distanceToSqr(Vec3D.atBottomCenterOf(this.getBlockPos())));
    }

    @Nullable
    private static CreakingTransient spawnProtector(WorldServer worldserver, CreakingHeartBlockEntity creakingheartblockentity) {
        BlockPosition blockposition = creakingheartblockentity.getBlockPos();
        Optional<CreakingTransient> optional = SpawnUtil.trySpawnMob(EntityTypes.CREAKING_TRANSIENT, EntitySpawnReason.SPAWNER, worldserver, blockposition, 5, 16, 8, SpawnUtil.a.ON_TOP_OF_COLLIDER_NO_LEAVES);

        if (optional.isEmpty()) {
            return null;
        } else {
            CreakingTransient creakingtransient = (CreakingTransient) optional.get();

            worldserver.gameEvent((Entity) creakingtransient, (Holder) GameEvent.ENTITY_PLACE, creakingtransient.position());
            worldserver.broadcastEntityEvent(creakingtransient, (byte) 60);
            creakingtransient.bindToCreakingHeart(blockposition);
            return creakingtransient;
        }
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public void creakingHurt() {
        if (this.creaking != null) {
            World world = this.level;

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                this.emitParticles(worldserver, 20, false);
                this.emitter = 100;
                this.emitterTarget = this.creaking.getBoundingBox().getCenter();
            }
        }
    }

    private void emitParticles(WorldServer worldserver, int i, boolean flag) {
        if (this.creaking != null) {
            int j = flag ? 16545810 : 6250335;
            RandomSource randomsource = worldserver.random;

            for (double d0 = 0.0D; d0 < (double) i; ++d0) {
                Vec3D vec3d = this.creaking.getBoundingBox().getMinPosition().add(randomsource.nextDouble() * this.creaking.getBoundingBox().getXsize(), randomsource.nextDouble() * this.creaking.getBoundingBox().getYsize(), randomsource.nextDouble() * this.creaking.getBoundingBox().getZsize());
                Vec3D vec3d1 = Vec3D.atLowerCornerOf(this.getBlockPos()).add(randomsource.nextDouble(), randomsource.nextDouble(), randomsource.nextDouble());

                if (flag) {
                    Vec3D vec3d2 = vec3d;

                    vec3d = vec3d1;
                    vec3d1 = vec3d2;
                }

                TargetColorParticleOption targetcolorparticleoption = new TargetColorParticleOption(vec3d1, j);

                worldserver.sendParticles(targetcolorparticleoption, vec3d.x, vec3d.y, vec3d.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

        }
    }

    public void removeProtector(@Nullable DamageSource damagesource) {
        if (this.creaking != null) {
            this.creaking.tearDown(damagesource);
            this.creaking = null;
        }

    }

    public boolean isProtector(Creaking creaking) {
        return this.creaking == creaking;
    }

    public int getAnalogOutputSignal() {
        return this.outputSignal;
    }

    public int computeAnalogOutputSignal() {
        if (this.creaking == null) {
            return 0;
        } else {
            double d0 = this.distanceToCreaking();
            double d1 = Math.clamp(d0, 0.0D, 32.0D) / 32.0D;

            return 15 - (int) Math.floor(d1 * 15.0D);
        }
    }
}
