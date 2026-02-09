package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class MultifaceBlock extends Block implements IBlockWaterlogged {

    public static final MapCodec<MultifaceBlock> CODEC = simpleCodec(MultifaceBlock::new);
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final Map<EnumDirection, BlockStateBoolean> PROPERTY_BY_DIRECTION = BlockSprawling.PROPERTY_BY_DIRECTION;
    protected static final EnumDirection[] DIRECTIONS = EnumDirection.values();
    private final Function<IBlockData, VoxelShape> shapes;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    @Override
    protected MapCodec<? extends MultifaceBlock> codec() {
        return MultifaceBlock.CODEC;
    }

    public MultifaceBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
        this.shapes = this.makeShapes();
        this.canRotate = EnumDirection.EnumDirectionLimit.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = EnumDirection.EnumDirectionLimit.HORIZONTAL.stream().filter(EnumDirection.EnumAxis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = EnumDirection.EnumDirectionLimit.HORIZONTAL.stream().filter(EnumDirection.EnumAxis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    private Function<IBlockData, VoxelShape> makeShapes() {
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateAll(Block.boxZ(16.0D, 0.0D, 1.0D));

        return this.getShapeForEachState((iblockdata) -> {
            VoxelShape voxelshape = VoxelShapes.empty();

            for (EnumDirection enumdirection : MultifaceBlock.DIRECTIONS) {
                if (hasFace(iblockdata, enumdirection)) {
                    voxelshape = VoxelShapes.or(voxelshape, (VoxelShape) map.get(enumdirection));
                }
            }

            return voxelshape.isEmpty() ? VoxelShapes.block() : voxelshape;
        }, new IBlockState[]{MultifaceBlock.WATERLOGGED});
    }

    public static Set<EnumDirection> availableFaces(IBlockData iblockdata) {
        if (!(iblockdata.getBlock() instanceof MultifaceBlock)) {
            return Set.of();
        } else {
            Set<EnumDirection> set = EnumSet.noneOf(EnumDirection.class);

            for (EnumDirection enumdirection : EnumDirection.values()) {
                if (hasFace(iblockdata, enumdirection)) {
                    set.add(enumdirection);
                }
            }

            return set;
        }
    }

    public static Set<EnumDirection> unpack(byte b0) {
        Set<EnumDirection> set = EnumSet.noneOf(EnumDirection.class);

        for (EnumDirection enumdirection : EnumDirection.values()) {
            if ((b0 & (byte) (1 << enumdirection.ordinal())) > 0) {
                set.add(enumdirection);
            }
        }

        return set;
    }

    public static byte pack(Collection<EnumDirection> collection) {
        byte b0 = 0;

        for (EnumDirection enumdirection : collection) {
            b0 = (byte) (b0 | 1 << enumdirection.ordinal());
        }

        return b0;
    }

    protected boolean isFaceSupported(EnumDirection enumdirection) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        for (EnumDirection enumdirection : MultifaceBlock.DIRECTIONS) {
            if (this.isFaceSupported(enumdirection)) {
                blockstatelist_a.add(getFaceProperty(enumdirection));
            }
        }

        blockstatelist_a.add(MultifaceBlock.WATERLOGGED);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(MultifaceBlock.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return !hasAnyFace(iblockdata) ? Blocks.AIR.defaultBlockState() : (hasFace(iblockdata, enumdirection) && !canAttachTo(iworldreader, enumdirection, blockposition1, iblockdata1) ? removeFace(iblockdata, getFaceProperty(enumdirection)) : iblockdata);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(MultifaceBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        boolean flag = false;

        for (EnumDirection enumdirection : MultifaceBlock.DIRECTIONS) {
            if (hasFace(iblockdata, enumdirection)) {
                if (!canAttachTo(iworldreader, blockposition, enumdirection)) {
                    return false;
                }

                flag = true;
            }
        }

        return flag;
    }

    @Override
    protected boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return !blockactioncontext.getItemInHand().is(this.asItem()) || hasAnyVacantFace(iblockdata);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        return (IBlockData) Arrays.stream(blockactioncontext.getNearestLookingDirections()).map((enumdirection) -> {
            return this.getStateForPlacement(iblockdata, world, blockposition, enumdirection);
        }).filter(Objects::nonNull).findFirst().orElse((Object) null);
    }

    public boolean isValidStateForPlacement(IBlockAccess iblockaccess, IBlockData iblockdata, BlockPosition blockposition, EnumDirection enumdirection) {
        if (this.isFaceSupported(enumdirection) && (!iblockdata.is(this) || !hasFace(iblockdata, enumdirection))) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            return canAttachTo(iblockaccess, enumdirection, blockposition1, iblockaccess.getBlockState(blockposition1));
        } else {
            return false;
        }
    }

    @Nullable
    public IBlockData getStateForPlacement(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        if (!this.isValidStateForPlacement(iblockaccess, iblockdata, blockposition, enumdirection)) {
            return null;
        } else {
            IBlockData iblockdata1;

            if (iblockdata.is(this)) {
                iblockdata1 = iblockdata;
            } else if (iblockdata.getFluidState().isSourceOfType(FluidTypes.WATER)) {
                iblockdata1 = (IBlockData) this.defaultBlockState().setValue(BlockProperties.WATERLOGGED, true);
            } else {
                iblockdata1 = this.defaultBlockState();
            }

            return (IBlockData) iblockdata1.setValue(getFaceProperty(enumdirection), true);
        }
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        if (!this.canRotate) {
            return iblockdata;
        } else {
            Objects.requireNonNull(enumblockrotation);
            return this.mapDirections(iblockdata, enumblockrotation::rotate);
        }
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        if (enumblockmirror == EnumBlockMirror.FRONT_BACK && !this.canMirrorX) {
            return iblockdata;
        } else if (enumblockmirror == EnumBlockMirror.LEFT_RIGHT && !this.canMirrorZ) {
            return iblockdata;
        } else {
            Objects.requireNonNull(enumblockmirror);
            return this.mapDirections(iblockdata, enumblockmirror::mirror);
        }
    }

    private IBlockData mapDirections(IBlockData iblockdata, Function<EnumDirection, EnumDirection> function) {
        IBlockData iblockdata1 = iblockdata;

        for (EnumDirection enumdirection : MultifaceBlock.DIRECTIONS) {
            if (this.isFaceSupported(enumdirection)) {
                iblockdata1 = (IBlockData) iblockdata1.setValue(getFaceProperty((EnumDirection) function.apply(enumdirection)), (Boolean) iblockdata.getValue(getFaceProperty(enumdirection)));
            }
        }

        return iblockdata1;
    }

    public static boolean hasFace(IBlockData iblockdata, EnumDirection enumdirection) {
        BlockStateBoolean blockstateboolean = getFaceProperty(enumdirection);

        return (Boolean) iblockdata.getValueOrElse(blockstateboolean, false);
    }

    public static boolean canAttachTo(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = blockposition.relative(enumdirection);
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition1);

        return canAttachTo(iblockaccess, enumdirection, blockposition1, iblockdata);
    }

    public static boolean canAttachTo(IBlockAccess iblockaccess, EnumDirection enumdirection, BlockPosition blockposition, IBlockData iblockdata) {
        return Block.isFaceFull(iblockdata.getBlockSupportShape(iblockaccess, blockposition), enumdirection.getOpposite()) || Block.isFaceFull(iblockdata.getCollisionShape(iblockaccess, blockposition), enumdirection.getOpposite());
    }

    private static IBlockData removeFace(IBlockData iblockdata, BlockStateBoolean blockstateboolean) {
        IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(blockstateboolean, false);

        return hasAnyFace(iblockdata1) ? iblockdata1 : Blocks.AIR.defaultBlockState();
    }

    public static BlockStateBoolean getFaceProperty(EnumDirection enumdirection) {
        return (BlockStateBoolean) MultifaceBlock.PROPERTY_BY_DIRECTION.get(enumdirection);
    }

    private static IBlockData getDefaultMultifaceState(BlockStateList<Block, IBlockData> blockstatelist) {
        IBlockData iblockdata = (IBlockData) (blockstatelist.any()).setValue(MultifaceBlock.WATERLOGGED, false);

        for (BlockStateBoolean blockstateboolean : MultifaceBlock.PROPERTY_BY_DIRECTION.values()) {
            iblockdata = (IBlockData) iblockdata.trySetValue(blockstateboolean, false);
        }

        return iblockdata;
    }

    protected static boolean hasAnyFace(IBlockData iblockdata) {
        for (EnumDirection enumdirection : MultifaceBlock.DIRECTIONS) {
            if (hasFace(iblockdata, enumdirection)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasAnyVacantFace(IBlockData iblockdata) {
        for (EnumDirection enumdirection : MultifaceBlock.DIRECTIONS) {
            if (!hasFace(iblockdata, enumdirection)) {
                return true;
            }
        }

        return false;
    }
}
