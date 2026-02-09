package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class ItemTippedArrow extends ItemArrow {

    public ItemTippedArrow(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack itemstack = super.getDefaultInstance();

        itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
        return itemstack;
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        PotionContents potioncontents = (PotionContents) itemstack.get(DataComponents.POTION_CONTENTS);

        return potioncontents != null ? potioncontents.getName(this.descriptionId + ".effect.") : super.getName(itemstack);
    }
}
