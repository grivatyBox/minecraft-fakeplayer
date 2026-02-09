package net.minecraft.world.entity.npc;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityPositionTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.entity.animal.EntityCat;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawner;
import net.minecraft.world.phys.AxisAlignedBB;

public class MobSpawnerCat implements MobSpawner {

    private static final int TICK_DELAY = 1200;
    private int nextTick;

    public MobSpawnerCat() {}

    @Override
    public void tick(WorldServer worldserver, boolean flag, boolean flag1) {
        if (flag1 && worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            --this.nextTick;
            if (this.nextTick <= 0) {
                this.nextTick = 1200;
                EntityHuman entityhuman = worldserver.getRandomPlayer();

                if (entityhuman != null) {
                    RandomSource randomsource = worldserver.random;
                    int i = (8 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
                    int j = (8 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
                    BlockPosition blockposition = entityhuman.blockPosition().offset(i, 0, j);
                    int k = 10;

                    if (worldserver.hasChunksAt(blockposition.getX() - 10, blockposition.getZ() - 10, blockposition.getX() + 10, blockposition.getZ() + 10)) {
                        if (EntityPositionTypes.isSpawnPositionOk(EntityTypes.CAT, worldserver, blockposition)) {
                            if (worldserver.isCloseToVillage(blockposition, 2)) {
                                this.spawnInVillage(worldserver, blockposition);
                            } else if (worldserver.structureManager().getStructureWithPieceAt(blockposition, StructureTags.CATS_SPAWN_IN).isValid()) {
                                this.spawnInHut(worldserver, blockposition);
                            }
                        }

                    }
                }
            }
        }
    }

    private void spawnInVillage(WorldServer worldserver, BlockPosition blockposition) {
        int i = 48;

        if (worldserver.getPoiManager().getCountInRange((holder) -> {
            return holder.is(PoiTypes.HOME);
        }, blockposition, 48, VillagePlace.Occupancy.IS_OCCUPIED) > 4L) {
            List<EntityCat> list = worldserver.<EntityCat>getEntitiesOfClass(EntityCat.class, (new AxisAlignedBB(blockposition)).inflate(48.0D, 8.0D, 48.0D));

            if (list.size() < 5) {
                this.spawnCat(blockposition, worldserver, false);
            }
        }

    }

    private void spawnInHut(WorldServer worldserver, BlockPosition blockposition) {
        int i = 16;
        List<EntityCat> list = worldserver.<EntityCat>getEntitiesOfClass(EntityCat.class, (new AxisAlignedBB(blockposition)).inflate(16.0D, 8.0D, 16.0D));

        if (list.isEmpty()) {
            this.spawnCat(blockposition, worldserver, true);
        }

    }

    private void spawnCat(BlockPosition blockposition, WorldServer worldserver, boolean flag) {
        EntityCat entitycat = EntityTypes.CAT.create(worldserver, EntitySpawnReason.NATURAL);

        if (entitycat != null) {
            entitycat.finalizeSpawn(worldserver, worldserver.getCurrentDifficultyAt(blockposition), EntitySpawnReason.NATURAL, (GroupDataEntity) null);
            if (flag) {
                entitycat.setPersistenceRequired();
            }

            entitycat.snapTo(blockposition, 0.0F, 0.0F);
            worldserver.addFreshEntityWithPassengers(entitycat);
        }
    }
}
