package net.minecraft.world.entity.animal.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEffect;

public record WolfSoundVariant(Holder<SoundEffect> ambientSound, Holder<SoundEffect> deathSound, Holder<SoundEffect> growlSound, Holder<SoundEffect> hurtSound, Holder<SoundEffect> pantSound, Holder<SoundEffect> whineSound) {

    public static final Codec<WolfSoundVariant> DIRECT_CODEC = getWolfSoundVariantCodec();
    public static final Codec<WolfSoundVariant> NETWORK_CODEC = getWolfSoundVariantCodec();
    public static final Codec<Holder<WolfSoundVariant>> CODEC = RegistryFixedCodec.<Holder<WolfSoundVariant>>create(Registries.WOLF_SOUND_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfSoundVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_SOUND_VARIANT);

    private static Codec<WolfSoundVariant> getWolfSoundVariantCodec() {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(SoundEffect.CODEC.fieldOf("ambient_sound").forGetter(WolfSoundVariant::ambientSound), SoundEffect.CODEC.fieldOf("death_sound").forGetter(WolfSoundVariant::deathSound), SoundEffect.CODEC.fieldOf("growl_sound").forGetter(WolfSoundVariant::growlSound), SoundEffect.CODEC.fieldOf("hurt_sound").forGetter(WolfSoundVariant::hurtSound), SoundEffect.CODEC.fieldOf("pant_sound").forGetter(WolfSoundVariant::pantSound), SoundEffect.CODEC.fieldOf("whine_sound").forGetter(WolfSoundVariant::whineSound)).apply(instance, WolfSoundVariant::new);
        });
    }
}
