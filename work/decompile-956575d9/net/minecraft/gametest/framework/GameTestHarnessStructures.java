package net.minecraft.gametest.framework;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.commands.arguments.blocks.ArgumentTileLocation;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class GameTestHarnessStructures {

    public static final int DEFAULT_Y_SEARCH_RADIUS = 10;
    public static final String DEFAULT_TEST_STRUCTURES_DIR = "Minecraft.Server/src/test/convertables/data";
    public static Path testStructuresDir = Paths.get("Minecraft.Server/src/test/convertables/data");

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
                throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + String.valueOf(enumblockrotation));
        }
    }

    public static TestInstanceBlockEntity createNewEmptyTest(MinecraftKey minecraftkey, BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        StructureBoundingBox structureboundingbox = getStructureBoundingBox(TestInstanceBlockEntity.getStructurePos(blockposition), baseblockposition, enumblockrotation);

        clearSpaceForStructure(structureboundingbox, worldserver);
        worldserver.setBlockAndUpdate(blockposition, Blocks.TEST_INSTANCE_BLOCK.defaultBlockState());
        TestInstanceBlockEntity testinstanceblockentity = (TestInstanceBlockEntity) worldserver.getBlockEntity(blockposition);
        ResourceKey<GameTestInstance> resourcekey = ResourceKey.create(Registries.TEST_INSTANCE, minecraftkey);

        testinstanceblockentity.set(new TestInstanceBlockEntity.a(Optional.of(resourcekey), baseblockposition, enumblockrotation, false, TestInstanceBlockEntity.b.CLEARED, Optional.empty()));
        return testinstanceblockentity;
    }

    public static void clearSpaceForStructure(StructureBoundingBox structureboundingbox, WorldServer worldserver) {
        int i = structureboundingbox.minY() - 1;
        StructureBoundingBox structureboundingbox1 = new StructureBoundingBox(structureboundingbox.minX() - 2, structureboundingbox.minY() - 3, structureboundingbox.minZ() - 3, structureboundingbox.maxX() + 3, structureboundingbox.maxY() + 20, structureboundingbox.maxZ() + 3);

        BlockPosition.betweenClosedStream(structureboundingbox1).forEach((blockposition) -> {
            clearBlock(i, blockposition, worldserver);
        });
        worldserver.getBlockTicks().clearArea(structureboundingbox1);
        worldserver.clearBlockEvents(structureboundingbox1);
        AxisAlignedBB axisalignedbb = AxisAlignedBB.of(structureboundingbox1);
        List<Entity> list = worldserver.<Entity>getEntitiesOfClass(Entity.class, axisalignedbb, (entity) -> {
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

    public static Optional<BlockPosition> findTestContainingPos(BlockPosition blockposition, int i, WorldServer worldserver) {
        return findTestBlocks(blockposition, i, worldserver).filter((blockposition1) -> {
            return doesStructureContain(blockposition1, blockposition, worldserver);
        }).findFirst();
    }

    public static Optional<BlockPosition> findNearestTest(BlockPosition blockposition, int i, WorldServer worldserver) {
        Comparator<BlockPosition> comparator = Comparator.comparingInt((blockposition1) -> {
            return blockposition1.distManhattan(blockposition);
        });

        return findTestBlocks(blockposition, i, worldserver).min(comparator);
    }

    public static Stream<BlockPosition> findTestBlocks(BlockPosition blockposition, int i, WorldServer worldserver) {
        StructureBoundingBox structureboundingbox = getBoundingBoxAtGround(blockposition, i, worldserver);

        return BlockPosition.betweenClosedStream(structureboundingbox).filter((blockposition1) -> {
            return worldserver.getBlockState(blockposition1).is(Blocks.TEST_INSTANCE_BLOCK);
        }).map(BlockPosition::immutable);
    }

    private static StructureBoundingBox getBoundingBoxAtGround(BlockPosition blockposition, int i, WorldServer worldserver) {
        BlockPosition blockposition1 = BlockPosition.containing((double) blockposition.getX(), (double) worldserver.getHeightmapPos(HeightMap.Type.WORLD_SURFACE, blockposition).getY(), (double) blockposition.getZ());

        return (new StructureBoundingBox(blockposition1)).inflatedBy(i, 10, i);
    }

    public static Stream<BlockPosition> lookedAtTestPos(BlockPosition blockposition, Entity entity, WorldServer worldserver) {
        int i = 200;
        Vec3D vec3d = entity.getEyePosition();
        Vec3D vec3d1 = vec3d.add(entity.getLookAngle().scale(200.0D));
        Stream stream = findTestBlocks(blockposition, 200, worldserver).map((blockposition1) -> {
            return worldserver.getBlockEntity(blockposition1, TileEntityTypes.TEST_INSTANCE_BLOCK);
        }).flatMap(Optional::stream).filter((testinstanceblockentity) -> {
            return testinstanceblockentity.getStructureBounds().clip(vec3d, vec3d1).isPresent();
        }).map(TileEntity::getBlockPos);

        Objects.requireNonNull(blockposition);
        return stream.sorted(Comparator.comparing(blockposition::distSqr)).limit(1L);
    }

    private static void clearBlock(int i, BlockPosition blockposition, WorldServer worldserver) {
        IBlockData iblockdata;

        if (blockposition.getY() < i) {
            iblockdata = Blocks.STONE.defaultBlockState();
        } else {
            iblockdata = Blocks.AIR.defaultBlockState();
        }

        ArgumentTileLocation argumenttilelocation = new ArgumentTileLocation(iblockdata, Collections.emptySet(), (NBTTagCompound) null);

        argumenttilelocation.place(worldserver, blockposition, 818);
        worldserver.updateNeighborsAt(blockposition, iblockdata.getBlock());
    }

    private static boolean doesStructureContain(BlockPosition blockposition, BlockPosition blockposition1, WorldServer worldserver) {
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof TestInstanceBlockEntity testinstanceblockentity) {
            return testinstanceblockentity.getStructureBoundingBox().isInside(blockposition1);
        } else {
            return false;
        }
    }
}
