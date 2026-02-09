package net.minecraft.world.level.block.piston;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockDirectional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyPistonType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockPistonExtension extends BlockDirectional {

    public static final MapCodec<BlockPistonExtension> CODEC = simpleCodec(BlockPistonExtension::new);
    public static final BlockStateEnum<BlockPropertyPistonType> TYPE = BlockProperties.PISTON_TYPE;
    public static final BlockStateBoolean SHORT = BlockProperties.SHORT;
    public static final int PLATFORM_THICKNESS = 4;
    private static final VoxelShape SHAPE_PLATFORM = Block.boxZ(16.0D, 0.0D, 4.0D);
    private static final Map<EnumDirection, VoxelShape> SHAPES_SHORT = VoxelShapes.rotateAll(VoxelShapes.or(BlockPistonExtension.SHAPE_PLATFORM, Block.boxZ(4.0D, 4.0D, 16.0D)));
    private static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateAll(VoxelShapes.or(BlockPistonExtension.SHAPE_PLATFORM, Block.boxZ(4.0D, 4.0D, 20.0D)));

    @Override
    protected MapCodec<BlockPistonExtension> codec() {
        return BlockPistonExtension.CODEC;
    }

    public BlockPistonExtension(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockPistonExtension.FACING, EnumDirection.NORTH)).setValue(BlockPistonExtension.TYPE, BlockPropertyPistonType.DEFAULT)).setValue(BlockPistonExtension.SHORT, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) ((Boolean) iblockdata.getValue(BlockPistonExtension.SHORT) ? BlockPistonExtension.SHAPES_SHORT : BlockPistonExtension.SHAPES).get(iblockdata.getValue(BlockPistonExtension.FACING));
    }

    private boolean isFittingBase(IBlockData iblockdata, IBlockData iblockdata1) {
        Block block = iblockdata.getValue(BlockPistonExtension.TYPE) == BlockPropertyPistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;

        return iblockdata1.is(block) && (Boolean) iblockdata1.getValue(BlockPiston.EXTENDED) && iblockdata1.getValue(BlockPistonExtension.FACING) == iblockdata.getValue(BlockPistonExtension.FACING);
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!world.isClientSide && entityhuman.preventsBlockDrops()) {
            BlockPosition blockposition1 = blockposition.relative(((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)).getOpposite());

            if (this.isFittingBase(iblockdata, world.getBlockState(blockposition1))) {
                world.destroyBlock(blockposition1, false);
            }
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        BlockPosition blockposition1 = blockposition.relative(((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)).getOpposite());

        if (this.isFittingBase(iblockdata, worldserver.getBlockState(blockposition1))) {
            worldserver.destroyBlock(blockposition1, true);
        }

    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection.getOpposite() == iblockdata.getValue(BlockPistonExtension.FACING) && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.relative(((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)).getOpposite()));

        return this.isFittingBase(iblockdata, iblockdata1) || iblockdata1.is(Blocks.MOVING_PISTON) && iblockdata1.getValue(BlockPistonExtension.FACING) == iblockdata.getValue(BlockPistonExtension.FACING);
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (iblockdata.canSurvive(world, blockposition)) {
            world.neighborChanged(blockposition.relative(((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)).getOpposite()), block, ExperimentalRedstoneUtils.withFront(orientation, ((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)).getOpposite()));
        }

    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return new ItemStack(iblockdata.getValue(BlockPistonExtension.TYPE) == BlockPropertyPistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockPistonExtension.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockPistonExtension.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPistonExtension.FACING, BlockPistonExtension.TYPE, BlockPistonExtension.SHORT);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
