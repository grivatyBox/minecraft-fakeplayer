package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;

public class ItemSkullPlayer extends ItemBlockWallable {

    public ItemSkullPlayer(Block block, Block block1, Item.Info item_info) {
        super(block, block1, EnumDirection.DOWN, item_info);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        ResolvableProfile resolvableprofile = (ResolvableProfile) itemstack.get(DataComponents.PROFILE);

        return (IChatBaseComponent) (resolvableprofile != null && resolvableprofile.name().isPresent() ? IChatBaseComponent.translatable(this.descriptionId + ".named", resolvableprofile.name().get()) : super.getName(itemstack));
    }
}
