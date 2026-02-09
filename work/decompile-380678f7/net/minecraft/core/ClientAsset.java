package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public record ClientAsset(MinecraftKey id, MinecraftKey texturePath) {

    public static final Codec<ClientAsset> CODEC = MinecraftKey.CODEC.xmap(ClientAsset::new, ClientAsset::id);
    public static final MapCodec<ClientAsset> DEFAULT_FIELD_CODEC = ClientAsset.CODEC.fieldOf("asset_id");
    public static final StreamCodec<ByteBuf, ClientAsset> STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, ClientAsset::id, ClientAsset::new);

    public ClientAsset(MinecraftKey minecraftkey) {
        this(minecraftkey, minecraftkey.withPath((s) -> {
            return "textures/" + s + ".png";
        }));
    }
}
