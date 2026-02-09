package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ChargedProjectiles implements TooltipProvider {

    public static final ChargedProjectiles EMPTY = new ChargedProjectiles(List.of());
    public static final Codec<ChargedProjectiles> CODEC = ItemStack.CODEC.listOf().xmap(ChargedProjectiles::new, (chargedprojectiles) -> {
        return chargedprojectiles.items;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedProjectiles> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ChargedProjectiles::new, (chargedprojectiles) -> {
        return chargedprojectiles.items;
    });
    private final List<ItemStack> items;

    private ChargedProjectiles(List<ItemStack> list) {
        this.items = list;
    }

    public static ChargedProjectiles of(ItemStack itemstack) {
        return new ChargedProjectiles(List.of(itemstack.copy()));
    }

    public static ChargedProjectiles of(List<ItemStack> list) {
        return new ChargedProjectiles(List.copyOf(Lists.transform(list, ItemStack::copy)));
    }

    public boolean contains(Item item) {
        for (ItemStack itemstack : this.items) {
            if (itemstack.is(item)) {
                return true;
            }
        }

        return false;
    }

    public List<ItemStack> getItems() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof ChargedProjectiles) {
                ChargedProjectiles chargedprojectiles = (ChargedProjectiles) object;

                if (ItemStack.listMatches(this.items, chargedprojectiles.items)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public String toString() {
        return "ChargedProjectiles[items=" + String.valueOf(this.items) + "]";
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        ItemStack itemstack = null;
        int i = 0;

        for (ItemStack itemstack1 : this.items) {
            if (itemstack == null) {
                itemstack = itemstack1;
                i = 1;
            } else if (ItemStack.matches(itemstack, itemstack1)) {
                ++i;
            } else {
                addProjectileTooltip(item_b, consumer, itemstack, i);
                itemstack = itemstack1;
                i = 1;
            }
        }

        if (itemstack != null) {
            addProjectileTooltip(item_b, consumer, itemstack, i);
        }

    }

    private static void addProjectileTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, ItemStack itemstack, int i) {
        if (i == 1) {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.crossbow.projectile.single", itemstack.getDisplayName()));
        } else {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.crossbow.projectile.multiple", i, itemstack.getDisplayName()));
        }

        TooltipDisplay tooltipdisplay = (TooltipDisplay) itemstack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

        itemstack.addDetailsToTooltip(item_b, tooltipdisplay, (EntityHuman) null, TooltipFlag.NORMAL, (ichatbasecomponent) -> {
            consumer.accept(IChatBaseComponent.literal("  ").append(ichatbasecomponent).withStyle(EnumChatFormat.GRAY));
        });
    }
}
