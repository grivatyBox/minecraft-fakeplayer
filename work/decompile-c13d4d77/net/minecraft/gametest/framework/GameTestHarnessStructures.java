package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.ArgumentTileLocation;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityCommand;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyStructureMode;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.phys.AxisAlignedBB;
import org.slf4j.Logger;

public class GameTestHarnessStructures {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
    public static String testStructuresDir = "gameteststructures";

    public GameTestHarnessStructures() {}

    public static EnumBlockRotation getRotationForRotationSteps(int i) {
        switch (i) {
            case 0:
                return EnumBlockRotation.NONE;
            case 1:
                return EnumBlockRotation.CLOCKWISE_90;
            case 2:
                return EnumBlockRotation.CLOCKWISE_180;
            case 3:
                return EnumBlockRotation.COUNTERCLOCKWISE_90;
            default:
                throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + i);
        }
    }

    public static int getRotationStepsForRotation(EnumBlockRotation enumblockrotation) {
        switch (enumblockrotation) {
            case NONE:
                return 0;
            case CLOCKWISE_90:
                return 1;
            case CLOCKWISE_180:
                return 2;
            case COUNTERCLOCKWISE_90:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + enumblockrotation);
        }
    }

    public static AxisAlignedBB getStructureBounds(TileEntityStructure tileentitystructure) {
        return AxisAlignedBB.of(getStructureBoundingBox(tileentitystructure));
    }

    public static StructureBoundingBox getStructureBoundingBox(TileEntityStructure tileentitystructure) {
        BlockPosition blockposition = getStructureOrigin(tileentitystructure);
        BlockPosition blockposition1 = getTransformedFarCorner(blockposition, tileentitystructure.getStructureSize(), tileentitystructure.getRotation());

        return StructureBoundingBox.fromCorners(blockposition, blockposition1);
    }

    public static BlockPosition getStructureOrigin(TileEntityStructure tileentitystructure) {
        return tileentitystructure.getBlockPos().offset(tileentitystructure.getStructurePos());
    }

    public static void addCommandBlockAndButtonToStartTest(BlockPosition blockposition, BlockPosition blockposition1, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        BlockPosition blockposition2 = DefinedStructure.transform(blockposition.offset(blockposition1), EnumBlockMirror.NONE, enumblockrotation, blockposition);

        worldserver.setBlockAndUpdate(blockposition2, Blocks.COMMAND_BLOCK.defaultBlockState());
        TileEntityCommand tileentitycommand = (TileEntityCommand) worldserver.getBlockEntity(blockposition2);

        tileentitycommand.getCommandBlock().setCommand("test runthis");
        BlockPosition blockposition3 = DefinedStructure.transform(blockposition2.offset(0, 0, -1), EnumBlockMirror.NONE, enumblockrotation, blockposition2);

        worldserver.setBlockAndUpdate(blockposition3, Blocks.STONE_BUTTON.defaultBlockState().rotate(enumblockrotation));
    }

    public static void createNewEmptyStructureBlock(String s, BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        StructureBoundingBox structureboundingbox = getStructureBoundingBox(blockposition.above(), baseblockposition, enumblockrotation);

        clearSpaceForStructure(structureboundingbox, worldserver);
        worldserver.setBlockAndUpdate(blockposition, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        tileentitystructure.setIgnoreEntities(false);
        tileentitystructure.setStructureName(new MinecraftKey(s));
        tileentitystructure.setStructureSize(baseblockposition);
        tileentitystructure.setMode(BlockPropertyStructureMode.SAVE);
        tileentitystructure.setShowBoundingBox(true);
    }

    public static TileEntityStructure prepareTestStructure(GameTestHarnessInfo gametestharnessinfo, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        BaseBlockPosition baseblockposition = ((DefinedStructure) worldserver.getStructureManager().get(new MinecraftKey(gametestharnessinfo.getStructureName())).orElseThrow(() -> {
            return new IllegalStateException("Missing test structure: " + gametestharnessinfo.getStructureName());
        })).getSize();
        StructureBoundingBox structureboundingbox = getStructureBoundingBox(blockposition, baseblockposition, enumblockrotation);
        BlockPosition blockposition1;

        if (enumblockrotation == EnumBlockRotation.NONE) {
            blockposition1 = blockposition;
        } else if (enumblockrotation == EnumBlockRotation.CLOCKWISE_90) {
            blockposition1 = blockposition.offset(baseblockposition.getZ() - 1, 0, 0);
        } else if (enumblockrotation == EnumBlockRotation.CLOCKWISE_180) {
            blockposition1 = blockposition.offset(baseblockposition.getX() - 1, 0, baseblockposition.getZ() - 1);
        } else {
            if (enumblockrotation != EnumBlockRotation.COUNTERCLOCKWISE_90) {
                throw new IllegalArgumentException("Invalid rotation: " + enumblockrotation);
            }

            blockposition1 = blockposition.offset(0, 0, baseblockposition.getX() - 1);
        }

        forceLoadChunks(structureboundingbox, worldserver);
        clearSpaceForStructure(structureboundingbox, worldserver);
        return createStructureBlock(gametestharnessinfo, blockposition1.below(), enumblockrotation, worldserver);
    }

    private static void forceLoadChunks(StructureBoundingBox structureboundingbox, WorldServer worldserver) {
        structureboundingbox.intersectingChunks().forEach((chunkcoordintpair) -> {
            worldserver.setChunkForced(chunkcoordintpair.x, chunkcoordintpair.z, true);
        });
    }

    public static void clearSpaceForStructure(StructureBoundingBox structureboundingbox, WorldServer worldserver) {
        int i = structureboundingbox.minY() - 1;
        StructureBoundingBox structureboundingbox1 = new StructureBoundingBox(structureboundingbox.minX() - 2, structureboundingbox.minY() - 3, structureboundingbox.minZ() - 3, structureboundingbox.maxX() + 3, structureboundingbox.maxY() + 20, structureboundingbox.maxZ() + 3);

        BlockPosition.betweenClosedStream(structureboundingbox1).forEach((blockposition) -> {
            clearBlock(i, blockposition, worldserver);
        });
        worldserver.getBlockTicks().clearArea(structureboundingbox1);
        worldserver.clearBlockEvents(structureboundingbox1);
        AxisAlignedBB axisalignedbb = new AxisAlignedBB((double) structureboundingbox1.minX(), (double) structureboundingbox1.minY(), (double) structureboundingbox1.minZ(), (double) structureboundingbox1.maxX(), (double) structureboundingbox1.maxY(), (double) structureboundingbox1.maxZ());
        List<Entity> list = worldserver.getEntitiesOfClass(Entity.class, axisalignedbb, (entity) -> {
            return !(entity instanceof EntityHuman);
        });

        list.forEach(Entity::discard);
    }

    public static BlockPosition getTransformedFarCorner(BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation) {
        BlockPosition blockposition1 = blockposition.offset(baseblockposition).offset(-1, -1, -1);

        return DefinedStructure.transform(blockposition1, EnumBlockMirror.NONE, enumblockrotation, blockposition);
    }

    public static StructureBoundingBox getStructureBoundingBox(BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation) {
        BlockPosition blockposition1 = getTransformedFarCorner(blockposition, baseblockposition, enumblockrotation);
        StructureBoundingBox structureboundingbox = StructureBoundingBox.fromCorners(blockposition, blockposition1);
        int i = Math.min(structureboundingbox.minX(), structureboundingbox.maxX());
        int j = Math.min(structureboundingbox.minZ(), structureboundingbox.maxZ());

        return structureboundingbox.move(blockposition.getX() - i, 0, blockposition.getZ() - j);
    }

    public static Optional<BlockPosition> findStructureBlockContainingPos(BlockPosition blockposition, int i, WorldServer worldserver) {
        return findStructureBlocks(blockposition, i, worldserver).stream().filter((blockposition1) -> {
            return doesStructureContain(blockposition1, blockposition, worldserver);
        }).findFirst();
    }

    @Nullable
    public static BlockPosition findNearestStructureBlock(BlockPosition blockposition, int i, WorldServer worldserver) {
        Comparator<BlockPosition> comparator = Comparator.comparingInt((blockposition1) -> {
            return blockposition1.distManhattan(blockposition);
        });
        Collection<BlockPosition> collection = findStructureBlocks(blockposition, i, worldserver);
        Optional<BlockPosition> optional = collection.stream().min(comparator);

        return (BlockPosition) optional.orElse((Object) null);
    }

    public static Collection<BlockPosition> findStructureBlocks(BlockPosition blockposition, int i, WorldServer worldserver) {
        Collection<BlockPosition> collection = Lists.newArrayList();
        StructureBoundingBox structureboundingbox = (new StructureBoundingBox(blockposition)).inflatedBy(i);

        BlockPosition.betweenClosedStream(structureboundingbox).forEach((blockposition1) -> {
            if (worldserver.getBlockState(blockposition1).is(Blocks.STRUCTURE_BLOCK)) {
                collection.add(blockposition1.immutable());
            }

        });
        return collection;
    }

    private static TileEntityStructure createStructureBlock(GameTestHarnessInfo gametestharnessinfo, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        worldserver.setBlockAndUpdate(blockposition, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        tileentitystructure.setMode(BlockPropertyStructureMode.LOAD);
        tileentitystructure.setRotation(enumblockrotation);
        tileentitystructure.setIgnoreEntities(false);
        tileentitystructure.setStructureName(new MinecraftKey(gametestharnessinfo.getStructureName()));
        tileentitystructure.setMetaData(gametestharnessinfo.getTestName());
        if (!tileentitystructure.loadStructureInfo(worldserver)) {
            String s = gametestharnessinfo.getTestName();

            throw new RuntimeException("Failed to load structure info for test: " + s + ". Structure name: " + gametestharnessinfo.getStructureName());
        } else {
            return tileentitystructure;
        }
    }

    private static void clearBlock(int i, BlockPosition blockposition, WorldServer worldserver) {
        IBlockData iblockdata;

        if (blockposition.getY() < i) {
            iblockdata = Blocks.STONE.defaultBlockState();
        } else {
            iblockdata = Blocks.AIR.defaultBlockState();
        }

        ArgumentTileLocation argumenttilelocation = new ArgumentTileLocation(iblockdata, Collections.emptySet(), (NBTTagCompound) null);

        argumenttilelocation.place(worldserver, blockposition, 2);
        worldserver.blockUpdated(blockposition, iblockdata.getBlock());
    }

    private static boolean doesStructureContain(BlockPosition blockposition, BlockPosition blockposition1, WorldServer worldserver) {
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        return getStructureBoundingBox(tileentitystructure).isInside(blockposition1);
    }
}
