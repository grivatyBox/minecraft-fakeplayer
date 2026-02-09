package net.minecraft.world.item.crafting;

import com.mojang.datafixers.Products.P6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class RecipeSerializerCooking<T extends RecipeCooking> implements RecipeSerializer<T> {

    private final RecipeCooking.a<T> factory;
    private final Codec<T> codec;

    public RecipeSerializerCooking(RecipeCooking.a<T> recipecooking_a, int i) {
        this.factory = recipecooking_a;
        this.codec = RecordCodecBuilder.create((instance) -> {
            P6 p6 = instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((recipecooking) -> {
                return recipecooking.group;
            }), CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter((recipecooking) -> {
                return recipecooking.category;
            }), RecipeItemStack.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((recipecooking) -> {
                return recipecooking.ingredient;
            }), BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem).fieldOf("result").forGetter((recipecooking) -> {
                return recipecooking.result;
            }), Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter((recipecooking) -> {
                return recipecooking.experience;
            }), Codec.INT.fieldOf("cookingtime").orElse(i).forGetter((recipecooking) -> {
                return recipecooking.cookingTime;
            }));

            Objects.requireNonNull(recipecooking_a);
            return p6.apply(instance, recipecooking_a::create);
        });
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    @Override
    public T fromNetwork(PacketDataSerializer packetdataserializer) {
        String s = packetdataserializer.readUtf();
        CookingBookCategory cookingbookcategory = (CookingBookCategory) packetdataserializer.readEnum(CookingBookCategory.class);
        RecipeItemStack recipeitemstack = RecipeItemStack.fromNetwork(packetdataserializer);
        ItemStack itemstack = packetdataserializer.readItem();
        float f = packetdataserializer.readFloat();
        int i = packetdataserializer.readVarInt();

        return this.factory.create(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }

    public void toNetwork(PacketDataSerializer packetdataserializer, T t0) {
        packetdataserializer.writeUtf(t0.group);
        packetdataserializer.writeEnum(t0.category());
        t0.ingredient.toNetwork(packetdataserializer);
        packetdataserializer.writeItem(t0.result);
        packetdataserializer.writeFloat(t0.experience);
        packetdataserializer.writeVarInt(t0.cookingTime);
    }

    public RecipeCooking create(String s, CookingBookCategory cookingbookcategory, RecipeItemStack recipeitemstack, ItemStack itemstack, float f, int i) {
        return this.factory.create(s, cookingbookcategory, recipeitemstack, itemstack, f, i);
    }
}
