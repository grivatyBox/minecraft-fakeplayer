package net.minecraft.world.level.block.entity.trialspawner;

import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public enum TrialSpawnerState implements INamable {

    INACTIVE("inactive", 0, TrialSpawnerState.b.NONE, -1.0D, false), WAITING_FOR_PLAYERS("waiting_for_players", 4, TrialSpawnerState.b.SMALL_FLAMES, 200.0D, true), ACTIVE("active", 8, TrialSpawnerState.b.FLAMES_AND_SMOKE, 1000.0D, true), WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, TrialSpawnerState.b.SMALL_FLAMES, -1.0D, false), EJECTING_REWARD("ejecting_reward", 8, TrialSpawnerState.b.SMALL_FLAMES, -1.0D, false), COOLDOWN("cooldown", 0, TrialSpawnerState.b.SMOKE_INSIDE_AND_TOP_FACE, -1.0D, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = MathHelper.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final TrialSpawnerState.b particleEmission;
    private final boolean isCapableOfSpawning;

    private TrialSpawnerState(String s, int i, TrialSpawnerState.b trialspawnerstate_b, double d0, boolean flag) {
        this.name = s;
        this.lightLevel = i;
        this.particleEmission = trialspawnerstate_b;
        this.spinningMobSpeed = d0;
        this.isCapableOfSpawning = flag;
    }

    TrialSpawnerState tickAndGetNext(BlockPosition blockposition, TrialSpawner trialspawner, WorldServer worldserver) {
        TrialSpawnerData trialspawnerdata = trialspawner.getData();
        TrialSpawnerConfig trialspawnerconfig = trialspawner.getConfig();
        PlayerDetector playerdetector = trialspawner.getPlayerDetector();
        TrialSpawnerState trialspawnerstate;

        switch (this) {
            case INACTIVE:
                trialspawnerstate = trialspawnerdata.getOrCreateDisplayEntity(trialspawner, worldserver, TrialSpawnerState.WAITING_FOR_PLAYERS) == null ? this : TrialSpawnerState.WAITING_FOR_PLAYERS;
                break;
            case WAITING_FOR_PLAYERS:
                if (!trialspawnerdata.hasMobToSpawn()) {
                    trialspawnerstate = TrialSpawnerState.INACTIVE;
                } else {
                    trialspawnerdata.tryDetectPlayers(worldserver, blockposition, playerdetector, trialspawnerconfig.requiredPlayerRange());
                    trialspawnerstate = trialspawnerdata.detectedPlayers.isEmpty() ? this : TrialSpawnerState.ACTIVE;
                }
                break;
            case ACTIVE:
                if (!trialspawnerdata.hasMobToSpawn()) {
                    trialspawnerstate = TrialSpawnerState.INACTIVE;
                } else {
                    int i = trialspawnerdata.countAdditionalPlayers(blockposition);

                    trialspawnerdata.tryDetectPlayers(worldserver, blockposition, playerdetector, trialspawnerconfig.requiredPlayerRange());
                    if (trialspawnerdata.hasFinishedSpawningAllMobs(trialspawnerconfig, i)) {
                        if (trialspawnerdata.haveAllCurrentMobsDied()) {
                            trialspawnerdata.cooldownEndsAt = worldserver.getGameTime() + (long) trialspawnerconfig.targetCooldownLength();
                            trialspawnerdata.totalMobsSpawned = 0;
                            trialspawnerdata.nextMobSpawnsAt = 0L;
                            trialspawnerstate = TrialSpawnerState.WAITING_FOR_REWARD_EJECTION;
                            break;
                        }
                    } else if (trialspawnerdata.isReadyToSpawnNextMob(worldserver, trialspawnerconfig, i)) {
                        trialspawner.spawnMob(worldserver, blockposition).ifPresent((uuid) -> {
                            trialspawnerdata.currentMobs.add(uuid);
                            ++trialspawnerdata.totalMobsSpawned;
                            trialspawnerdata.nextMobSpawnsAt = worldserver.getGameTime() + (long) trialspawnerconfig.ticksBetweenSpawn();
                            trialspawnerdata.spawnPotentials.getRandom(worldserver.getRandom()).ifPresent((weightedentry_b) -> {
                                trialspawnerdata.nextSpawnData = Optional.of((MobSpawnerData) weightedentry_b.getData());
                                trialspawner.markUpdated();
                            });
                        });
                    }

                    trialspawnerstate = this;
                }
                break;
            case WAITING_FOR_REWARD_EJECTION:
                if (trialspawnerdata.isReadyToOpenShutter(worldserver, trialspawnerconfig, 40.0F)) {
                    worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.BLOCKS);
                    trialspawnerstate = TrialSpawnerState.EJECTING_REWARD;
                } else {
                    trialspawnerstate = this;
                }
                break;
            case EJECTING_REWARD:
                if (!trialspawnerdata.isReadyToEjectItems(worldserver, trialspawnerconfig, (float) TrialSpawnerState.TIME_BETWEEN_EACH_EJECTION)) {
                    trialspawnerstate = this;
                } else if (trialspawnerdata.detectedPlayers.isEmpty()) {
                    worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundCategory.BLOCKS);
                    trialspawnerdata.ejectingLootTable = Optional.empty();
                    trialspawnerstate = TrialSpawnerState.COOLDOWN;
                } else {
                    if (trialspawnerdata.ejectingLootTable.isEmpty()) {
                        trialspawnerdata.ejectingLootTable = trialspawnerconfig.lootTablesToEject().getRandomValue(worldserver.getRandom());
                    }

                    trialspawnerdata.ejectingLootTable.ifPresent((minecraftkey) -> {
                        trialspawner.ejectReward(worldserver, blockposition, minecraftkey);
                    });
                    trialspawnerdata.detectedPlayers.remove(trialspawnerdata.detectedPlayers.iterator().next());
                    trialspawnerstate = this;
                }
                break;
            case COOLDOWN:
                if (trialspawnerdata.isCooldownFinished(worldserver)) {
                    trialspawnerdata.cooldownEndsAt = 0L;
                    trialspawnerstate = TrialSpawnerState.WAITING_FOR_PLAYERS;
                } else {
                    trialspawnerstate = this;
                }
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return trialspawnerstate;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0D;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(World world, BlockPosition blockposition) {
        this.particleEmission.emit(world, world.getRandom(), blockposition);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private interface b {

        TrialSpawnerState.b NONE = (world, randomsource, blockposition) -> {
        };
        TrialSpawnerState.b SMALL_FLAMES = (world, randomsource, blockposition) -> {
            if (randomsource.nextInt(2) == 0) {
                Vec3D vec3d = blockposition.getCenter().offsetRandom(randomsource, 0.9F);

                addParticle(Particles.SMALL_FLAME, vec3d, world);
            }

        };
        TrialSpawnerState.b FLAMES_AND_SMOKE = (world, randomsource, blockposition) -> {
            Vec3D vec3d = blockposition.getCenter().offsetRandom(randomsource, 1.0F);

            addParticle(Particles.SMOKE, vec3d, world);
            addParticle(Particles.FLAME, vec3d, world);
        };
        TrialSpawnerState.b SMOKE_INSIDE_AND_TOP_FACE = (world, randomsource, blockposition) -> {
            Vec3D vec3d = blockposition.getCenter().offsetRandom(randomsource, 0.9F);

            if (randomsource.nextInt(3) == 0) {
                addParticle(Particles.SMOKE, vec3d, world);
            }

            if (world.getGameTime() % 20L == 0L) {
                Vec3D vec3d1 = blockposition.getCenter().add(0.0D, 0.5D, 0.0D);
                int i = world.getRandom().nextInt(4) + 20;

                for (int j = 0; j < i; ++j) {
                    addParticle(Particles.SMOKE, vec3d1, world);
                }
            }

        };

        private static void addParticle(ParticleType particletype, Vec3D vec3d, World world) {
            world.addParticle(particletype, vec3d.x(), vec3d.y(), vec3d.z(), 0.0D, 0.0D, 0.0D);
        }

        void emit(World world, RandomSource randomsource, BlockPosition blockposition);
    }

    private static class a {

        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private a() {}
    }

    private static class c {

        private static final double NONE = -1.0D;
        private static final double SLOW = 200.0D;
        private static final double FAST = 1000.0D;

        private c() {}
    }
}
