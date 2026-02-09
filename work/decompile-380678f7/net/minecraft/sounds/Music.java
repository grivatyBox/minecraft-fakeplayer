package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;

public record Music(Holder<SoundEffect> event, int minDelay, int maxDelay, boolean replaceCurrentMusic) {

    public static final Codec<Music> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(SoundEffect.CODEC.fieldOf("sound").forGetter((music) -> {
            return music.event;
        }), Codec.INT.fieldOf("min_delay").forGetter((music) -> {
            return music.minDelay;
        }), Codec.INT.fieldOf("max_delay").forGetter((music) -> {
            return music.maxDelay;
        }), Codec.BOOL.fieldOf("replace_current_music").forGetter((music) -> {
            return music.replaceCurrentMusic;
        })).apply(instance, Music::new);
    });
}
