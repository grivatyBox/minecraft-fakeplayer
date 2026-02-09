package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.BlockChest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.structure.DefinedStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.WorldGenFeatureStructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockIgnore;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.LootTables;

public class WoodlandMansionPieces {

    public WoodlandMansionPieces() {}

    public static void generateMansion(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation, List<WoodlandMansionPieces.i> list, RandomSource randomsource) {
        WoodlandMansionPieces.c woodlandmansionpieces_c = new WoodlandMansionPieces.c(randomsource);
        WoodlandMansionPieces.d woodlandmansionpieces_d = new WoodlandMansionPieces.d(structuretemplatemanager, randomsource);

        woodlandmansionpieces_d.createMansion(blockposition, enumblockrotation, list, woodlandmansionpieces_c);
    }

    public static class i extends DefinedStructurePiece {

        public i(StructureTemplateManager structuretemplatemanager, String s, BlockPosition blockposition, EnumBlockRotation enumblockrotation) {
            this(structuretemplatemanager, s, blockposition, enumblockrotation, EnumBlockMirror.NONE);
        }

        public i(StructureTemplateManager structuretemplatemanager, String s, BlockPosition blockposition, EnumBlockRotation enumblockrotation, EnumBlockMirror enumblockmirror) {
            super(WorldGenFeatureStructurePieceType.WOODLAND_MANSION_PIECE, 0, structuretemplatemanager, makeLocation(s), s, makeSettings(enumblockmirror, enumblockrotation), blockposition);
        }

        public i(StructureTemplateManager structuretemplatemanager, NBTTagCompound nbttagcompound) {
            super(WorldGenFeatureStructurePieceType.WOODLAND_MANSION_PIECE, nbttagcompound, structuretemplatemanager, (minecraftkey) -> {
                return makeSettings((EnumBlockMirror) nbttagcompound.read("Mi", EnumBlockMirror.LEGACY_CODEC).orElseThrow(), (EnumBlockRotation) nbttagcompound.read("Rot", EnumBlockRotation.LEGACY_CODEC).orElseThrow());
            });
        }

        @Override
        protected MinecraftKey makeTemplateLocation() {
            return makeLocation(this.templateName);
        }

        private static MinecraftKey makeLocation(String s) {
            return MinecraftKey.withDefaultNamespace("woodland_mansion/" + s);
        }

        private static DefinedStructureInfo makeSettings(EnumBlockMirror enumblockmirror, EnumBlockRotation enumblockrotation) {
            return (new DefinedStructureInfo()).setIgnoreEntities(true).setRotation(enumblockrotation).setMirror(enumblockmirror).addProcessor(DefinedStructureProcessorBlockIgnore.STRUCTURE_BLOCK);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(structurepieceserializationcontext, nbttagcompound);
            nbttagcompound.store("Rot", EnumBlockRotation.LEGACY_CODEC, this.placeSettings.getRotation());
            nbttagcompound.store("Mi", EnumBlockMirror.LEGACY_CODEC, this.placeSettings.getMirror());
        }

        @Override
        protected void handleDataMarker(String s, BlockPosition blockposition, WorldAccess worldaccess, RandomSource randomsource, StructureBoundingBox structureboundingbox) {
            if (s.startsWith("Chest")) {
                EnumBlockRotation enumblockrotation = this.placeSettings.getRotation();
                IBlockData iblockdata = Blocks.CHEST.defaultBlockState();

                if ("ChestWest".equals(s)) {
                    iblockdata = (IBlockData) iblockdata.setValue(BlockChest.FACING, enumblockrotation.rotate(EnumDirection.WEST));
                } else if ("ChestEast".equals(s)) {
                    iblockdata = (IBlockData) iblockdata.setValue(BlockChest.FACING, enumblockrotation.rotate(EnumDirection.EAST));
                } else if ("ChestSouth".equals(s)) {
                    iblockdata = (IBlockData) iblockdata.setValue(BlockChest.FACING, enumblockrotation.rotate(EnumDirection.SOUTH));
                } else if ("ChestNorth".equals(s)) {
                    iblockdata = (IBlockData) iblockdata.setValue(BlockChest.FACING, enumblockrotation.rotate(EnumDirection.NORTH));
                }

                this.createChest(worldaccess, structureboundingbox, randomsource, blockposition, LootTables.WOODLAND_MANSION, iblockdata);
            } else {
                List<EntityInsentient> list = new ArrayList();

                switch (s) {
                    case "Mage":
                        list.add(EntityTypes.EVOKER.create(worldaccess.getLevel(), EntitySpawnReason.STRUCTURE));
                        break;
                    case "Warrior":
                        list.add(EntityTypes.VINDICATOR.create(worldaccess.getLevel(), EntitySpawnReason.STRUCTURE));
                        break;
                    case "Group of Allays":
                        int i = worldaccess.getRandom().nextInt(3) + 1;

                        for (int j = 0; j < i; ++j) {
                            list.add(EntityTypes.ALLAY.create(worldaccess.getLevel(), EntitySpawnReason.STRUCTURE));
                        }
                        break;
                    default:
                        return;
                }

                for (EntityInsentient entityinsentient : list) {
                    if (entityinsentient != null) {
                        entityinsentient.setPersistenceRequired();
                        entityinsentient.snapTo(blockposition, 0.0F, 0.0F);
                        entityinsentient.finalizeSpawn(worldaccess, worldaccess.getCurrentDifficultyAt(entityinsentient.blockPosition()), EntitySpawnReason.STRUCTURE, (GroupDataEntity) null);
                        worldaccess.addFreshEntityWithPassengers(entityinsentient);
                        worldaccess.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }

        }
    }

    private static class e {

        public EnumBlockRotation rotation;
        public BlockPosition position;
        public String wallType;

        e() {}
    }

    private static class d {

        private final StructureTemplateManager structureTemplateManager;
        private final RandomSource random;
        private int startX;
        private int startY;

        public d(StructureTemplateManager structuretemplatemanager, RandomSource randomsource) {
            this.structureTemplateManager = structuretemplatemanager;
            this.random = randomsource;
        }

        public void createMansion(BlockPosition blockposition, EnumBlockRotation enumblockrotation, List<WoodlandMansionPieces.i> list, WoodlandMansionPieces.c woodlandmansionpieces_c) {
            WoodlandMansionPieces.e woodlandmansionpieces_e = new WoodlandMansionPieces.e();

            woodlandmansionpieces_e.position = blockposition;
            woodlandmansionpieces_e.rotation = enumblockrotation;
            woodlandmansionpieces_e.wallType = "wall_flat";
            WoodlandMansionPieces.e woodlandmansionpieces_e1 = new WoodlandMansionPieces.e();

            this.entrance(list, woodlandmansionpieces_e);
            woodlandmansionpieces_e1.position = woodlandmansionpieces_e.position.above(8);
            woodlandmansionpieces_e1.rotation = woodlandmansionpieces_e.rotation;
            woodlandmansionpieces_e1.wallType = "wall_window";
            if (!list.isEmpty()) {
                ;
            }

            WoodlandMansionPieces.g woodlandmansionpieces_g = woodlandmansionpieces_c.baseGrid;
            WoodlandMansionPieces.g woodlandmansionpieces_g1 = woodlandmansionpieces_c.thirdFloorGrid;

            this.startX = woodlandmansionpieces_c.entranceX + 1;
            this.startY = woodlandmansionpieces_c.entranceY + 1;
            int i = woodlandmansionpieces_c.entranceX + 1;
            int j = woodlandmansionpieces_c.entranceY;

            this.traverseOuterWalls(list, woodlandmansionpieces_e, woodlandmansionpieces_g, EnumDirection.SOUTH, this.startX, this.startY, i, j);
            this.traverseOuterWalls(list, woodlandmansionpieces_e1, woodlandmansionpieces_g, EnumDirection.SOUTH, this.startX, this.startY, i, j);
            WoodlandMansionPieces.e woodlandmansionpieces_e2 = new WoodlandMansionPieces.e();

            woodlandmansionpieces_e2.position = woodlandmansionpieces_e.position.above(19);
            woodlandmansionpieces_e2.rotation = woodlandmansionpieces_e.rotation;
            woodlandmansionpieces_e2.wallType = "wall_window";
            boolean flag = false;

            for (int k = 0; k < woodlandmansionpieces_g1.height && !flag; ++k) {
                for (int l = woodlandmansionpieces_g1.width - 1; l >= 0 && !flag; --l) {
                    if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g1, l, k)) {
                        woodlandmansionpieces_e2.position = woodlandmansionpieces_e2.position.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8 + (k - this.startY) * 8);
                        woodlandmansionpieces_e2.position = woodlandmansionpieces_e2.position.relative(enumblockrotation.rotate(EnumDirection.EAST), (l - this.startX) * 8);
                        this.traverseWallPiece(list, woodlandmansionpieces_e2);
                        this.traverseOuterWalls(list, woodlandmansionpieces_e2, woodlandmansionpieces_g1, EnumDirection.SOUTH, l, k, l, k);
                        flag = true;
                    }
                }
            }

