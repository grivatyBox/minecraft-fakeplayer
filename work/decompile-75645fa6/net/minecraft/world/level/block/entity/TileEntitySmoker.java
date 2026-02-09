package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerSmoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.level.block.state.IBlockData;

public class TileEntitySmoker extends TileEntityFurnace {

    private static final IChatBaseComponent DEFAULT_NAME = IChatBaseComponent.translatable("container.smoker");

    public TileEntitySmoker(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.SMOKER, blockposition, iblockdata, Recipes.SMOKING);
    }

    @Override
    protected IChatBaseComponent getDefaultName() {
        return TileEntitySmoker.DEFAULT_NAME;
    }

    @Override
    protected int getBurnDuration(FuelValues fuelvalues, ItemStack itemstack) {
        return super.getBurnDuration(fuelvalues, itemstack) / 2;
    }

    @Override
    protected Container createMenu(int i, PlayerInventory playerinventory) {
        return new ContainerSmoker(i, playerinventory, this, this.dataAccess);
    }
}
