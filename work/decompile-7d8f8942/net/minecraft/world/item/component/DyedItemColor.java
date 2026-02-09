package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public record DyedItemColor(int rgb) implements TooltipProvider {

    public static final Codec<DyedItemColor> CODEC = ExtraCodecs.RGB_COLOR_CODEC.xmap(DyedItemColor::new, DyedItemColor::rgb);
    public static final StreamCodec<ByteBuf, DyedItemColor> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, DyedItemColor::rgb, DyedItemColor::new);
    public static final int LEATHER_COLOR = -6265536;

    public static int getOrDefault(ItemStack itemstack, int i) {
        DyedItemColor dyeditemcolor = (DyedItemColor) itemstack.get(DataComponents.DYED_COLOR);

        return dyeditemcolor != null ? ARGB.opaque(dyeditemcolor.rgb()) : i;
    }

    public static ItemStack applyDyes(ItemStack itemstack, List<ItemDye> list) {
        if (!itemstack.is(TagsItem.DYEABLE)) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack1 = itemstack.copyWithCount(1);
            int i = 0;
            int j = 0;
            int k = 0;
            int l = 0;
            int i1 = 0;
            DyedItemColor dyeditemcolor = (DyedItemColor) itemstack1.get(DataComponents.DYED_COLOR);

            if (dyeditemcolor != null) {
                int j1 = ARGB.red(dyeditemcolor.rgb());
                int k1 = ARGB.green(dyeditemcolor.rgb());
                int l1 = ARGB.blue(dyeditemcolor.rgb());

                l += Math.max(j1, Math.max(k1, l1));
                i += j1;
                j += k1;
                k += l1;
                ++i1;
            }

            for (ItemDye itemdye : list) {
                int i2 = itemdye.getDyeColor().getTextureDiffuseColor();
                int j2 = ARGB.red(i2);
                int k2 = ARGB.green(i2);
                int l2 = ARGB.blue(i2);

                l += Math.max(j2, Math.max(k2, l2));
                i += j2;
                j += k2;
                k += l2;
                ++i1;
            }

            int i3 = i / i1;
            int j3 = j / i1;
            int k3 = k / i1;
            float f = (float) l / (float) i1;
            float f1 = (float) Math.max(i3, Math.max(j3, k3));

            i3 = (int) ((float) i3 * f / f1);
            j3 = (int) ((float) j3 * f / f1);
            k3 = (int) ((float) k3 * f / f1);
            int l3 = ARGB.color(0, i3, j3, k3);

            itemstack1.set(DataComponents.DYED_COLOR, new DyedItemColor(l3));
            return itemstack1;
        }
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        if (tooltipflag.isAdvanced()) {
            consumer.accept(IChatBaseComponent.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).withStyle(EnumChatFormat.GRAY));
        } else {
            consumer.accept(IChatBaseComponent.translatable("item.dyed").withStyle(EnumChatFormat.GRAY, EnumChatFormat.ITALIC));
        }

    }
}
