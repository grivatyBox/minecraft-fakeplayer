package net.minecraft.world.level.block;

import com.mojang.math.PointGroupO;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.TileInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerGrindstone;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyAttachPosition;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockGrindstone extends BlockAttachable {

    public static final MapCodec<BlockGrindstone> CODEC = simpleCodec(BlockGrindstone::new);
    private static final IChatBaseComponent CONTAINER_TITLE = IChatBaseComponent.translatable("container.grindstone_title");
    private final Function<IBlockData, VoxelShape> shapes;

    @Override
    public MapCodec<BlockGrindstone> codec() {
        return BlockGrindstone.CODEC;
    }

    protected BlockGrindstone(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockGrindstone.FACING, EnumDirection.NORTH)).setValue(BlockGrindstone.FACE, BlockPropertyAttachPosition.WALL));
        this.shapes = this.makeShapes();
    }

    private Function<IBlockData, VoxelShape> makeShapes() {
        VoxelShape voxelshape = VoxelShapes.or(Block.box(2.0D, 6.0D, 7.0D, 4.0D, 10.0D, 16.0D), Block.box(2.0D, 5.0D, 3.0D, 4.0D, 11.0D, 9.0D));
        VoxelShape voxelshape1 = VoxelShapes.rotate(voxelshape, PointGroupO.INVERT_X);
        VoxelShape voxelshape2 = VoxelShapes.or(Block.boxZ(8.0D, 2.0D, 14.0D, 0.0D, 12.0D), voxelshape, voxelshape1);
        Map<BlockPropertyAttachPosition, Map<EnumDirection, VoxelShape>> map = VoxelShapes.rotateAttachFace(voxelshape2);

        return this.getShapeForEachState((iblockdata) -> {
            return (VoxelShape) ((Map) map.get(iblockdata.getValue(BlockGrindstone.FACE))).get(iblockdata.getValue(BlockGrindstone.FACING));
        });
    }

    private VoxelShape getVoxelShape(IBlockData iblockdata) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return this.getVoxelShape(iblockdata);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return this.getVoxelShape(iblockdata);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return true;
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!world.isClientSide) {
            entityhuman.openMenu(iblockdata.getMenuProvider(world, blockposition));
            entityhuman.awardStat(StatisticList.INTERACT_WITH_GRINDSTONE);
        }

        return EnumInteractionResult.SUCCESS;
    }

    @Override
    protected ITileInventory getMenuProvider(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return new TileInventory((i, playerinventory, entityhuman) -> {
            return new ContainerGrindstone(i, playerinventory, ContainerAccess.create(world, blockposition));
        }, BlockGrindstone.CONTAINER_TITLE);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockGrindstone.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockGrindstone.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockGrindstone.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockGrindstone.FACING, BlockGrindstone.FACE);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
