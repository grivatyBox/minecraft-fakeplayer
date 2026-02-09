package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyWallHeight;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class MossyCarpetBlock extends Block implements IBlockFragilePlantElement {

    public static final MapCodec<MossyCarpetBlock> CODEC = simpleCodec(MossyCarpetBlock::new);
    public static final BlockStateBoolean BASE = BlockProperties.BOTTOM;
    public static final BlockStateEnum<BlockPropertyWallHeight> NORTH = BlockProperties.NORTH_WALL;
    public static final BlockStateEnum<BlockPropertyWallHeight> EAST = BlockProperties.EAST_WALL;
    public static final BlockStateEnum<BlockPropertyWallHeight> SOUTH = BlockProperties.SOUTH_WALL;
    public static final BlockStateEnum<BlockPropertyWallHeight> WEST = BlockProperties.WEST_WALL;
    public static final Map<EnumDirection, BlockStateEnum<BlockPropertyWallHeight>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(EnumDirection.NORTH, MossyCarpetBlock.NORTH, EnumDirection.EAST, MossyCarpetBlock.EAST, EnumDirection.SOUTH, MossyCarpetBlock.SOUTH, EnumDirection.WEST, MossyCarpetBlock.WEST)));
    private final Function<IBlockData, VoxelShape> shapes;

    @Override
    public MapCodec<MossyCarpetBlock> codec() {
        return MossyCarpetBlock.CODEC;
    }

    public MossyCarpetBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(MossyCarpetBlock.BASE, true)).setValue(MossyCarpetBlock.NORTH, BlockPropertyWallHeight.NONE)).setValue(MossyCarpetBlock.EAST, BlockPropertyWallHeight.NONE)).setValue(MossyCarpetBlock.SOUTH, BlockPropertyWallHeight.NONE)).setValue(MossyCarpetBlock.WEST, BlockPropertyWallHeight.NONE));
        this.shapes = this.makeShapes();
    }

    @Override
    protected VoxelShape getOcclusionShape(IBlockData iblockdata) {
        return VoxelShapes.empty();
    }

    public Function<IBlockData, VoxelShape> makeShapes() {
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateHorizontal(Block.boxZ(16.0D, 0.0D, 10.0D, 0.0D, 1.0D));
        Map<EnumDirection, VoxelShape> map1 = VoxelShapes.rotateAll(Block.boxZ(16.0D, 0.0D, 1.0D));

        return this.getShapeForEachState((iblockdata) -> {
            VoxelShape voxelshape = (Boolean) iblockdata.getValue(MossyCarpetBlock.BASE) ? (VoxelShape) map1.get(EnumDirection.DOWN) : VoxelShapes.empty();

            for (Map.Entry<EnumDirection, BlockStateEnum<BlockPropertyWallHeight>> map_entry : MossyCarpetBlock.PROPERTY_BY_DIRECTION.entrySet()) {
                switch ((BlockPropertyWallHeight) iblockdata.getValue((IBlockState) map_entry.getValue())) {
                    case NONE:
                    default:
                        break;
                    case LOW:
                        voxelshape = VoxelShapes.or(voxelshape, (VoxelShape) map.get(map_entry.getKey()));
                        break;
                    case TALL:
                        voxelshape = VoxelShapes.or(voxelshape, (VoxelShape) map1.get(map_entry.getKey()));
                }
            }

            return voxelshape.isEmpty() ? VoxelShapes.block() : voxelshape;
        });
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (Boolean) iblockdata.getValue(MossyCarpetBlock.BASE) ? (VoxelShape) this.shapes.apply(this.defaultBlockState()) : VoxelShapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.below());

        return (Boolean) iblockdata.getValue(MossyCarpetBlock.BASE) ? !iblockdata1.isAir() : iblockdata1.is(this) && (Boolean) iblockdata1.getValue(MossyCarpetBlock.BASE);
    }

    private static boolean hasFaces(IBlockData iblockdata) {
        if ((Boolean) iblockdata.getValue(MossyCarpetBlock.BASE)) {
            return true;
        } else {
            for (BlockStateEnum<BlockPropertyWallHeight> blockstateenum : MossyCarpetBlock.PROPERTY_BY_DIRECTION.values()) {
                if (iblockdata.getValue(blockstateenum) != BlockPropertyWallHeight.NONE) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean canSupportAtFace(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return enumdirection == EnumDirection.UP ? false : MultifaceBlock.canAttachTo(iblockaccess, blockposition, enumdirection);
    }

    private static IBlockData getUpdatedState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, boolean flag) {
        IBlockData iblockdata1 = null;
        IBlockData iblockdata2 = null;

        flag |= (Boolean) iblockdata.getValue(MossyCarpetBlock.BASE);

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            BlockStateEnum<BlockPropertyWallHeight> blockstateenum = getPropertyForFace(enumdirection);
            BlockPropertyWallHeight blockpropertywallheight = canSupportAtFace(iblockaccess, blockposition, enumdirection) ? (flag ? BlockPropertyWallHeight.LOW : (BlockPropertyWallHeight) iblockdata.getValue(blockstateenum)) : BlockPropertyWallHeight.NONE;

            if (blockpropertywallheight == BlockPropertyWallHeight.LOW) {
                if (iblockdata1 == null) {
                    iblockdata1 = iblockaccess.getBlockState(blockposition.above());
                }

                if (iblockdata1.is(Blocks.PALE_MOSS_CARPET) && iblockdata1.getValue(blockstateenum) != BlockPropertyWallHeight.NONE && !(Boolean) iblockdata1.getValue(MossyCarpetBlock.BASE)) {
                    blockpropertywallheight = BlockPropertyWallHeight.TALL;
                }

                if (!(Boolean) iblockdata.getValue(MossyCarpetBlock.BASE)) {
                    if (iblockdata2 == null) {
                        iblockdata2 = iblockaccess.getBlockState(blockposition.below());
                    }

                    if (iblockdata2.is(Blocks.PALE_MOSS_CARPET) && iblockdata2.getValue(blockstateenum) == BlockPropertyWallHeight.NONE) {
                        blockpropertywallheight = BlockPropertyWallHeight.NONE;
                    }
                }
            }

            iblockdata = (IBlockData) iblockdata.setValue(blockstateenum, blockpropertywallheight);
        }

        return iblockdata;
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return getUpdatedState(this.defaultBlockState(), blockactioncontext.getLevel(), blockactioncontext.getClickedPos(), true);
    }

    public static void placeAt(GeneratorAccess generatoraccess, BlockPosition blockposition, RandomSource randomsource, int i) {
        IBlockData iblockdata = Blocks.PALE_MOSS_CARPET.defaultBlockState();
        IBlockData iblockdata1 = getUpdatedState(iblockdata, generatoraccess, blockposition, true);

        generatoraccess.setBlock(blockposition, iblockdata1, i);
        Objects.requireNonNull(randomsource);
        IBlockData iblockdata2 = createTopperWithSideChance(generatoraccess, blockposition, randomsource::nextBoolean);

        if (!iblockdata2.isAir()) {
            generatoraccess.setBlock(blockposition.above(), iblockdata2, i);
            IBlockData iblockdata3 = getUpdatedState(iblockdata1, generatoraccess, blockposition, true);

            generatoraccess.setBlock(blockposition, iblockdata3, i);
        }

    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        if (!world.isClientSide) {
            RandomSource randomsource = world.getRandom();

            Objects.requireNonNull(randomsource);
            IBlockData iblockdata1 = createTopperWithSideChance(world, blockposition, randomsource::nextBoolean);

            if (!iblockdata1.isAir()) {
                world.setBlock(blockposition.above(), iblockdata1, 3);
            }

        }
    }

    private static IBlockData createTopperWithSideChance(IBlockAccess iblockaccess, BlockPosition blockposition, BooleanSupplier booleansupplier) {
        BlockPosition blockposition1 = blockposition.above();
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition1);
        boolean flag = iblockdata.is(Blocks.PALE_MOSS_CARPET);

        if ((!flag || !(Boolean) iblockdata.getValue(MossyCarpetBlock.BASE)) && (flag || iblockdata.canBeReplaced())) {
            IBlockData iblockdata1 = (IBlockData) Blocks.PALE_MOSS_CARPET.defaultBlockState().setValue(MossyCarpetBlock.BASE, false);
            IBlockData iblockdata2 = getUpdatedState(iblockdata1, iblockaccess, blockposition.above(), true);

            for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                BlockStateEnum<BlockPropertyWallHeight> blockstateenum = getPropertyForFace(enumdirection);

                if (iblockdata2.getValue(blockstateenum) != BlockPropertyWallHeight.NONE && !booleansupplier.getAsBoolean()) {
                    iblockdata2 = (IBlockData) iblockdata2.setValue(blockstateenum, BlockPropertyWallHeight.NONE);
                }
            }

            if (hasFaces(iblockdata2) && iblockdata2 != iblockdata) {
                return iblockdata2;
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (!iblockdata.canSurvive(iworldreader, blockposition)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            IBlockData iblockdata2 = getUpdatedState(iblockdata, iworldreader, blockposition, false);

            return !hasFaces(iblockdata2) ? Blocks.AIR.defaultBlockState() : iblockdata2;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(MossyCarpetBlock.BASE, MossyCarpetBlock.NORTH, MossyCarpetBlock.EAST, MossyCarpetBlock.SOUTH, MossyCarpetBlock.WEST);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        IBlockData iblockdata1;

        switch (enumblockrotation) {
            case CLOCKWISE_180:
                iblockdata1 = (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(MossyCarpetBlock.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.SOUTH))).setValue(MossyCarpetBlock.EAST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.WEST))).setValue(MossyCarpetBlock.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.NORTH))).setValue(MossyCarpetBlock.WEST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.EAST));
                break;
            case COUNTERCLOCKWISE_90:
                iblockdata1 = (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(MossyCarpetBlock.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.EAST))).setValue(MossyCarpetBlock.EAST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.SOUTH))).setValue(MossyCarpetBlock.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.WEST))).setValue(MossyCarpetBlock.WEST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.NORTH));
                break;
            case CLOCKWISE_90:
                iblockdata1 = (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(MossyCarpetBlock.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.WEST))).setValue(MossyCarpetBlock.EAST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.NORTH))).setValue(MossyCarpetBlock.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.EAST))).setValue(MossyCarpetBlock.WEST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.SOUTH));
                break;
            default:
                iblockdata1 = iblockdata;
        }

        return iblockdata1;
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        IBlockData iblockdata1;

        switch (enumblockmirror) {
            case LEFT_RIGHT:
                iblockdata1 = (IBlockData) ((IBlockData) iblockdata.setValue(MossyCarpetBlock.NORTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.SOUTH))).setValue(MossyCarpetBlock.SOUTH, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.NORTH));
                break;
            case FRONT_BACK:
                iblockdata1 = (IBlockData) ((IBlockData) iblockdata.setValue(MossyCarpetBlock.EAST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.WEST))).setValue(MossyCarpetBlock.WEST, (BlockPropertyWallHeight) iblockdata.getValue(MossyCarpetBlock.EAST));
                break;
            default:
                iblockdata1 = super.mirror(iblockdata, enumblockmirror);
        }

        return iblockdata1;
    }

    @Nullable
    public static BlockStateEnum<BlockPropertyWallHeight> getPropertyForFace(EnumDirection enumdirection) {
        return (BlockStateEnum) MossyCarpetBlock.PROPERTY_BY_DIRECTION.get(enumdirection);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(MossyCarpetBlock.BASE) && !createTopperWithSideChance(iworldreader, blockposition, () -> {
            return true;
        }).isAir();
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1 = createTopperWithSideChance(worldserver, blockposition, () -> {
            return true;
        });

        if (!iblockdata1.isAir()) {
            worldserver.setBlock(blockposition.above(), iblockdata1, 3);
        }

    }
}