            this.createRoof(list, blockposition.above(16), enumblockrotation, woodlandmansionpieces_g, woodlandmansionpieces_g1);
            this.createRoof(list, blockposition.above(27), enumblockrotation, woodlandmansionpieces_g1, (WoodlandMansionPieces.g) null);
            if (!list.isEmpty()) {
                ;
            }

            WoodlandMansionPieces.b[] awoodlandmansionpieces_b = new WoodlandMansionPieces.b[]{new WoodlandMansionPieces.a(), new WoodlandMansionPieces.f(), new WoodlandMansionPieces.h()};

            for (int i1 = 0; i1 < 3; ++i1) {
                BlockPosition blockposition1 = blockposition.above(8 * i1 + (i1 == 2 ? 3 : 0));
                WoodlandMansionPieces.g woodlandmansionpieces_g2 = woodlandmansionpieces_c.floorRooms[i1];
                WoodlandMansionPieces.g woodlandmansionpieces_g3 = i1 == 2 ? woodlandmansionpieces_g1 : woodlandmansionpieces_g;
                String s = i1 == 0 ? "carpet_south_1" : "carpet_south_2";
                String s1 = i1 == 0 ? "carpet_west_1" : "carpet_west_2";

                for (int j1 = 0; j1 < woodlandmansionpieces_g3.height; ++j1) {
                    for (int k1 = 0; k1 < woodlandmansionpieces_g3.width; ++k1) {
                        if (woodlandmansionpieces_g3.get(k1, j1) == 1) {
                            BlockPosition blockposition2 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8 + (j1 - this.startY) * 8);

                            blockposition2 = blockposition2.relative(enumblockrotation.rotate(EnumDirection.EAST), (k1 - this.startX) * 8);
                            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "corridor_floor", blockposition2, enumblockrotation));
                            if (woodlandmansionpieces_g3.get(k1, j1 - 1) == 1 || (woodlandmansionpieces_g2.get(k1, j1 - 1) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "carpet_north", blockposition2.relative(enumblockrotation.rotate(EnumDirection.EAST), 1).above(), enumblockrotation));
                            }

