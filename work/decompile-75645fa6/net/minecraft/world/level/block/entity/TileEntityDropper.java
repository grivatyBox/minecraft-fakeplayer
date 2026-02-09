package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.block.state.IBlockData;

public class TileEntityDropper extends TileEntityDispenser {

    private static final IChatBaseComponent DEFAULT_NAME = IChatBaseComponent.translatable("container.dropper");

    public TileEntityDropper(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.DROPPER, blockposition, iblockdata);
    }

    @Override
    protected IChatBaseComponent getDefaultName() {
        return TileEntityDropper.DEFAULT_NAME;
    }
}
