package net.minecraft.world.item;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.block.Block;

public class ItemAir extends Item {

    public ItemAir(Block block, Item.Info item_info) {
        super(item_info);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        return this.getName();
    }
}
