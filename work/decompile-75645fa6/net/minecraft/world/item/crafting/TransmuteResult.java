package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record TransmuteResult(Holder<Item> item, int count, DataComponentPatch components) {

    private static final Codec<TransmuteResult> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Item.CODEC.fieldOf("id").forGetter(TransmuteResult::item), ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(TransmuteResult::count), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(TransmuteResult::components)).apply(instance, TransmuteResult::new);
    });
    public static final Codec<TransmuteResult> CODEC = Codec.withAlternative(TransmuteResult.FULL_CODEC, Item.CODEC, (holder) -> {
        return new TransmuteResult((Item) holder.value());
    }).validate(TransmuteResult::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteResult> STREAM_CODEC = StreamCodec.composite(Item.STREAM_CODEC, TransmuteResult::item, ByteBufCodecs.VAR_INT, TransmuteResult::count, DataComponentPatch.STREAM_CODEC, TransmuteResult::components, TransmuteResult::new);

    public TransmuteResult(Item item) {
        this(item.builtInRegistryHolder(), 1, DataComponentPatch.EMPTY);
    }

    private static DataResult<TransmuteResult> validate(TransmuteResult transmuteresult) {
        return ItemStack.validateStrict(new ItemStack(transmuteresult.item, transmuteresult.count, transmuteresult.components)).map((itemstack) -> {
            return transmuteresult;
        });
    }

    public ItemStack apply(ItemStack itemstack) {
        ItemStack itemstack1 = itemstack.transmuteCopy(this.item.value(), this.count);

        itemstack1.applyComponents(this.components);
        return itemstack1;
    }

    public boolean isResultUnchanged(ItemStack itemstack) {
        ItemStack itemstack1 = this.apply(itemstack);

        return itemstack1.getCount() == 1 && ItemStack.isSameItemSameComponents(itemstack, itemstack1);
    }

    public SlotDisplay display() {
        return new SlotDisplay.f(new ItemStack(this.item, this.count, this.components));
    }
}
