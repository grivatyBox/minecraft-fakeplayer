package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;

public class TrialSpawnerBlock extends BlockTileEntity {

    public static final MapCodec<TrialSpawnerBlock> CODEC = simpleCodec(TrialSpawnerBlock::new);
    public static final BlockStateEnum<TrialSpawnerState> STATE = BlockProperties.TRIAL_SPAWNER_STATE;

    @Override
    public MapCodec<TrialSpawnerBlock> codec() {
        return TrialSpawnerBlock.CODEC;
    }

    public TrialSpawnerBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(TrialSpawnerBlock.STATE, TrialSpawnerState.INACTIVE));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(TrialSpawnerBlock.STATE);
    }

    @Override
    public EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TrialSpawnerBlockEntity(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        BlockEntityTicker blockentityticker;

        if (world instanceof WorldServer) {
            WorldServer worldserver = (WorldServer) world;

            blockentityticker = createTickerHelper(tileentitytypes, TileEntityTypes.TRIAL_SPAWNER, (world1, blockposition, iblockdata1, trialspawnerblockentity) -> {
                trialspawnerblockentity.getTrialSpawner().tickServer(worldserver, blockposition);
            });
        } else {
            blockentityticker = createTickerHelper(tileentitytypes, TileEntityTypes.TRIAL_SPAWNER, (world1, blockposition, iblockdata1, trialspawnerblockentity) -> {
                trialspawnerblockentity.getTrialSpawner().tickClient(world1, blockposition);
            });
        }

        return blockentityticker;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable IBlockAccess iblockaccess, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, iblockaccess, list, tooltipflag);
        Spawner.appendHoverText(itemstack, list, "spawn_data");
    }
}