                            if (woodlandmansionpieces_g3.get(k1 + 1, j1) == 1 || (woodlandmansionpieces_g2.get(k1 + 1, j1) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "carpet_east", blockposition2.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 1).relative(enumblockrotation.rotate(EnumDirection.EAST), 5).above(), enumblockrotation));
                            }

                            if (woodlandmansionpieces_g3.get(k1, j1 + 1) == 1 || (woodlandmansionpieces_g2.get(k1, j1 + 1) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, s, blockposition2.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 5).relative(enumblockrotation.rotate(EnumDirection.WEST), 1), enumblockrotation));
                            }

                            if (woodlandmansionpieces_g3.get(k1 - 1, j1) == 1 || (woodlandmansionpieces_g2.get(k1 - 1, j1) & 8388608) == 8388608) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, s1, blockposition2.relative(enumblockrotation.rotate(EnumDirection.WEST), 1).relative(enumblockrotation.rotate(EnumDirection.NORTH), 1), enumblockrotation));
                            }
                        }
                    }
                }

                String s2 = i1 == 0 ? "indoors_wall_1" : "indoors_wall_2";
                String s3 = i1 == 0 ? "indoors_door_1" : "indoors_door_2";
                List<EnumDirection> list1 = Lists.newArrayList();

                for (int l1 = 0; l1 < woodlandmansionpieces_g3.height; ++l1) {
                    for (int i2 = 0; i2 < woodlandmansionpieces_g3.width; ++i2) {
                        boolean flag1 = i1 == 2 && woodlandmansionpieces_g3.get(i2, l1) == 3;

                        if (woodlandmansionpieces_g3.get(i2, l1) == 2 || flag1) {
                            int j2 = woodlandmansionpieces_g2.get(i2, l1);
                            int k2 = j2 & 983040;
                            int l2 = j2 & '\uffff';

                            flag1 = flag1 && (j2 & 8388608) == 8388608;
                            list1.clear();
                            if ((j2 & 2097152) == 2097152) {
                                for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                                    if (woodlandmansionpieces_g3.get(i2 + enumdirection.getStepX(), l1 + enumdirection.getStepZ()) == 1) {
                                        list1.add(enumdirection);
                                    }
                                }
                            }

                            EnumDirection enumdirection1 = null;

                            if (!list1.isEmpty()) {
                                enumdirection1 = (EnumDirection) list1.get(this.random.nextInt(list1.size()));
                            } else if ((j2 & 1048576) == 1048576) {
                                enumdirection1 = EnumDirection.UP;
                            }

                            BlockPosition blockposition3 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8 + (l1 - this.startY) * 8);

                            blockposition3 = blockposition3.relative(enumblockrotation.rotate(EnumDirection.EAST), -1 + (i2 - this.startX) * 8);
                            if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g3, i2 - 1, l1) && !woodlandmansionpieces_c.isRoomId(woodlandmansionpieces_g3, i2 - 1, l1, i1, l2)) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, enumdirection1 == EnumDirection.WEST ? s3 : s2, blockposition3, enumblockrotation));
                            }

                            if (woodlandmansionpieces_g3.get(i2 + 1, l1) == 1 && !flag1) {
                                BlockPosition blockposition4 = blockposition3.relative(enumblockrotation.rotate(EnumDirection.EAST), 8);

                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, enumdirection1 == EnumDirection.EAST ? s3 : s2, blockposition4, enumblockrotation));
                            }

                            if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g3, i2, l1 + 1) && !woodlandmansionpieces_c.isRoomId(woodlandmansionpieces_g3, i2, l1 + 1, i1, l2)) {
                                BlockPosition blockposition5 = blockposition3.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 7);

                                blockposition5 = blockposition5.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, enumdirection1 == EnumDirection.SOUTH ? s3 : s2, blockposition5, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                            }

                            if (woodlandmansionpieces_g3.get(i2, l1 - 1) == 1 && !flag1) {
                                BlockPosition blockposition6 = blockposition3.relative(enumblockrotation.rotate(EnumDirection.NORTH), 1);

                                blockposition6 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, enumdirection1 == EnumDirection.NORTH ? s3 : s2, blockposition6, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                            }

                            if (k2 == 65536) {
                                this.addRoom1x1(list, blockposition3, enumblockrotation, enumdirection1, awoodlandmansionpieces_b[i1]);
                            } else if (k2 == 131072 && enumdirection1 != null) {
                                EnumDirection enumdirection2 = woodlandmansionpieces_c.get1x2RoomDirection(woodlandmansionpieces_g3, i2, l1, i1, l2);
                                boolean flag2 = (j2 & 4194304) == 4194304;

                                this.addRoom1x2(list, blockposition3, enumblockrotation, enumdirection2, enumdirection1, awoodlandmansionpieces_b[i1], flag2);
                            } else if (k2 == 262144 && enumdirection1 != null && enumdirection1 != EnumDirection.UP) {
                                EnumDirection enumdirection3 = enumdirection1.getClockWise();

                                if (!woodlandmansionpieces_c.isRoomId(woodlandmansionpieces_g3, i2 + enumdirection3.getStepX(), l1 + enumdirection3.getStepZ(), i1, l2)) {
                                    enumdirection3 = enumdirection3.getOpposite();
                                }

                                this.addRoom2x2(list, blockposition3, enumblockrotation, enumdirection3, enumdirection1, awoodlandmansionpieces_b[i1]);
                            } else if (k2 == 262144 && enumdirection1 == EnumDirection.UP) {
                                this.addRoom2x2Secret(list, blockposition3, enumblockrotation, awoodlandmansionpieces_b[i1]);
                            }
                        }
                    }
                }
            }

        }

        private void traverseOuterWalls(List<WoodlandMansionPieces.i> list, WoodlandMansionPieces.e woodlandmansionpieces_e, WoodlandMansionPieces.g woodlandmansionpieces_g, EnumDirection enumdirection, int i, int j, int k, int l) {
            int i1 = i;
            int j1 = j;
            EnumDirection enumdirection1 = enumdirection;

            do {
                if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, i1 + enumdirection.getStepX(), j1 + enumdirection.getStepZ())) {
                    this.traverseTurn(list, woodlandmansionpieces_e);
                    enumdirection = enumdirection.getClockWise();
                    if (i1 != k || j1 != l || enumdirection1 != enumdirection) {
                        this.traverseWallPiece(list, woodlandmansionpieces_e);
                    }
                } else if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, i1 + enumdirection.getStepX(), j1 + enumdirection.getStepZ()) && WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, i1 + enumdirection.getStepX() + enumdirection.getCounterClockWise().getStepX(), j1 + enumdirection.getStepZ() + enumdirection.getCounterClockWise().getStepZ())) {
                    this.traverseInnerTurn(list, woodlandmansionpieces_e);
                    i1 += enumdirection.getStepX();
                    j1 += enumdirection.getStepZ();
                    enumdirection = enumdirection.getCounterClockWise();
                } else {
                    i1 += enumdirection.getStepX();
                    j1 += enumdirection.getStepZ();
                    if (i1 != k || j1 != l || enumdirection1 != enumdirection) {
                        this.traverseWallPiece(list, woodlandmansionpieces_e);
                    }
                }
            } while (i1 != k || j1 != l || enumdirection1 != enumdirection);

        }

        private void createRoof(List<WoodlandMansionPieces.i> list, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WoodlandMansionPieces.g woodlandmansionpieces_g, @Nullable WoodlandMansionPieces.g woodlandmansionpieces_g1) {
            for (int i = 0; i < woodlandmansionpieces_g.height; ++i) {
                for (int j = 0; j < woodlandmansionpieces_g.width; ++j) {
                    BlockPosition blockposition1 = blockposition.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8 + (i - this.startY) * 8);

                    blockposition1 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.EAST), (j - this.startX) * 8);
                    boolean flag = woodlandmansionpieces_g1 != null && WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g1, j, i);

                    if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j, i) && !flag) {
                        list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof", blockposition1.above(3), enumblockrotation));
                        if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j + 1, i)) {
                            BlockPosition blockposition2 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.EAST), 6);

                            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_front", blockposition2, enumblockrotation));
                        }

                        if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j - 1, i)) {
                            BlockPosition blockposition3 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.EAST), 0);

                            blockposition3 = blockposition3.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 7);
                            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_front", blockposition3, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
                        }

                        if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j, i - 1)) {
                            BlockPosition blockposition4 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.WEST), 1);

                            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_front", blockposition4, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
                        }

                        if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j, i + 1)) {
                            BlockPosition blockposition5 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.EAST), 6);

                            blockposition5 = blockposition5.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_front", blockposition5, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                        }
                    }
                }
            }

            if (woodlandmansionpieces_g1 != null) {
                for (int k = 0; k < woodlandmansionpieces_g.height; ++k) {
                    for (int l = 0; l < woodlandmansionpieces_g.width; ++l) {
                        BlockPosition blockposition6 = blockposition.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8 + (k - this.startY) * 8);

                        blockposition6 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.EAST), (l - this.startX) * 8);
                        boolean flag1 = WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g1, l, k);

                        if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k) && flag1) {
                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l + 1, k)) {
                                BlockPosition blockposition7 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall", blockposition7, enumblockrotation));
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l - 1, k)) {
                                BlockPosition blockposition8 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.WEST), 1);

                                blockposition8 = blockposition8.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall", blockposition8, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k - 1)) {
                                BlockPosition blockposition9 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.WEST), 0);

                                blockposition9 = blockposition9.relative(enumblockrotation.rotate(EnumDirection.NORTH), 1);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall", blockposition9, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k + 1)) {
                                BlockPosition blockposition10 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.EAST), 6);

                                blockposition10 = blockposition10.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 7);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall", blockposition10, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l + 1, k)) {
                                if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k - 1)) {
                                    BlockPosition blockposition11 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                                    blockposition11 = blockposition11.relative(enumblockrotation.rotate(EnumDirection.NORTH), 2);
                                    list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall_corner", blockposition11, enumblockrotation));
                                }

                                if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k + 1)) {
                                    BlockPosition blockposition12 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.EAST), 8);

                                    blockposition12 = blockposition12.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 7);
                                    list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall_corner", blockposition12, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                                }
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l - 1, k)) {
                                if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k - 1)) {
                                    BlockPosition blockposition13 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.WEST), 2);

                                    blockposition13 = blockposition13.relative(enumblockrotation.rotate(EnumDirection.NORTH), 1);
                                    list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall_corner", blockposition13, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
                                }

                                if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, l, k + 1)) {
                                    BlockPosition blockposition14 = blockposition6.relative(enumblockrotation.rotate(EnumDirection.WEST), 1);

                                    blockposition14 = blockposition14.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8);
                                    list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "small_wall_corner", blockposition14, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
                                }
                            }
                        }
                    }
                }
            }

            for (int i1 = 0; i1 < woodlandmansionpieces_g.height; ++i1) {
                for (int j1 = 0; j1 < woodlandmansionpieces_g.width; ++j1) {
                    BlockPosition blockposition15 = blockposition.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8 + (i1 - this.startY) * 8);

                    blockposition15 = blockposition15.relative(enumblockrotation.rotate(EnumDirection.EAST), (j1 - this.startX) * 8);
                    boolean flag2 = woodlandmansionpieces_g1 != null && WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g1, j1, i1);

                    if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1, i1) && !flag2) {
                        if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1 + 1, i1)) {
                            BlockPosition blockposition16 = blockposition15.relative(enumblockrotation.rotate(EnumDirection.EAST), 6);

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1, i1 + 1)) {
                                BlockPosition blockposition17 = blockposition16.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);

                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_corner", blockposition17, enumblockrotation));
                            } else if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1 + 1, i1 + 1)) {
                                BlockPosition blockposition18 = blockposition16.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 5);

                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_inner_corner", blockposition18, enumblockrotation));
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1, i1 - 1)) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_corner", blockposition16, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
                            } else if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1 + 1, i1 - 1)) {
                                BlockPosition blockposition19 = blockposition15.relative(enumblockrotation.rotate(EnumDirection.EAST), 9);

                                blockposition19 = blockposition19.relative(enumblockrotation.rotate(EnumDirection.NORTH), 2);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_inner_corner", blockposition19, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                            }
                        }

                        if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1 - 1, i1)) {
                            BlockPosition blockposition20 = blockposition15.relative(enumblockrotation.rotate(EnumDirection.EAST), 0);

                            blockposition20 = blockposition20.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 0);
                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1, i1 + 1)) {
                                BlockPosition blockposition21 = blockposition20.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);

                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_corner", blockposition21, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
                            } else if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1 - 1, i1 + 1)) {
                                BlockPosition blockposition22 = blockposition20.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 8);

                                blockposition22 = blockposition22.relative(enumblockrotation.rotate(EnumDirection.WEST), 3);
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_inner_corner", blockposition22, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
                            }

                            if (!WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1, i1 - 1)) {
                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_corner", blockposition20, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
                            } else if (WoodlandMansionPieces.c.isHouse(woodlandmansionpieces_g, j1 - 1, i1 - 1)) {
                                BlockPosition blockposition23 = blockposition20.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 1);

                                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "roof_inner_corner", blockposition23, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
                            }
                        }
                    }
                }
            }

        }

        private void entrance(List<WoodlandMansionPieces.i> list, WoodlandMansionPieces.e woodlandmansionpieces_e) {
            EnumDirection enumdirection = woodlandmansionpieces_e.rotation.rotate(EnumDirection.WEST);

            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "entrance", woodlandmansionpieces_e.position.relative(enumdirection, 9), woodlandmansionpieces_e.rotation));
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.SOUTH), 16);
        }

        private void traverseWallPiece(List<WoodlandMansionPieces.i> list, WoodlandMansionPieces.e woodlandmansionpieces_e) {
            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_e.wallType, woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.EAST), 7), woodlandmansionpieces_e.rotation));
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.SOUTH), 8);
        }

        private void traverseTurn(List<WoodlandMansionPieces.i> list, WoodlandMansionPieces.e woodlandmansionpieces_e) {
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.SOUTH), -1);
            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, "wall_corner", woodlandmansionpieces_e.position, woodlandmansionpieces_e.rotation));
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.SOUTH), -7);
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.WEST), -6);
            woodlandmansionpieces_e.rotation = woodlandmansionpieces_e.rotation.getRotated(EnumBlockRotation.CLOCKWISE_90);
        }

        private void traverseInnerTurn(List<WoodlandMansionPieces.i> list, WoodlandMansionPieces.e woodlandmansionpieces_e) {
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.SOUTH), 6);
            woodlandmansionpieces_e.position = woodlandmansionpieces_e.position.relative(woodlandmansionpieces_e.rotation.rotate(EnumDirection.EAST), 8);
            woodlandmansionpieces_e.rotation = woodlandmansionpieces_e.rotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90);
        }

        private void addRoom1x1(List<WoodlandMansionPieces.i> list, BlockPosition blockposition, EnumBlockRotation enumblockrotation, EnumDirection enumdirection, WoodlandMansionPieces.b woodlandmansionpieces_b) {
            EnumBlockRotation enumblockrotation1 = EnumBlockRotation.NONE;
            String s = woodlandmansionpieces_b.get1x1(this.random);

            if (enumdirection != EnumDirection.EAST) {
                if (enumdirection == EnumDirection.NORTH) {
                    enumblockrotation1 = enumblockrotation1.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90);
                } else if (enumdirection == EnumDirection.WEST) {
                    enumblockrotation1 = enumblockrotation1.getRotated(EnumBlockRotation.CLOCKWISE_180);
                } else if (enumdirection == EnumDirection.SOUTH) {
                    enumblockrotation1 = enumblockrotation1.getRotated(EnumBlockRotation.CLOCKWISE_90);
                } else {
                    s = woodlandmansionpieces_b.get1x1Secret(this.random);
                }
            }

            BlockPosition blockposition1 = DefinedStructure.getZeroPositionWithTransform(new BlockPosition(1, 0, 0), EnumBlockMirror.NONE, enumblockrotation1, 7, 7);

            enumblockrotation1 = enumblockrotation1.getRotated(enumblockrotation);
            blockposition1 = blockposition1.rotate(enumblockrotation);
            BlockPosition blockposition2 = blockposition.offset(blockposition1.getX(), 0, blockposition1.getZ());

            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, s, blockposition2, enumblockrotation1));
        }

        private void addRoom1x2(List<WoodlandMansionPieces.i> list, BlockPosition blockposition, EnumBlockRotation enumblockrotation, EnumDirection enumdirection, EnumDirection enumdirection1, WoodlandMansionPieces.b woodlandmansionpieces_b, boolean flag) {
            if (enumdirection1 == EnumDirection.EAST && enumdirection == EnumDirection.SOUTH) {
                BlockPosition blockposition1 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition1, enumblockrotation));
            } else if (enumdirection1 == EnumDirection.EAST && enumdirection == EnumDirection.NORTH) {
                BlockPosition blockposition2 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

                blockposition2 = blockposition2.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition2, enumblockrotation, EnumBlockMirror.LEFT_RIGHT));
            } else if (enumdirection1 == EnumDirection.WEST && enumdirection == EnumDirection.NORTH) {
                BlockPosition blockposition3 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                blockposition3 = blockposition3.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition3, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
            } else if (enumdirection1 == EnumDirection.WEST && enumdirection == EnumDirection.SOUTH) {
                BlockPosition blockposition4 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition4, enumblockrotation, EnumBlockMirror.FRONT_BACK));
            } else if (enumdirection1 == EnumDirection.SOUTH && enumdirection == EnumDirection.EAST) {
                BlockPosition blockposition5 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition5, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90), EnumBlockMirror.LEFT_RIGHT));
            } else if (enumdirection1 == EnumDirection.SOUTH && enumdirection == EnumDirection.WEST) {
                BlockPosition blockposition6 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition6, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
            } else if (enumdirection1 == EnumDirection.NORTH && enumdirection == EnumDirection.WEST) {
                BlockPosition blockposition7 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                blockposition7 = blockposition7.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition7, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90), EnumBlockMirror.FRONT_BACK));
            } else if (enumdirection1 == EnumDirection.NORTH && enumdirection == EnumDirection.EAST) {
                BlockPosition blockposition8 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

                blockposition8 = blockposition8.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2SideEntrance(this.random, flag), blockposition8, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
            } else if (enumdirection1 == EnumDirection.SOUTH && enumdirection == EnumDirection.NORTH) {
                BlockPosition blockposition9 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

                blockposition9 = blockposition9.relative(enumblockrotation.rotate(EnumDirection.NORTH), 8);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2FrontEntrance(this.random, flag), blockposition9, enumblockrotation));
            } else if (enumdirection1 == EnumDirection.NORTH && enumdirection == EnumDirection.SOUTH) {
                BlockPosition blockposition10 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 7);

                blockposition10 = blockposition10.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 14);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2FrontEntrance(this.random, flag), blockposition10, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180)));
            } else if (enumdirection1 == EnumDirection.WEST && enumdirection == EnumDirection.EAST) {
                BlockPosition blockposition11 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 15);

                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2FrontEntrance(this.random, flag), blockposition11, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
            } else if (enumdirection1 == EnumDirection.EAST && enumdirection == EnumDirection.WEST) {
                BlockPosition blockposition12 = blockposition.relative(enumblockrotation.rotate(EnumDirection.WEST), 7);

                blockposition12 = blockposition12.relative(enumblockrotation.rotate(EnumDirection.SOUTH), 6);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2FrontEntrance(this.random, flag), blockposition12, enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90)));
            } else if (enumdirection1 == EnumDirection.UP && enumdirection == EnumDirection.EAST) {
                BlockPosition blockposition13 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 15);

                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2Secret(this.random), blockposition13, enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90)));
            } else if (enumdirection1 == EnumDirection.UP && enumdirection == EnumDirection.SOUTH) {
                BlockPosition blockposition14 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

                blockposition14 = blockposition14.relative(enumblockrotation.rotate(EnumDirection.NORTH), 0);
                list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get1x2Secret(this.random), blockposition14, enumblockrotation));
            }

        }

        private void addRoom2x2(List<WoodlandMansionPieces.i> list, BlockPosition blockposition, EnumBlockRotation enumblockrotation, EnumDirection enumdirection, EnumDirection enumdirection1, WoodlandMansionPieces.b woodlandmansionpieces_b) {
            int i = 0;
            int j = 0;
            EnumBlockRotation enumblockrotation1 = enumblockrotation;
            EnumBlockMirror enumblockmirror = EnumBlockMirror.NONE;

            if (enumdirection1 == EnumDirection.EAST && enumdirection == EnumDirection.SOUTH) {
                i = -7;
            } else if (enumdirection1 == EnumDirection.EAST && enumdirection == EnumDirection.NORTH) {
                i = -7;
                j = 6;
                enumblockmirror = EnumBlockMirror.LEFT_RIGHT;
            } else if (enumdirection1 == EnumDirection.NORTH && enumdirection == EnumDirection.EAST) {
                i = 1;
                j = 14;
                enumblockrotation1 = enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90);
            } else if (enumdirection1 == EnumDirection.NORTH && enumdirection == EnumDirection.WEST) {
                i = 7;
                j = 14;
                enumblockrotation1 = enumblockrotation.getRotated(EnumBlockRotation.COUNTERCLOCKWISE_90);
                enumblockmirror = EnumBlockMirror.LEFT_RIGHT;
            } else if (enumdirection1 == EnumDirection.SOUTH && enumdirection == EnumDirection.WEST) {
                i = 7;
                j = -8;
                enumblockrotation1 = enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90);
            } else if (enumdirection1 == EnumDirection.SOUTH && enumdirection == EnumDirection.EAST) {
                i = 1;
                j = -8;
                enumblockrotation1 = enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_90);
                enumblockmirror = EnumBlockMirror.LEFT_RIGHT;
            } else if (enumdirection1 == EnumDirection.WEST && enumdirection == EnumDirection.NORTH) {
                i = 15;
                j = 6;
                enumblockrotation1 = enumblockrotation.getRotated(EnumBlockRotation.CLOCKWISE_180);
            } else if (enumdirection1 == EnumDirection.WEST && enumdirection == EnumDirection.SOUTH) {
                i = 15;
                enumblockmirror = EnumBlockMirror.FRONT_BACK;
            }

            BlockPosition blockposition1 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), i);

            blockposition1 = blockposition1.relative(enumblockrotation.rotate(EnumDirection.SOUTH), j);
            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get2x2(this.random), blockposition1, enumblockrotation1, enumblockmirror));
        }

        private void addRoom2x2Secret(List<WoodlandMansionPieces.i> list, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WoodlandMansionPieces.b woodlandmansionpieces_b) {
            BlockPosition blockposition1 = blockposition.relative(enumblockrotation.rotate(EnumDirection.EAST), 1);

            list.add(new WoodlandMansionPieces.i(this.structureTemplateManager, woodlandmansionpieces_b.get2x2Secret(this.random), blockposition1, enumblockrotation, EnumBlockMirror.NONE));
        }
    }

    private static class c {

        private static final int DEFAULT_SIZE = 11;
        private static final int CLEAR = 0;
        private static final int CORRIDOR = 1;
        private static final int ROOM = 2;
        private static final int START_ROOM = 3;
        private static final int TEST_ROOM = 4;
        private static final int BLOCKED = 5;
        private static final int ROOM_1x1 = 65536;
        private static final int ROOM_1x2 = 131072;
        private static final int ROOM_2x2 = 262144;
        private static final int ROOM_ORIGIN_FLAG = 1048576;
        private static final int ROOM_DOOR_FLAG = 2097152;
        private static final int ROOM_STAIRS_FLAG = 4194304;
        private static final int ROOM_CORRIDOR_FLAG = 8388608;
        private static final int ROOM_TYPE_MASK = 983040;
        private static final int ROOM_ID_MASK = 65535;
        private final RandomSource random;
        final WoodlandMansionPieces.g baseGrid;
        final WoodlandMansionPieces.g thirdFloorGrid;
        final WoodlandMansionPieces.g[] floorRooms;
        final int entranceX;
        final int entranceY;

        public c(RandomSource randomsource) {
            this.random = randomsource;
            int i = 11;

            this.entranceX = 7;
            this.entranceY = 4;
            this.baseGrid = new WoodlandMansionPieces.g(11, 11, 5);
            this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
            this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
            this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
            this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
            this.baseGrid.set(0, 0, 11, 1, 5);
            this.baseGrid.set(0, 9, 11, 11, 5);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, EnumDirection.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, EnumDirection.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, EnumDirection.WEST, 3);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, EnumDirection.WEST, 3);

            while (this.cleanEdges(this.baseGrid)) {
                ;
            }

            this.floorRooms = new WoodlandMansionPieces.g[3];
            this.floorRooms[0] = new WoodlandMansionPieces.g(11, 11, 5);
            this.floorRooms[1] = new WoodlandMansionPieces.g(11, 11, 5);
            this.floorRooms[2] = new WoodlandMansionPieces.g(11, 11, 5);
            this.identifyRooms(this.baseGrid, this.floorRooms[0]);
            this.identifyRooms(this.baseGrid, this.floorRooms[1]);
            this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.thirdFloorGrid = new WoodlandMansionPieces.g(this.baseGrid.width, this.baseGrid.height, 5);
            this.setupThirdFloor();
            this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
        }

        public static boolean isHouse(WoodlandMansionPieces.g woodlandmansionpieces_g, int i, int j) {
            int k = woodlandmansionpieces_g.get(i, j);

            return k == 1 || k == 2 || k == 3 || k == 4;
        }

        public boolean isRoomId(WoodlandMansionPieces.g woodlandmansionpieces_g, int i, int j, int k, int l) {
            return (this.floorRooms[k].get(i, j) & '\uffff') == l;
        }

        @Nullable
        public EnumDirection get1x2RoomDirection(WoodlandMansionPieces.g woodlandmansionpieces_g, int i, int j, int k, int l) {
            for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                if (this.isRoomId(woodlandmansionpieces_g, i + enumdirection.getStepX(), j + enumdirection.getStepZ(), k, l)) {
                    return enumdirection;
                }
            }

            return null;
        }

        private void recursiveCorridor(WoodlandMansionPieces.g woodlandmansionpieces_g, int i, int j, EnumDirection enumdirection, int k) {
            if (k > 0) {
                woodlandmansionpieces_g.set(i, j, 1);
                woodlandmansionpieces_g.setif(i + enumdirection.getStepX(), j + enumdirection.getStepZ(), 0, 1);

                for (int l = 0; l < 8; ++l) {
                    EnumDirection enumdirection1 = EnumDirection.from2DDataValue(this.random.nextInt(4));

                    if (enumdirection1 != enumdirection.getOpposite() && (enumdirection1 != EnumDirection.EAST || !this.random.nextBoolean())) {
                        int i1 = i + enumdirection.getStepX();
                        int j1 = j + enumdirection.getStepZ();

                        if (woodlandmansionpieces_g.get(i1 + enumdirection1.getStepX(), j1 + enumdirection1.getStepZ()) == 0 && woodlandmansionpieces_g.get(i1 + enumdirection1.getStepX() * 2, j1 + enumdirection1.getStepZ() * 2) == 0) {
                            this.recursiveCorridor(woodlandmansionpieces_g, i + enumdirection.getStepX() + enumdirection1.getStepX(), j + enumdirection.getStepZ() + enumdirection1.getStepZ(), enumdirection1, k - 1);
                            break;
                        }
                    }
                }

                EnumDirection enumdirection2 = enumdirection.getClockWise();
                EnumDirection enumdirection3 = enumdirection.getCounterClockWise();

                woodlandmansionpieces_g.setif(i + enumdirection2.getStepX(), j + enumdirection2.getStepZ(), 0, 2);
                woodlandmansionpieces_g.setif(i + enumdirection3.getStepX(), j + enumdirection3.getStepZ(), 0, 2);
                woodlandmansionpieces_g.setif(i + enumdirection.getStepX() + enumdirection2.getStepX(), j + enumdirection.getStepZ() + enumdirection2.getStepZ(), 0, 2);
                woodlandmansionpieces_g.setif(i + enumdirection.getStepX() + enumdirection3.getStepX(), j + enumdirection.getStepZ() + enumdirection3.getStepZ(), 0, 2);
                woodlandmansionpieces_g.setif(i + enumdirection.getStepX() * 2, j + enumdirection.getStepZ() * 2, 0, 2);
                woodlandmansionpieces_g.setif(i + enumdirection2.getStepX() * 2, j + enumdirection2.getStepZ() * 2, 0, 2);
                woodlandmansionpieces_g.setif(i + enumdirection3.getStepX() * 2, j + enumdirection3.getStepZ() * 2, 0, 2);
            }
        }

        private boolean cleanEdges(WoodlandMansionPieces.g woodlandmansionpieces_g) {
            boolean flag = false;

            for (int i = 0; i < woodlandmansionpieces_g.height; ++i) {
                for (int j = 0; j < woodlandmansionpieces_g.width; ++j) {
                    if (woodlandmansionpieces_g.get(j, i) == 0) {
                        int k = 0;

                        k += isHouse(woodlandmansionpieces_g, j + 1, i) ? 1 : 0;
                        k += isHouse(woodlandmansionpieces_g, j - 1, i) ? 1 : 0;
                        k += isHouse(woodlandmansionpieces_g, j, i + 1) ? 1 : 0;
                        k += isHouse(woodlandmansionpieces_g, j, i - 1) ? 1 : 0;
                        if (k >= 3) {
                            woodlandmansionpieces_g.set(j, i, 2);
                            flag = true;
                        } else if (k == 2) {
                            int l = 0;

                            l += isHouse(woodlandmansionpieces_g, j + 1, i + 1) ? 1 : 0;
                            l += isHouse(woodlandmansionpieces_g, j - 1, i + 1) ? 1 : 0;
                            l += isHouse(woodlandmansionpieces_g, j + 1, i - 1) ? 1 : 0;
                            l += isHouse(woodlandmansionpieces_g, j - 1, i - 1) ? 1 : 0;
                            if (l <= 1) {
                                woodlandmansionpieces_g.set(j, i, 2);
                                flag = true;
                            }
                        }
                    }
                }
            }

            return flag;
        }

        private void setupThirdFloor() {
            List<Tuple<Integer, Integer>> list = Lists.newArrayList();
            WoodlandMansionPieces.g woodlandmansionpieces_g = this.floorRooms[1];

            for (int i = 0; i < this.thirdFloorGrid.height; ++i) {
                for (int j = 0; j < this.thirdFloorGrid.width; ++j) {
                    int k = woodlandmansionpieces_g.get(j, i);
                    int l = k & 983040;

                    if (l == 131072 && (k & 2097152) == 2097152) {
                        list.add(new Tuple(j, i));
                    }
                }
            }

            if (list.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
            } else {
                Tuple<Integer, Integer> tuple = (Tuple) list.get(this.random.nextInt(list.size()));
                int i1 = woodlandmansionpieces_g.get((Integer) tuple.getA(), (Integer) tuple.getB());

                woodlandmansionpieces_g.set((Integer) tuple.getA(), (Integer) tuple.getB(), i1 | 4194304);
                EnumDirection enumdirection = this.get1x2RoomDirection(this.baseGrid, (Integer) tuple.getA(), (Integer) tuple.getB(), 1, i1 & '\uffff');
                int j1 = (Integer) tuple.getA() + enumdirection.getStepX();
                int k1 = (Integer) tuple.getB() + enumdirection.getStepZ();

                for (int l1 = 0; l1 < this.thirdFloorGrid.height; ++l1) {
                    for (int i2 = 0; i2 < this.thirdFloorGrid.width; ++i2) {
                        if (!isHouse(this.baseGrid, i2, l1)) {
                            this.thirdFloorGrid.set(i2, l1, 5);
                        } else if (i2 == (Integer) tuple.getA() && l1 == (Integer) tuple.getB()) {
                            this.thirdFloorGrid.set(i2, l1, 3);
                        } else if (i2 == j1 && l1 == k1) {
                            this.thirdFloorGrid.set(i2, l1, 3);
                            this.floorRooms[2].set(i2, l1, 8388608);
                        }
                    }
                }

                List<EnumDirection> list1 = Lists.newArrayList();

                for (EnumDirection enumdirection1 : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                    if (this.thirdFloorGrid.get(j1 + enumdirection1.getStepX(), k1 + enumdirection1.getStepZ()) == 0) {
                        list1.add(enumdirection1);
                    }
                }

                if (list1.isEmpty()) {
                    this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                    woodlandmansionpieces_g.set((Integer) tuple.getA(), (Integer) tuple.getB(), i1);
                } else {
                    EnumDirection enumdirection2 = (EnumDirection) list1.get(this.random.nextInt(list1.size()));

                    this.recursiveCorridor(this.thirdFloorGrid, j1 + enumdirection2.getStepX(), k1 + enumdirection2.getStepZ(), enumdirection2, 4);

                    while (this.cleanEdges(this.thirdFloorGrid)) {
                        ;
                    }

                }
            }
        }

        private void identifyRooms(WoodlandMansionPieces.g woodlandmansionpieces_g, WoodlandMansionPieces.g woodlandmansionpieces_g1) {
            ObjectArrayList<Tuple<Integer, Integer>> objectarraylist = new ObjectArrayList();

            for (int i = 0; i < woodlandmansionpieces_g.height; ++i) {
                for (int j = 0; j < woodlandmansionpieces_g.width; ++j) {
                    if (woodlandmansionpieces_g.get(j, i) == 2) {
                        objectarraylist.add(new Tuple(j, i));
                    }
                }
            }

            SystemUtils.shuffle(objectarraylist, this.random);
            int k = 10;
            ObjectListIterator objectlistiterator = objectarraylist.iterator();

            while (objectlistiterator.hasNext()) {
                Tuple<Integer, Integer> tuple = (Tuple) objectlistiterator.next();
                int l = (Integer) tuple.getA();
                int i1 = (Integer) tuple.getB();

                if (woodlandmansionpieces_g1.get(l, i1) == 0) {
                    int j1 = l;
                    int k1 = l;
                    int l1 = i1;
                    int i2 = i1;
                    int j2 = 65536;

                    if (woodlandmansionpieces_g1.get(l + 1, i1) == 0 && woodlandmansionpieces_g1.get(l, i1 + 1) == 0 && woodlandmansionpieces_g1.get(l + 1, i1 + 1) == 0 && woodlandmansionpieces_g.get(l + 1, i1) == 2 && woodlandmansionpieces_g.get(l, i1 + 1) == 2 && woodlandmansionpieces_g.get(l + 1, i1 + 1) == 2) {
                        k1 = l + 1;
                        i2 = i1 + 1;
                        j2 = 262144;
                    } else if (woodlandmansionpieces_g1.get(l - 1, i1) == 0 && woodlandmansionpieces_g1.get(l, i1 + 1) == 0 && woodlandmansionpieces_g1.get(l - 1, i1 + 1) == 0 && woodlandmansionpieces_g.get(l - 1, i1) == 2 && woodlandmansionpieces_g.get(l, i1 + 1) == 2 && woodlandmansionpieces_g.get(l - 1, i1 + 1) == 2) {
                        j1 = l - 1;
                        i2 = i1 + 1;
                        j2 = 262144;
                    } else if (woodlandmansionpieces_g1.get(l - 1, i1) == 0 && woodlandmansionpieces_g1.get(l, i1 - 1) == 0 && woodlandmansionpieces_g1.get(l - 1, i1 - 1) == 0 && woodlandmansionpieces_g.get(l - 1, i1) == 2 && woodlandmansionpieces_g.get(l, i1 - 1) == 2 && woodlandmansionpieces_g.get(l - 1, i1 - 1) == 2) {
                        j1 = l - 1;
                        l1 = i1 - 1;
                        j2 = 262144;
                    } else if (woodlandmansionpieces_g1.get(l + 1, i1) == 0 && woodlandmansionpieces_g.get(l + 1, i1) == 2) {
                        k1 = l + 1;
                        j2 = 131072;
                    } else if (woodlandmansionpieces_g1.get(l, i1 + 1) == 0 && woodlandmansionpieces_g.get(l, i1 + 1) == 2) {
                        i2 = i1 + 1;
                        j2 = 131072;
                    } else if (woodlandmansionpieces_g1.get(l - 1, i1) == 0 && woodlandmansionpieces_g.get(l - 1, i1) == 2) {
                        j1 = l - 1;
                        j2 = 131072;
                    } else if (woodlandmansionpieces_g1.get(l, i1 - 1) == 0 && woodlandmansionpieces_g.get(l, i1 - 1) == 2) {
                        l1 = i1 - 1;
                        j2 = 131072;
                    }

                    int k2 = this.random.nextBoolean() ? j1 : k1;
                    int l2 = this.random.nextBoolean() ? l1 : i2;
                    int i3 = 2097152;

                    if (!woodlandmansionpieces_g.edgesTo(k2, l2, 1)) {
                        k2 = k2 == j1 ? k1 : j1;
                        l2 = l2 == l1 ? i2 : l1;
                        if (!woodlandmansionpieces_g.edgesTo(k2, l2, 1)) {
                            l2 = l2 == l1 ? i2 : l1;
                            if (!woodlandmansionpieces_g.edgesTo(k2, l2, 1)) {
                                k2 = k2 == j1 ? k1 : j1;
                                l2 = l2 == l1 ? i2 : l1;
                                if (!woodlandmansionpieces_g.edgesTo(k2, l2, 1)) {
                                    i3 = 0;
                                    k2 = j1;
                                    l2 = l1;
                                }
                            }
                        }
                    }

                    for (int j3 = l1; j3 <= i2; ++j3) {
                        for (int k3 = j1; k3 <= k1; ++k3) {
                            if (k3 == k2 && j3 == l2) {
                                woodlandmansionpieces_g1.set(k3, j3, 1048576 | i3 | j2 | k);
                            } else {
                                woodlandmansionpieces_g1.set(k3, j3, j2 | k);
                            }
                        }
                    }

                    ++k;
                }
            }

        }
    }

    private static class g {

        private final int[][] grid;
        final int width;
        final int height;
        private final int valueIfOutside;

        public g(int i, int j, int k) {
            this.width = i;
            this.height = j;
            this.valueIfOutside = k;
            this.grid = new int[i][j];
        }

        public void set(int i, int j, int k) {
            if (i >= 0 && i < this.width && j >= 0 && j < this.height) {
                this.grid[i][j] = k;
            }

        }

        public void set(int i, int j, int k, int l, int i1) {
            for (int j1 = j; j1 <= l; ++j1) {
                for (int k1 = i; k1 <= k; ++k1) {
                    this.set(k1, j1, i1);
                }
            }

        }

        public int get(int i, int j) {
            return i >= 0 && i < this.width && j >= 0 && j < this.height ? this.grid[i][j] : this.valueIfOutside;
        }

        public void setif(int i, int j, int k, int l) {
            if (this.get(i, j) == k) {
                this.set(i, j, l);
            }

        }

        public boolean edgesTo(int i, int j, int k) {
            return this.get(i - 1, j) == k || this.get(i + 1, j) == k || this.get(i, j + 1) == k || this.get(i, j - 1) == k;
        }
    }

    private abstract static class b {

        b() {}

        public abstract String get1x1(RandomSource randomsource);

        public abstract String get1x1Secret(RandomSource randomsource);

        public abstract String get1x2SideEntrance(RandomSource randomsource, boolean flag);

        public abstract String get1x2FrontEntrance(RandomSource randomsource, boolean flag);

        public abstract String get1x2Secret(RandomSource randomsource);

        public abstract String get2x2(RandomSource randomsource);

        public abstract String get2x2Secret(RandomSource randomsource);
    }

    private static class a extends WoodlandMansionPieces.b {

        a() {}

        @Override
        public String get1x1(RandomSource randomsource) {
            int i = randomsource.nextInt(5);

            return "1x1_a" + (i + 1);
        }

        @Override
        public String get1x1Secret(RandomSource randomsource) {
            int i = randomsource.nextInt(4);

            return "1x1_as" + (i + 1);
        }

        @Override
        public String get1x2SideEntrance(RandomSource randomsource, boolean flag) {
            int i = randomsource.nextInt(9);

            return "1x2_a" + (i + 1);
        }

        @Override
        public String get1x2FrontEntrance(RandomSource randomsource, boolean flag) {
            int i = randomsource.nextInt(5);

            return "1x2_b" + (i + 1);
        }

        @Override
        public String get1x2Secret(RandomSource randomsource) {
            int i = randomsource.nextInt(2);

            return "1x2_s" + (i + 1);
        }

        @Override
        public String get2x2(RandomSource randomsource) {
            int i = randomsource.nextInt(4);

            return "2x2_a" + (i + 1);
        }

        @Override
        public String get2x2Secret(RandomSource randomsource) {
            return "2x2_s1";
        }
    }

    private static class f extends WoodlandMansionPieces.b {

        f() {}

        @Override
        public String get1x1(RandomSource randomsource) {
            int i = randomsource.nextInt(4);

            return "1x1_b" + (i + 1);
        }

        @Override
        public String get1x1Secret(RandomSource randomsource) {
            int i = randomsource.nextInt(4);

            return "1x1_as" + (i + 1);
        }

        @Override
        public String get1x2SideEntrance(RandomSource randomsource, boolean flag) {
            if (flag) {
                return "1x2_c_stairs";
            } else {
                int i = randomsource.nextInt(4);

                return "1x2_c" + (i + 1);
            }
        }

        @Override
        public String get1x2FrontEntrance(RandomSource randomsource, boolean flag) {
            if (flag) {
                return "1x2_d_stairs";
            } else {
                int i = randomsource.nextInt(5);

                return "1x2_d" + (i + 1);
            }
        }

        @Override
        public String get1x2Secret(RandomSource randomsource) {
            int i = randomsource.nextInt(1);

            return "1x2_se" + (i + 1);
        }

        @Override
        public String get2x2(RandomSource randomsource) {
            int i = randomsource.nextInt(5);

            return "2x2_b" + (i + 1);
        }

        @Override
        public String get2x2Secret(RandomSource randomsource) {
            return "2x2_s1";
        }
    }

    private static class h extends WoodlandMansionPieces.f {

        h() {}
    }
}
