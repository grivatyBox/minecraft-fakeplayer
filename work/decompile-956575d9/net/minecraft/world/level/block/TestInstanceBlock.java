package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class TestInstanceBlock extends BlockTileEntity implements GameMasterBlock {

    public static final MapCodec<TestInstanceBlock> CODEC = simpleCodec(TestInstanceBlock::new);

    public TestInstanceBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TestInstanceBlockEntity(blockposition, iblockdata);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TestInstanceBlockEntity testinstanceblockentity) {
            if (!entityhuman.canUseGameMasterBlocks()) {
                return EnumInteractionResult.PASS;
            } else {
                if (entityhuman.getCommandSenderWorld().isClientSide) {
                    entityhuman.openTestInstanceBlock(testinstanceblockentity);
                }

                return EnumInteractionResult.SUCCESS;
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    protected MapCodec<TestInstanceBlock> codec() {
        return TestInstanceBlock.CODEC;
    }
}
