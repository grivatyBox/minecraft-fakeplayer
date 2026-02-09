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

public record CowVariant(ModelAndTexture<CowVariant.a> modelAndTexture, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition> {

    public static final Codec<CowVariant> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ModelAndTexture.codec(CowVariant.a.CODEC, CowVariant.a.NORMAL).forGetter(CowVariant::modelAndTexture), SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(CowVariant::spawnConditions)).apply(instance, CowVariant::new);
    });
    public static final Codec<CowVariant> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ModelAndTexture.codec(CowVariant.a.CODEC, CowVariant.a.NORMAL).forGetter(CowVariant::modelAndTexture)).apply(instance, CowVariant::new);
    });
    public static final Codec<Holder<CowVariant>> CODEC = RegistryFixedCodec.<Holder<CowVariant>>create(Registries.COW_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<CowVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.COW_VARIANT);

    private CowVariant(ModelAndTexture<CowVariant.a> modelandtexture) {
        this(modelandtexture, SpawnPrioritySelectors.EMPTY);
    }

    @Override
    public List<PriorityProvider.a<SpawnContext, SpawnCondition>> selectors() {
        return this.spawnConditions.selectors();
    }

    public static enum a implements INamable {

        NORMAL("normal"), COLD("cold"), WARM("warm");

        public static final Codec<CowVariant.a> CODEC = INamable.<CowVariant.a>fromEnum(CowVariant.a::values);
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
