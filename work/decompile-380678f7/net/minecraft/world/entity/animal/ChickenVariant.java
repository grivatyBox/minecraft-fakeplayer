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

public record ChickenVariant(ModelAndTexture<ChickenVariant.a> modelAndTexture, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition> {

    public static final Codec<ChickenVariant> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ModelAndTexture.codec(ChickenVariant.a.CODEC, ChickenVariant.a.NORMAL).forGetter(ChickenVariant::modelAndTexture), SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(ChickenVariant::spawnConditions)).apply(instance, ChickenVariant::new);
    });
    public static final Codec<ChickenVariant> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ModelAndTexture.codec(ChickenVariant.a.CODEC, ChickenVariant.a.NORMAL).forGetter(ChickenVariant::modelAndTexture)).apply(instance, ChickenVariant::new);
    });
    public static final Codec<Holder<ChickenVariant>> CODEC = RegistryFixedCodec.<Holder<ChickenVariant>>create(Registries.CHICKEN_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ChickenVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.CHICKEN_VARIANT);

    private ChickenVariant(ModelAndTexture<ChickenVariant.a> modelandtexture) {
        this(modelandtexture, SpawnPrioritySelectors.EMPTY);
    }

    @Override
    public List<PriorityProvider.a<SpawnContext, SpawnCondition>> selectors() {
        return this.spawnConditions.selectors();
    }

    public static enum a implements INamable {

        NORMAL("normal"), COLD("cold");

        public static final Codec<ChickenVariant.a> CODEC = INamable.<ChickenVariant.a>fromEnum(ChickenVariant.a::values);
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
