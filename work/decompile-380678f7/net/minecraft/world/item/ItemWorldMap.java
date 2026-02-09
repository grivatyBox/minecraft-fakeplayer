package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MaterialMapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;

public class ItemWorldMap extends Item {

    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 128;

    public ItemWorldMap(Item.Info item_info) {
        super(item_info);
    }

    public static ItemStack create(WorldServer worldserver, int i, int j, byte b0, boolean flag, boolean flag1) {
        ItemStack itemstack = new ItemStack(Items.FILLED_MAP);
        MapId mapid = createNewSavedData(worldserver, i, j, b0, flag, flag1, worldserver.dimension());

        itemstack.set(DataComponents.MAP_ID, mapid);
        return itemstack;
    }

    @Nullable
    public static WorldMap getSavedData(@Nullable MapId mapid, World world) {
        return mapid == null ? null : world.getMapData(mapid);
    }

    @Nullable
    public static WorldMap getSavedData(ItemStack itemstack, World world) {
        MapId mapid = (MapId) itemstack.get(DataComponents.MAP_ID);

        return getSavedData(mapid, world);
    }

    public static MapId createNewSavedData(WorldServer worldserver, int i, int j, int k, boolean flag, boolean flag1, ResourceKey<World> resourcekey) {
        WorldMap worldmap = WorldMap.createFresh((double) i, (double) j, (byte) k, flag, flag1, resourcekey);
        MapId mapid = worldserver.getFreeMapId();

        worldserver.setMapData(mapid, worldmap);
        return mapid;
    }

