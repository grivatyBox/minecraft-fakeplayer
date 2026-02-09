package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.monster.EntityRavager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyDoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class PitcherCropBlock extends BlockTallPlant implements IBlockFragilePlantElement {

    public static final MapCodec<PitcherCropBlock> CODEC = simpleCodec(PitcherCropBlock::new);
    public static final int MAX_AGE = 4;
    public static final BlockStateInteger AGE = BlockProperties.AGE_4;
    public static final BlockStateEnum<BlockPropertyDoubleBlockHalf> HALF = BlockTallPlant.HALF;
    private static final int DOUBLE_PLANT_AGE_INTERSECTION = 3;
    private static final int BONEMEAL_INCREASE = 1;
    private static final VoxelShape SHAPE_BULB = Block.column(6.0D, -1.0D, 3.0D);
    private static final VoxelShape SHAPE_CROP = Block.column(10.0D, -1.0D, 5.0D);
    private final Function<IBlockData, VoxelShape> shapes = this.makeShapes();

    @Override
    public MapCodec<PitcherCropBlock> codec() {
        return PitcherCropBlock.CODEC;
    }

    public PitcherCropBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    private Function<IBlockData, VoxelShape> makeShapes() {
        int[] aint = new int[]{0, 9, 11, 22, 26};

        return this.getShapeForEachState((iblockdata) -> {
            int i = ((Integer) iblockdata.getValue(PitcherCropBlock.AGE) == 0 ? 4 : 6) + aint[(Integer) iblockdata.getValue(PitcherCropBlock.AGE)];
            int j = (Integer) iblockdata.getValue(PitcherCropBlock.AGE) == 0 ? 6 : 10;
            VoxelShape voxelshape;

            switch ((BlockPropertyDoubleBlockHalf) iblockdata.getValue(PitcherCropBlock.HALF)) {
                case LOWER:
                    voxelshape = Block.column((double) j, -1.0D, (double) Math.min(16, -1 + i));
                    break;
                case UPPER:
                    voxelshape = Block.column((double) j, 0.0D, (double) Math.max(0, -1 + i - 16));
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return voxelshape;
        });
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    public VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.LOWER ? ((Integer) iblockdata.getValue(PitcherCropBlock.AGE) == 0 ? PitcherCropBlock.SHAPE_BULB : PitcherCropBlock.SHAPE_CROP) : VoxelShapes.empty();
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return isDouble((Integer) iblockdata.getValue(PitcherCropBlock.AGE)) ? super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource) : (iblockdata.canSurvive(iworldreader, blockposition) ? iblockdata : Blocks.AIR.defaultBlockState());
    }

    @Override
    public boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return isLower(iblockdata) && !sufficientLight(iworldreader, blockposition) ? false : super.canSurvive(iblockdata, iworldreader, blockposition);
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(Blocks.FARMLAND);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(PitcherCropBlock.AGE);
        super.createBlockStateDefinition(blockstatelist_a);
    }

    @Override
    public void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity, InsideBlockEffectApplier insideblockeffectapplier) {
        if (world instanceof WorldServer worldserver) {
            if (entity instanceof EntityRavager && worldserver.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                worldserver.destroyBlock(blockposition, true, entity);
            }
        }

    }

    @Override
    public boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return false;
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityLiving entityliving, ItemStack itemstack) {}

    @Override
    public boolean isRandomlyTicking(IBlockData iblockdata) {
        return iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.LOWER && !this.isMaxAge(iblockdata);
    }

    @Override
    public void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        float f = BlockCrops.getGrowthSpeed(this, worldserver, blockposition);
        boolean flag = randomsource.nextInt((int) (25.0F / f) + 1) == 0;

        if (flag) {
            this.grow(worldserver, iblockdata, blockposition, 1);
        }

    }

    private void grow(WorldServer worldserver, IBlockData iblockdata, BlockPosition blockposition, int i) {
        int j = Math.min((Integer) iblockdata.getValue(PitcherCropBlock.AGE) + i, 4);

        if (this.canGrow(worldserver, blockposition, iblockdata, j)) {
            IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(PitcherCropBlock.AGE, j);

            worldserver.setBlock(blockposition, iblockdata1, 2);
            if (isDouble(j)) {
                worldserver.setBlock(blockposition.above(), (IBlockData) iblockdata1.setValue(PitcherCropBlock.HALF, BlockPropertyDoubleBlockHalf.UPPER), 3);
            }

        }
    }

    private static boolean canGrowInto(IWorldReader iworldreader, BlockPosition blockposition) {
        IBlockData iblockdata = iworldreader.getBlockState(blockposition);

        return iblockdata.isAir() || iblockdata.is(Blocks.PITCHER_CROP);
    }

    private static boolean sufficientLight(IWorldReader iworldreader, BlockPosition blockposition) {
        return BlockCrops.hasSufficientLight(iworldreader, blockposition);
    }

    private static boolean isLower(IBlockData iblockdata) {
        return iblockdata.is(Blocks.PITCHER_CROP) && iblockdata.getValue(PitcherCropBlock.HALF) == BlockPropertyDoubleBlockHalf.LOWER;
    }

    private static boolean isDouble(int i) {
        return i >= 3;
    }

    private boolean canGrow(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, int i) {
        return !this.isMaxAge(iblockdata) && sufficientLight(iworldreader, blockposition) && (!isDouble(i) || canGrowInto(iworldreader, blockposition.above()));
    }

    private boolean isMaxAge(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(PitcherCropBlock.AGE) >= 4;
    }

    @Nullable
    private PitcherCropBlock.a getLowerHalf(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        if (isLower(iblockdata)) {
            return new PitcherCropBlock.a(blockposition, iblockdata);
        } else {
            BlockPosition blockposition1 = blockposition.below();
            IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);

            return isLower(iblockdata1) ? new PitcherCropBlock.a(blockposition1, iblockdata1) : null;
        }
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        PitcherCropBlock.a pitchercropblock_a = this.getLowerHalf(iworldreader, blockposition, iblockdata);

        return pitchercropblock_a == null ? false : this.canGrow(iworldreader, pitchercropblock_a.pos, pitchercropblock_a.state, (Integer) pitchercropblock_a.state.getValue(PitcherCropBlock.AGE) + 1);
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        PitcherCropBlock.a pitchercropblock_a = this.getLowerHalf(worldserver, blockposition, iblockdata);

        if (pitchercropblock_a != null) {
            this.grow(worldserver, pitchercropblock_a.state, pitchercropblock_a.pos, 1);
        }
    }

    private static record a(BlockPosition pos, IBlockData state) {

    }
}
