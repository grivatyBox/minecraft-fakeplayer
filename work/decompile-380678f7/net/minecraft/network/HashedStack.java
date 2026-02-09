package net.minecraft.network;

import com.mojang.datafixers.DataFixUtils;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface HashedStack {

    HashedStack EMPTY = new HashedStack() {
        public String toString() {
            return "<empty>";
        }

        @Override
        public boolean matches(ItemStack itemstack, HashedPatchMap.a hashedpatchmap_a) {
            return itemstack.isEmpty();
        }
    };
    StreamCodec<RegistryFriendlyByteBuf, HashedStack> STREAM_CODEC = ByteBufCodecs.optional(HashedStack.a.STREAM_CODEC).map((optional) -> {
        return (HashedStack) DataFixUtils.orElse(optional, HashedStack.EMPTY);
    }, (hashedstack) -> {
        Optional optional;

        if (hashedstack instanceof HashedStack.a hashedstack_a) {
            optional = Optional.of(hashedstack_a);
        } else {
            optional = Optional.empty();
        }

        return optional;
    });

    boolean matches(ItemStack itemstack, HashedPatchMap.a hashedpatchmap_a);

    static HashedStack create(ItemStack itemstack, HashedPatchMap.a hashedpatchmap_a) {
        return (HashedStack) (itemstack.isEmpty() ? HashedStack.EMPTY : new HashedStack.a(itemstack.getItemHolder(), itemstack.getCount(), HashedPatchMap.create(itemstack.getComponentsPatch(), hashedpatchmap_a)));
    }

    public static record a(Holder<Item> item, int count, HashedPatchMap components) implements HashedStack {

        public static final StreamCodec<RegistryFriendlyByteBuf, HashedStack.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.ITEM), HashedStack.a::item, ByteBufCodecs.VAR_INT, HashedStack.a::count, HashedPatchMap.STREAM_CODEC, HashedStack.a::components, HashedStack.a::new);

        @Override
        public boolean matches(ItemStack itemstack, HashedPatchMap.a hashedpatchmap_a) {
            return this.count != itemstack.getCount() ? false : (!this.item.equals(itemstack.getItemHolder()) ? false : this.components.matches(itemstack.getComponentsPatch(), hashedpatchmap_a));
        }
    }
}