    public void update(World world, Entity entity, WorldMap worldmap) {
        if (world.dimension() == worldmap.dimension && entity instanceof EntityHuman) {
            int i = 1 << worldmap.scale;
            int j = worldmap.centerX;
            int k = worldmap.centerZ;
            int l = MathHelper.floor(entity.getX() - (double) j) / i + 64;
            int i1 = MathHelper.floor(entity.getZ() - (double) k) / i + 64;
            int j1 = 128 / i;

            if (world.dimensionType().hasCeiling()) {
                j1 /= 2;
            }

            WorldMap.WorldMapHumanTracker worldmap_worldmaphumantracker = worldmap.getHoldingPlayer((EntityHuman) entity);

            ++worldmap_worldmaphumantracker.step;
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition1 = new BlockPosition.MutableBlockPosition();
            boolean flag = false;

            for (int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
                if ((k1 & 15) == (worldmap_worldmaphumantracker.step & 15) || flag) {
                    flag = false;
                    double d0 = 0.0D;

                    for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                        if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
                            int i2 = MathHelper.square(k1 - l) + MathHelper.square(l1 - i1);
                            boolean flag1 = i2 > (j1 - 2) * (j1 - 2);
                            int j2 = (j / i + k1 - 64) * i;
                            int k2 = (k / i + l1 - 64) * i;
                            Multiset<MaterialMapColor> multiset = LinkedHashMultiset.create();
                            Chunk chunk = world.getChunk(SectionPosition.blockToSectionCoord(j2), SectionPosition.blockToSectionCoord(k2));

                            if (!chunk.isEmpty()) {
                                int l2 = 0;
                                double d1 = 0.0D;

                                if (world.dimensionType().hasCeiling()) {
                                    int i3 = j2 + k2 * 231871;

                                    i3 = i3 * i3 * 31287121 + i3 * 11;
                                    if ((i3 >> 20 & 1) == 0) {
                                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(world, BlockPosition.ZERO), 10);
                                    } else {
                                        multiset.add(Blocks.STONE.defaultBlockState().getMapColor(world, BlockPosition.ZERO), 100);
                                    }

                                    d1 = 100.0D;
                                } else {
                                    for (int j3 = 0; j3 < i; ++j3) {
                                        for (int k3 = 0; k3 < i; ++k3) {
                                            blockposition_mutableblockposition.set(j2 + j3, 0, k2 + k3);
                                            int l3 = chunk.getHeight(HeightMap.Type.WORLD_SURFACE, blockposition_mutableblockposition.getX(), blockposition_mutableblockposition.getZ()) + 1;
                                            IBlockData iblockdata;

                                            if (l3 <= world.getMinY()) {
                                                iblockdata = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    --l3;
                                                    blockposition_mutableblockposition.setY(l3);
                                                    iblockdata = chunk.getBlockState(blockposition_mutableblockposition);
                                                } while (iblockdata.getMapColor(world, blockposition_mutableblockposition) == MaterialMapColor.NONE && l3 > world.getMinY());

                                                if (l3 > world.getMinY() && !iblockdata.getFluidState().isEmpty()) {
                                                    int i4 = l3 - 1;

                                                    blockposition_mutableblockposition1.set(blockposition_mutableblockposition);

                                                    IBlockData iblockdata1;

                                                    do {
                                                        blockposition_mutableblockposition1.setY(i4--);
                                                        iblockdata1 = chunk.getBlockState(blockposition_mutableblockposition1);
                                                        ++l2;
                                                    } while (i4 > world.getMinY() && !iblockdata1.getFluidState().isEmpty());

                                                    iblockdata = this.getCorrectStateForFluidBlock(world, iblockdata, blockposition_mutableblockposition);
                                                }
                                            }

                                            worldmap.checkBanners(world, blockposition_mutableblockposition.getX(), blockposition_mutableblockposition.getZ());
                                            d1 += (double) l3 / (double) (i * i);
                                            multiset.add(iblockdata.getMapColor(world, blockposition_mutableblockposition));
                                        }
                                    }
                                }

                                l2 /= i * i;
                                MaterialMapColor materialmapcolor = (MaterialMapColor) Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialMapColor.NONE);
                                MaterialMapColor.a materialmapcolor_a;

                                if (materialmapcolor == MaterialMapColor.WATER) {
                                    double d2 = (double) l2 * 0.1D + (double) (k1 + l1 & 1) * 0.2D;

                                    if (d2 < 0.5D) {
                                        materialmapcolor_a = MaterialMapColor.a.HIGH;
                                    } else if (d2 > 0.9D) {
                                        materialmapcolor_a = MaterialMapColor.a.LOW;
                                    } else {
                                        materialmapcolor_a = MaterialMapColor.a.NORMAL;
                                    }
                                } else {
                                    double d3 = (d1 - d0) * 4.0D / (double) (i + 4) + ((double) (k1 + l1 & 1) - 0.5D) * 0.4D;

                                    if (d3 > 0.6D) {
                                        materialmapcolor_a = MaterialMapColor.a.HIGH;
                                    } else if (d3 < -0.6D) {
                                        materialmapcolor_a = MaterialMapColor.a.LOW;
                                    } else {
                                        materialmapcolor_a = MaterialMapColor.a.NORMAL;
                                    }
                                }

                                d0 = d1;
                                if (l1 >= 0 && i2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
                                    flag |= worldmap.updateColor(k1, l1, materialmapcolor.getPackedId(materialmapcolor_a));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private IBlockData getCorrectStateForFluidBlock(World world, IBlockData iblockdata, BlockPosition blockposition) {
        Fluid fluid = iblockdata.getFluidState();

        return !fluid.isEmpty() && !iblockdata.isFaceSturdy(world, blockposition, EnumDirection.UP) ? fluid.createLegacyBlock() : iblockdata;
    }

    private static boolean isBiomeWatery(boolean[] aboolean, int i, int j) {
        return aboolean[j * 128 + i];
    }

    public static void renderBiomePreviewMap(WorldServer worldserver, ItemStack itemstack) {
        WorldMap worldmap = getSavedData(itemstack, worldserver);

        if (worldmap != null) {
            if (worldserver.dimension() == worldmap.dimension) {
                int i = 1 << worldmap.scale;
                int j = worldmap.centerX;
                int k = worldmap.centerZ;
                boolean[] aboolean = new boolean[16384];
                int l = j / i - 64;
                int i1 = k / i - 64;
                BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

                for (int j1 = 0; j1 < 128; ++j1) {
                    for (int k1 = 0; k1 < 128; ++k1) {
                        Holder<BiomeBase> holder = worldserver.getBiome(blockposition_mutableblockposition.set((l + k1) * i, 0, (i1 + j1) * i));

                        aboolean[j1 * 128 + k1] = holder.is(BiomeTags.WATER_ON_MAP_OUTLINES);
                    }
                }

                for (int l1 = 1; l1 < 127; ++l1) {
                    for (int i2 = 1; i2 < 127; ++i2) {
                        int j2 = 0;

                        for (int k2 = -1; k2 < 2; ++k2) {
                            for (int l2 = -1; l2 < 2; ++l2) {
                                if ((k2 != 0 || l2 != 0) && isBiomeWatery(aboolean, l1 + k2, i2 + l2)) {
                                    ++j2;
                                }
                            }
                        }

                        MaterialMapColor.a materialmapcolor_a = MaterialMapColor.a.LOWEST;
                        MaterialMapColor materialmapcolor = MaterialMapColor.NONE;

                        if (isBiomeWatery(aboolean, l1, i2)) {
                            materialmapcolor = MaterialMapColor.COLOR_ORANGE;
                            if (j2 > 7 && i2 % 2 == 0) {
                                switch ((l1 + (int) (MathHelper.sin((float) i2 + 0.0F) * 7.0F)) / 8 % 5) {
                                    case 0:
                                    case 4:
                                        materialmapcolor_a = MaterialMapColor.a.LOW;
                                        break;
                                    case 1:
                                    case 3:
                                        materialmapcolor_a = MaterialMapColor.a.NORMAL;
                                        break;
                                    case 2:
                                        materialmapcolor_a = MaterialMapColor.a.HIGH;
                                }
                            } else if (j2 > 7) {
                                materialmapcolor = MaterialMapColor.NONE;
                            } else if (j2 > 5) {
                                materialmapcolor_a = MaterialMapColor.a.NORMAL;
                            } else if (j2 > 3) {
                                materialmapcolor_a = MaterialMapColor.a.LOW;
                            } else if (j2 > 1) {
                                materialmapcolor_a = MaterialMapColor.a.LOW;
                            }
                        } else if (j2 > 0) {
                            materialmapcolor = MaterialMapColor.COLOR_BROWN;
                            if (j2 > 3) {
                                materialmapcolor_a = MaterialMapColor.a.NORMAL;
                            } else {
                                materialmapcolor_a = MaterialMapColor.a.LOWEST;
                            }
                        }

                        if (materialmapcolor != MaterialMapColor.NONE) {
                            worldmap.setColor(l1, i2, materialmapcolor.getPackedId(materialmapcolor_a));
                        }
                    }
                }

            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemstack, WorldServer worldserver, Entity entity, @Nullable EnumItemSlot enumitemslot) {
        WorldMap worldmap = getSavedData(itemstack, worldserver);

        if (worldmap != null) {
            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;

                worldmap.tickCarriedBy(entityhuman, itemstack);
            }

            if (!worldmap.locked && enumitemslot != null && enumitemslot.getType() == EnumItemSlot.Function.HAND) {
                this.update(worldserver, entity, worldmap);
            }

        }
    }

    @Override
    public void onCraftedPostProcess(ItemStack itemstack, World world) {
        MapPostProcessing mappostprocessing = (MapPostProcessing) itemstack.remove(DataComponents.MAP_POST_PROCESSING);

        if (mappostprocessing != null) {
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                switch (mappostprocessing) {
                    case LOCK:
                        lockMap(itemstack, worldserver);
                        break;
                    case SCALE:
                        scaleMap(itemstack, worldserver);
                }
            }

        }
    }

    private static void scaleMap(ItemStack itemstack, WorldServer worldserver) {
        WorldMap worldmap = getSavedData(itemstack, worldserver);

        if (worldmap != null) {
            MapId mapid = worldserver.getFreeMapId();

            worldserver.setMapData(mapid, worldmap.scaled());
            itemstack.set(DataComponents.MAP_ID, mapid);
        }

    }

    private static void lockMap(ItemStack itemstack, WorldServer worldserver) {
        WorldMap worldmap = getSavedData(itemstack, worldserver);

        if (worldmap != null) {
            MapId mapid = worldserver.getFreeMapId();
            WorldMap worldmap1 = worldmap.locked();

            worldserver.setMapData(mapid, worldmap1);
            itemstack.set(DataComponents.MAP_ID, mapid);
        }

    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        IBlockData iblockdata = itemactioncontext.getLevel().getBlockState(itemactioncontext.getClickedPos());

        if (iblockdata.is(TagsBlock.BANNERS)) {
            if (!itemactioncontext.getLevel().isClientSide) {
                WorldMap worldmap = getSavedData(itemactioncontext.getItemInHand(), itemactioncontext.getLevel());

                if (worldmap != null && !worldmap.toggleBanner(itemactioncontext.getLevel(), itemactioncontext.getClickedPos())) {
                    return EnumInteractionResult.FAIL;
                }
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            return super.useOn(itemactioncontext);
        }
    }
}
