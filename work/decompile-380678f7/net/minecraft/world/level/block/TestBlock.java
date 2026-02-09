package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class TestBlock extends BlockTileEntity implements GameMasterBlock {

    public static final MapCodec<TestBlock> CODEC = simpleCodec(TestBlock::new);
    public static final BlockStateEnum<TestBlockMode> MODE = BlockProperties.TEST_BLOCK_MODE;

    public TestBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TestBlockEntity(blockposition, iblockdata);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        BlockItemStateProperties blockitemstateproperties = (BlockItemStateProperties) blockactioncontext.getItemInHand().get(DataComponents.BLOCK_STATE);
        IBlockData iblockdata = this.defaultBlockState();

        if (blockitemstateproperties != null) {
            TestBlockMode testblockmode = (TestBlockMode) blockitemstateproperties.get(TestBlock.MODE);

            if (testblockmode != null) {
                iblockdata = (IBlockData) iblockdata.setValue(TestBlock.MODE, testblockmode);
            }
        }

        return iblockdata;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(TestBlock.MODE);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TestBlockEntity testblockentity) {
            if (!entityhuman.canUseGameMasterBlocks()) {
                return EnumInteractionResult.PASS;
            } else {
                if (world.isClientSide) {
                    entityhuman.openTestBlock(testblockentity);
                }

                return EnumInteractionResult.SUCCESS;
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        TestBlockEntity testblockentity = getServerTestBlockEntity(worldserver, blockposition);

        if (testblockentity != null) {
            testblockentity.reset();
        }
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        TestBlockEntity testblockentity = getServerTestBlockEntity(world, blockposition);

        if (testblockentity != null) {
            if (testblockentity.getMode() != TestBlockMode.START) {
                boolean flag1 = world.hasNeighborSignal(blockposition);
                boolean flag2 = testblockentity.isPowered();

                if (flag1 && !flag2) {
                    testblockentity.setPowered(true);
                    testblockentity.trigger();
                } else if (!flag1 && flag2) {
                    testblockentity.setPowered(false);
                }

            }
        }
    }

    @Nullable
    private static TestBlockEntity getServerTestBlockEntity(World world, BlockPosition blockposition) {
        if (world instanceof WorldServer worldserver) {
            TileEntity tileentity = worldserver.getBlockEntity(blockposition);

            if (tileentity instanceof TestBlockEntity testblockentity) {
                return testblockentity;
            }
        }

        return null;
    }

    @Override
    public int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        if (iblockdata.getValue(TestBlock.MODE) != TestBlockMode.START) {
            return 0;
        } else {
            TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

            if (tileentity instanceof TestBlockEntity) {
                TestBlockEntity testblockentity = (TestBlockEntity) tileentity;

                return testblockentity.isPowered() ? 15 : 0;
            } else {
                return 0;
            }
        }
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        ItemStack itemstack = super.getCloneItemStack(iworldreader, blockposition, iblockdata, flag);

        return setModeOnStack(itemstack, (TestBlockMode) iblockdata.getValue(TestBlock.MODE));
    }

    public static ItemStack setModeOnStack(ItemStack itemstack, TestBlockMode testblockmode) {
        itemstack.set(DataComponents.BLOCK_STATE, ((BlockItemStateProperties) itemstack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)).with(TestBlock.MODE, testblockmode));
        return itemstack;
    }

    @Override
    protected MapCodec<TestBlock> codec() {
        return TestBlock.CODEC;
    }
}
