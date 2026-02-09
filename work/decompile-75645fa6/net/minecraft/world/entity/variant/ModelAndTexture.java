package net.minecraft.world.entity.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public record ModelAndTexture<T>(T model, ClientAsset.b asset) {

    public ModelAndTexture(T t0, MinecraftKey minecraftkey) {
        this(t0, new ClientAsset.b(minecraftkey));
    }

    public static <T> MapCodec<ModelAndTexture<T>> codec(Codec<T> codec, T t0) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(codec.optionalFieldOf("model", t0).forGetter(ModelAndTexture::model), ClientAsset.b.DEFAULT_FIELD_CODEC.forGetter(ModelAndTexture::asset)).apply(instance, ModelAndTexture::new);
        });
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, ModelAndTexture<T>> streamCodec(StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec) {
        return StreamCodec.composite(streamcodec, ModelAndTexture::model, ClientAsset.b.STREAM_CODEC, ModelAndTexture::asset, ModelAndTexture::new);
    }
}
