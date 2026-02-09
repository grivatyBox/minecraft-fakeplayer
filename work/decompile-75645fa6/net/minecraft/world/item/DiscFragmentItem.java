package net.minecraft.world.item;

import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.world.item.component.TooltipDisplay;

public class DiscFragmentItem extends Item {

    public DiscFragmentItem(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, TooltipDisplay tooltipdisplay, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        consumer.accept(this.getDisplayName().withStyle(EnumChatFormat.GRAY));
    }

    public IChatMutableComponent getDisplayName() {
        return IChatBaseComponent.translatable(this.descriptionId + ".desc");
    }
}
