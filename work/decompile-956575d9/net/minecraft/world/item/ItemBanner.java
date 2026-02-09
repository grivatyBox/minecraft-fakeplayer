package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockBannerAbstract;
import org.apache.commons.lang3.Validate;

public class ItemBanner extends ItemBlockWallable {

    public ItemBanner(Block block, Block block1, Item.Info item_info) {
        super(block, block1, EnumDirection.DOWN, item_info);
        Validate.isInstanceOf(BlockBannerAbstract.class, block);
        Validate.isInstanceOf(BlockBannerAbstract.class, block1);
    }

    public EnumColor getColor() {
        return ((BlockBannerAbstract) this.getBlock()).getColor();
    }
}
