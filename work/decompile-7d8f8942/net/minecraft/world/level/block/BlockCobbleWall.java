package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyWallHeight;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockCobbleWall extends Block implements IBlockWaterlogged {

    public static final MapCodec<BlockCobbleWall> CODEC = simpleCodec(BlockCobbleWall::new);
    public static final BlockStateBoolean UP = BlockProperties.UP;
    public static final BlockStateEnum<BlockPropertyWallHeight> EAST = BlockProperties.EAST_WALL;
    public static final BlockStateEnum<BlockPropertyWallHeight> NORTH = BlockProperties.NORTH_WALL;
    public static final BlockStateEnum<BlockPropertyWallHeight> SOUTH = BlockProperties.SOUTH_WALL;
    public static final BlockStateEnum<BlockPropertyWallHeight> WEST = BlockProperties.WEST_WALL;
    public static final Map<EnumDirection, BlockStateEnum<BlockPropertyWallHeight>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(EnumDirection.NORTH, BlockCobbleWall.NORTH, EnumDirection.EAST, BlockCobbleWall.EAST, EnumDirection.SOUTH, BlockCobbleWall.SOUTH, EnumDirection.WEST, BlockCobbleWall.WEST)));
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private final Function<IBlockData, VoxelShape> shapes;
    private final Function<IBlockData, VoxelShape> collisionShapes;
    private static final VoxelShape TEST_SHAPE_POST = Block.column(2.0D, 0.0D, 16.0D);
    private static final Map<EnumDirection, VoxelShape> TEST_SHAPES_WALL = VoxelShapes.rotateHorizontal(Block.boxZ(2.0D, 16.0D, 0.0D, 9.0D));

    @Override
    public MapCodec<BlockCobbleWall> codec() {
        return BlockCobbleWall.CODEC;
    }

    public BlockCobbleWall(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockCobbleWall.UP, true)).setValue(BlockCobbleWall.NORTH, BlockPropertyWallHeight.NONE)).setValue(BlockCobbleWall.EAST, BlockPropertyWallHeight.NONE)).setValue(BlockCobbleWall.SOUTH, BlockPropertyWallHeight.NONE)).setValue(BlockCobbleWall.WEST, BlockPropertyWallHeight.NONE)).setValue(BlockCobbleWall.WATERLOGGED, false));
        this.shapes = this.makeShapes(16.0F, 14.0F);
        this.collisionShapes = this.makeShapes(24.0F, 24.0F);
    }

    private Function<IBlockData, VoxelShape> makeShapes(float f, float f1) {
        VoxelShape voxelshape = Block.column(8.0D, 0.0D, (double) f);
        int i = 6;
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateHorizontal(Block.boxZ(6.0D, 0.0D, (double) f1, 0.0D, 11.0D));
        Map<EnumDirection, VoxelShape> map1 = VoxelShapes.rotateHorizontal(Block.boxZ(6.0D, 0.0D, (double) f, 0.0D, 11.0D));

        return this.getShapeForEachState((iblockdata) -> {
            VoxelShape voxelshape1 = (Boolean) iblockdata.getValue(BlockCobbleWall.UP) ? voxelshape : VoxelShapes.empty();

            for (Map.Entry<EnumDirection, BlockStateEnum<BlockPropertyWallHeight>> map_entry : BlockCobbleWall.PROPERTY_BY_DIRECTION.entrySet()) {
                VoxelShape voxelshape2;

                switch ((BlockPropertyWallHeight) iblockdata.getValue((IBlockState) map_entry.getValue())) {
                    case NONE:
                        voxelshape2 = VoxelShapes.empty();
                        break;
                    case LOW:
                        voxelshape2 = (VoxelShape) map.get(map_entry.getKey());
                        break;
                    case TALL:
                        voxelshape2 = (VoxelShape) map1.get(map_entry.getKey());
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }

                voxelshape1 = VoxelShapes.or(voxelshape1, voxelshape2);
            }

            return voxelshape1;
        }, new IBlockState[]{BlockCobbleWall.WATERLOGGED});
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.collisionShapes.apply(iblockdata);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    private boolean connectsTo(IBlockData iblockdata, boolean flag, EnumDirection enumdirection) {
        Block block = iblockdata.getBlock();
        boolean flag1 = block instanceof BlockFenceGate && BlockFenceGate.connectsToDirection(iblockdata, enumdirection);

        return iblockdata.is(TagsBlock.WALLS) || !isExceptionForConnection(iblockdata) && flag || block instanceof BlockIronBars || flag1;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IWorldReader iworldreader = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
        BlockPosition blockposition1 = blockposition.north();
        BlockPosition blockposition2 = blockposition.east();
        BlockPosition blockposition3 = blockposition.south();
        BlockPosition blockposition4 = blockposition.west();
        BlockPosition blockposition5 = blockposition.above();
        IBlockData iblockdata = iworldreader.getBlockState(blockposition1);
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition2);
        IBlockData iblockdata2 = iworldreader.getBlockState(blockposition3);
        IBlockData iblockdata3 = iworldreader.getBlockState(blockposition4);
        IBlockData iblockdata4 = iworldreader.getBlockState(blockposition5);
        boolean flag = this.connectsTo(iblockdata, iblockdata.isFaceSturdy(iworldreader, blockposition1, EnumDirection.SOUTH), EnumDirection.SOUTH);
        boolean flag1 = this.connectsTo(iblockdata1, iblockdata1.isFaceSturdy(iworldreader, blockposition2, EnumDirection.WEST), EnumDirection.WEST);
        boolean flag2 = this.connectsTo(iblockdata2, iblockdata2.isFaceSturdy(iworldreader, blockposition3, EnumDirection.NORTH), EnumDirection.NORTH);
        boolean flag3 = this.connectsTo(iblockdata3, iblockdata3.isFaceSturdy(iworldreader, blockposition4, EnumDirection.EAST), EnumDirection.EAST);
        IBlockData iblockdata5 = (IBlockData) this.defaultBlockState().setValue(BlockCobbleWall.WATERLOGGED, fluid.getType() == FluidTypes.WATER);

        return this.updateShape(iworldreader, iblockdata5, blockposition5, iblockdata4, flag, flag1, flag2, flag3);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockCobbleWall.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return enumdirection == EnumDirection.DOWN ? super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource) : (enumdirection == EnumDirection.UP ? this.topUpdate(iworldreader, iblockdata, blockposition1, iblockdata1) : this.sideUpdate(iworldreader, blockposition, iblockdata, blockposition1, iblockdata1, enumdirection));
    }

    private static boolean isConnected(IBlockData iblockdata, IBlockState<BlockPropertyWallHeight> iblockstate) {
        return iblockdata.getValue(iblockstate) != BlockPropertyWallHeight.NONE;
    }

    private static boolean isCovered(VoxelShape voxelshape, VoxelShape voxelshape1) {
        return !VoxelShapes.joinIsNotEmpty(voxelshape1, voxelshape, OperatorBoolean.ONLY_FIRST);
    }

    private IBlockData topUpdate(IWorldReader iworldreader, IBlockData iblockdata, BlockPosition blockposition, IBlockData iblockdata1) {
        boolean flag = isConnected(iblockdata, BlockCobbleWall.NORTH);
        boolean flag1 = isConnected(iblockdata, BlockCobbleWall.EAST);
        boolean flag2 = isConnected(iblockdata, BlockCobbleWall.SOUTH);
        boolean flag3 = isConnected(iblockdata, BlockCobbleWall.WEST);

        return this.updateShape(iworldreader, iblockdata, blockposition, iblockdata1, flag, flag1, flag2, flag3);
    }

    private IBlockData sideUpdate(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, BlockPosition blockposition1, IBlockData iblockdata1, EnumDirection enumdirection) {
        EnumDirection enumdirection1 = enumdirection.getOpposite();
        boolean flag = enumdirection == EnumDirection.NORTH ? this.connectsTo(iblockdata1, iblockdata1.isFaceSturdy(iworldreader, blockposition1, enumdirection1), enumdirection1) : isConnected(iblockdata, BlockCobbleWall.NORTH);
        boolean flag1 = enumdirection == EnumDirection.EAST ? this.connectsTo(iblockdata1, iblockdata1.isFaceSturdy(iworldreader, blockposition1, enumdirection1), enumdirection1) : isConnected(iblockdata, BlockCobbleWall.EAST);
        boolean flag2 = enumdirection == EnumDirection.SOUTH ? this.connectsTo(iblockdata1, iblockdata1.isFaceSturdy(iworldreader, blockposition1, enumdirection1), enumdirection1) : isConnected(iblockdata, BlockCobbleWall.SOUTH);
        boolean flag3 = enumdirection == EnumDirection.WEST ? this.connectsTo(iblockdata1, iblockdata1.isFaceSturdy(iworldreader, blockposition1, enumdirection1), enumdirection1) : isConnected(iblockdata, BlockCobbleWall.WEST);
        BlockPosition blockposition2 = blockposition.above();
        IBlockData iblockdata2 = iworldreader.getBlockState(blockposition2);

        return this.updateShape(iworldreader, iblockdata, blockposition2, iblockdata2, flag, flag1, flag2, flag3);
    }

    private IBlockData updateShape(IWorldReader iworldreader, IBlockData iblockdata, BlockPosition blockposition, IBlockData iblockdata1, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
        VoxelShape voxelshape = iblockdata1.getCollisionShape(iworldreader, blockposition).getFaceShape(EnumDirection.DOWN);
        IBlockData iblockdata2 = this.updateSides(iblockdata, flag, flag1, flag2, flag3, voxelshape);

        return (IBlockData) iblockdata2.setValue(BlockCobbleWall.UP, this.shouldRaisePost(iblockdata2, iblockdata1, voxelshape));
    }

    private boolean shouldRaisePost(IBlockData iblockdata, IBlockData iblockdata1, VoxelShape voxelshape) {
        boolean flag = iblockdata1.getBlock() instanceof BlockCobbleWall && (Boolean) iblockdata1.getValue(BlockCobbleWall.UP);

        if (flag) {
            return true;
        } else {
            BlockPropertyWallHeight blockpropertywallheight = (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.NORTH);
            BlockPropertyWallHeight blockpropertywallheight1 = (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.SOUTH);
            BlockPropertyWallHeight blockpropertywallheight2 = (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.EAST);
            BlockPropertyWallHeight blockpropertywallheight3 = (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.WEST);
            boolean flag1 = blockpropertywallheight1 == BlockPropertyWallHeight.NONE;
            boolean flag2 = blockpropertywallheight3 == BlockPropertyWallHeight.NONE;
            boolean flag3 = blockpropertywallheight2 == BlockPropertyWallHeight.NONE;
            boolean flag4 = blockpropertywallheight == BlockPropertyWallHeight.NONE;
            boolean flag5 = flag4 && flag1 && flag2 && flag3 || flag4 != flag1 || flag2 != flag3;

            if (flag5) {
                return true;
            } else {
                boolean flag6 = blockpropertywallheight == BlockPropertyWallHeight.TALL && blockpropertywallheight1 == BlockPropertyWallHeight.TALL || blockpropertywallheight2 == BlockPropertyWallHeight.TALL && blockpropertywallheight3 == BlockPropertyWallHeight.TALL;

                return flag6 ? false : iblockdata1.is(TagsBlock.WALL_POST_OVERRIDE) || isCovered(voxelshape, BlockCobbleWall.TEST_SHAPE_POST);
            }
        }
    }

    private IBlockData updateSides(IBlockData iblockdata, boolean flag, boolean flag1, boolean flag2, boolean flag3, VoxelShape voxelshape) {
        return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockCobbleWall.NORTH, this.makeWallState(flag, voxelshape, (VoxelShape) BlockCobbleWall.TEST_SHAPES_WALL.get(EnumDirection.NORTH)))).setValue(BlockCobbleWall.EAST, this.makeWallState(flag1, voxelshape, (VoxelShape) BlockCobbleWall.TEST_SHAPES_WALL.get(EnumDirection.EAST)))).setValue(BlockCobbleWall.SOUTH, this.makeWallState(flag2, voxelshape, (VoxelShape) BlockCobbleWall.TEST_SHAPES_WALL.get(EnumDirection.SOUTH)))).setValue(BlockCobbleWall.WEST, this.makeWallState(flag3, voxelshape, (VoxelShape) BlockCobbleWall.TEST_SHAPES_WALL.get(EnumDirection.WEST)));
    }

    private BlockPropertyWallHeight makeWallState(boolean flag, VoxelShape voxelshape, VoxelShape voxelshape1) {
        return flag ? (isCovered(voxelshape, voxelshape1) ? BlockPropertyWallHeight.TALL : BlockPropertyWallHeight.LOW) : BlockPropertyWallHeight.NONE;
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockCobbleWall.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return !(Boolean) iblockdata.getValue(BlockCobbleWall.WATERLOGGED);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockCobbleWall.UP, BlockCobbleWall.NORTH, BlockCobbleWall.EAST, BlockCobbleWall.WEST, BlockCobbleWall.SOUTH, BlockCobbleWall.WATERLOGGED);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (enumblockrotation) {
            case CLOCKWISE_180:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockCobbleWall.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.SOUTH))).setValue(BlockCobbleWall.EAST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.WEST))).setValue(BlockCobbleWall.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.NORTH))).setValue(BlockCobbleWall.WEST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.EAST));
            case COUNTERCLOCKWISE_90:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockCobbleWall.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.EAST))).setValue(BlockCobbleWall.EAST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.SOUTH))).setValue(BlockCobbleWall.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.WEST))).setValue(BlockCobbleWall.WEST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.NORTH));
            case CLOCKWISE_90:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockCobbleWall.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.WEST))).setValue(BlockCobbleWall.EAST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.NORTH))).setValue(BlockCobbleWall.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.EAST))).setValue(BlockCobbleWall.WEST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.SOUTH));
            default:
                return iblockdata;
        }
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        switch (enumblockmirror) {
            case LEFT_RIGHT:
                return (IBlockData) ((IBlockData) iblockdata.setValue(BlockCobbleWall.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.SOUTH))).setValue(BlockCobbleWall.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.NORTH));
            case FRONT_BACK:
                return (IBlockData) ((IBlockData) iblockdata.setValue(BlockCobbleWall.EAST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.WEST))).setValue(BlockCobbleWall.WEST, (BlockPropertyWallHeight) iblockdata.getValue(BlockCobbleWall.EAST));
            default:
                return super.mirror(iblockdata, enumblockmirror);
        }
    }
}
