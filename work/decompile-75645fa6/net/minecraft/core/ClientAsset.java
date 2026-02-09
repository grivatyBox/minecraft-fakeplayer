package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;

public interface ClientAsset {

    MinecraftKey id();

    public static record b(MinecraftKey id, MinecraftKey texturePath) implements ClientAsset.c {

        public static final Codec<ClientAsset.b> CODEC = MinecraftKey.CODEC.xmap(ClientAsset.b::new, ClientAsset.b::id);
        public static final MapCodec<ClientAsset.b> DEFAULT_FIELD_CODEC = ClientAsset.b.CODEC.fieldOf("asset_id");
        public static final StreamCodec<ByteBuf, ClientAsset.b> STREAM_CODEC = MinecraftKey.STREAM_CODEC.map(ClientAsset.b::new, ClientAsset.b::id);

        public b(MinecraftKey minecraftkey) {
            this(minecraftkey, minecraftkey.withPath((s) -> {
                return "textures/" + s + ".png";
            }));
        }
    }

    public static record a(MinecraftKey texturePath, String url) implements ClientAsset.c {

        @Override
        public MinecraftKey id() {
            return this.texturePath;
        }
    }

    public interface c extends ClientAsset {

        MinecraftKey texturePath();
    }
}
