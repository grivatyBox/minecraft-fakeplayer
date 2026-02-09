package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record PigVariant(ModelAndTexture<PigVariant.a> modelAndTexture, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition> {

    public static final Codec<PigVariant> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ModelAndTexture.codec(PigVariant.a.CODEC, PigVariant.a.NORMAL).forGetter(PigVariant::modelAndTexture), SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(PigVariant::spawnConditions)).apply(instance, PigVariant::new);
    });
    public static final Codec<PigVariant> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ModelAndTexture.codec(PigVariant.a.CODEC, PigVariant.a.NORMAL).forGetter(PigVariant::modelAndTexture)).apply(instance, PigVariant::new);
    });
    public static final Codec<Holder<PigVariant>> CODEC = RegistryFixedCodec.<Holder<PigVariant>>create(Registries.PIG_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<PigVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.PIG_VARIANT);

    private PigVariant(ModelAndTexture<PigVariant.a> modelandtexture) {
        this(modelandtexture, SpawnPrioritySelectors.EMPTY);
    }

    @Override
    public List<PriorityProvider.a<SpawnContext, SpawnCondition>> selectors() {
        return this.spawnConditions.selectors();
    }

    public static enum a implements INamable {

        NORMAL("normal"), COLD("cold");

        public static final Codec<PigVariant.a> CODEC = INamable.<PigVariant.a>fromEnum(PigVariant.a::values);
        private final String name;

        private a(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
