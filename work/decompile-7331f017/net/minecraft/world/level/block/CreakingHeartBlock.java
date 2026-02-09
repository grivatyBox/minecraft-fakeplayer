package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;

public class CreakingHeartBlock extends BlockTileEntity {

    public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);
    public static final BlockStateEnum<EnumDirection.EnumAxis> AXIS = BlockProperties.AXIS;
    public static final BlockStateEnum<CreakingHeartBlock.a> CREAKING = BlockProperties.CREAKING;

    @Override
    public MapCodec<CreakingHeartBlock> codec() {
        return CreakingHeartBlock.CODEC;
    }

    protected CreakingHeartBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.defaultBlockState().setValue(CreakingHeartBlock.AXIS, EnumDirection.EnumAxis.Y)).setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.DISABLED));
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new CreakingHeartBlockEntity(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : (iblockdata.getValue(CreakingHeartBlock.CREAKING) != CreakingHeartBlock.a.DISABLED ? createTickerHelper(tileentitytypes, TileEntityTypes.CREAKING_HEART, CreakingHeartBlockEntity::serverTick) : null);
    }

    public static boolean canSummonCreaking(World world) {
        return world.dimensionType().natural() && world.isNight();
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (canSummonCreaking(world)) {
            if (iblockdata.getValue(CreakingHeartBlock.CREAKING) != CreakingHeartBlock.a.DISABLED) {
                if (randomsource.nextInt(16) == 0 && isSurroundedByLogs(world, blockposition)) {
                    world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.CREAKING_HEART_IDLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                }

            }
        }
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        IBlockData iblockdata2 = super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);

        return updateState(iblockdata2, iworldreader, blockposition);
    }

    private static IBlockData updateState(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        boolean flag = hasRequiredLogs(iblockdata, iworldreader, blockposition);
        CreakingHeartBlock.a creakingheartblock_a = (CreakingHeartBlock.a) iblockdata.getValue(CreakingHeartBlock.CREAKING);

        return flag && creakingheartblock_a == CreakingHeartBlock.a.DISABLED ? (IBlockData) iblockdata.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.DORMANT) : iblockdata;
    }

    public static boolean hasRequiredLogs(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection.EnumAxis enumdirection_enumaxis = (EnumDirection.EnumAxis) iblockdata.getValue(CreakingHeartBlock.AXIS);
        EnumDirection[] aenumdirection = enumdirection_enumaxis.getDirections();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];
            IBlockData iblockdata1 = iworldreader.getBlockState(blockposition.relative(enumdirection));

            if (!iblockdata1.is(TagsBlock.PALE_OAK_LOGS) || iblockdata1.getValue(CreakingHeartBlock.AXIS) != enumdirection_enumaxis) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSurroundedByLogs(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            IBlockData iblockdata = generatoraccess.getBlockState(blockposition1);

            if (!iblockdata.is(TagsBlock.PALE_OAK_LOGS)) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return updateState((IBlockData) this.defaultBlockState().setValue(CreakingHeartBlock.AXIS, blockactioncontext.getClickedFace().getAxis()), blockactioncontext.getLevel(), blockactioncontext.getClickedPos());
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return BlockRotatable.rotatePillar(iblockdata, enumblockrotation);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(CreakingHeartBlock.AXIS, CreakingHeartBlock.CREAKING);
    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof CreakingHeartBlockEntity creakingheartblockentity) {
            creakingheartblockentity.removeProtector((DamageSource) null);
        }

        super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
    }

    @Override
    public IBlockData playerWillDestroy(World world, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof CreakingHeartBlockEntity creakingheartblockentity) {
            creakingheartblockentity.removeProtector(entityhuman.damageSources().playerAttack(entityhuman));
        }

        return super.playerWillDestroy(world, blockposition, iblockdata, entityhuman);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        if (iblockdata.getValue(CreakingHeartBlock.CREAKING) != CreakingHeartBlock.a.ACTIVE) {
            return 0;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof CreakingHeartBlockEntity) {
                CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) tileentity;

                return creakingheartblockentity.getAnalogOutputSignal();
            } else {
                return 0;
            }
        }
    }

    public static enum a implements INamable {

        DISABLED("disabled"), DORMANT("dormant"), ACTIVE("active");

        private final String name;

        private a(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
