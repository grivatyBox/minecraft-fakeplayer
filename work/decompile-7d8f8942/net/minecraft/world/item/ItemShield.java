package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;

public class ItemShield extends Item {

    public ItemShield(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        EnumColor enumcolor = (EnumColor) itemstack.get(DataComponents.BASE_COLOR);

        return (IChatBaseComponent) (enumcolor != null ? IChatBaseComponent.translatable(this.descriptionId + "." + enumcolor.getName()) : super.getName(itemstack));
    }
}
