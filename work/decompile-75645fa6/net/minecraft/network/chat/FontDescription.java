package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.component.ResolvableProfile;

public interface FontDescription {

    Codec<FontDescription> CODEC = MinecraftKey.CODEC.flatComapMap(FontDescription.c::new, (fontdescription) -> {
        if (fontdescription instanceof FontDescription.c fontdescription_c) {
            return DataResult.success(fontdescription_c.id());
        } else {
            return DataResult.error(() -> {
                return "Unsupported font description type: " + String.valueOf(fontdescription);
            });
        }
    });
    FontDescription.c DEFAULT = new FontDescription.c(MinecraftKey.withDefaultNamespace("default"));

    public static record c(MinecraftKey id) implements FontDescription {

    }

    public static record a(MinecraftKey atlasId, MinecraftKey spriteId) implements FontDescription {

    }

    public static record b(ResolvableProfile profile, boolean hat) implements FontDescription {

    }
}
