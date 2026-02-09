package net.minecraft.network.chat.contents.objects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.MinecraftKey;

public record AtlasSprite(MinecraftKey atlas, MinecraftKey sprite) implements ObjectInfo {

    public static final MinecraftKey DEFAULT_ATLAS = AtlasIds.BLOCKS;
    public static final MapCodec<AtlasSprite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.optionalFieldOf("atlas", AtlasSprite.DEFAULT_ATLAS).forGetter(AtlasSprite::atlas), MinecraftKey.CODEC.fieldOf("sprite").forGetter(AtlasSprite::sprite)).apply(instance, AtlasSprite::new);
    });

    @Override
    public MapCodec<AtlasSprite> codec() {
        return AtlasSprite.MAP_CODEC;
    }

    @Override
    public FontDescription fontDescription() {
        return new FontDescription.a(this.atlas, this.sprite);
    }

    private static String toShortName(MinecraftKey minecraftkey) {
        return minecraftkey.getNamespace().equals("minecraft") ? minecraftkey.getPath() : minecraftkey.toString();
    }

    @Override
    public String description() {
        String s = toShortName(this.sprite);

        return this.atlas.equals(AtlasSprite.DEFAULT_ATLAS) ? "[" + s + "]" : "[" + s + "@" + toShortName(this.atlas) + "]";
    }
}
