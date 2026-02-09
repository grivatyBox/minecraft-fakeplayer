package net.minecraft.world.entity.animal.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record WolfVariant(WolfVariant.a assetInfo, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition> {

    public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(WolfVariant.a.CODEC.fieldOf("assets").forGetter(WolfVariant::assetInfo), SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(WolfVariant::spawnConditions)).apply(instance, WolfVariant::new);
    });
    public static final Codec<WolfVariant> NETWORK_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(WolfVariant.a.CODEC.fieldOf("assets").forGetter(WolfVariant::assetInfo)).apply(instance, WolfVariant::new);
    });
    public static final Codec<Holder<WolfVariant>> CODEC = RegistryFixedCodec.<Holder<WolfVariant>>create(Registries.WOLF_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_VARIANT);

    private WolfVariant(WolfVariant.a wolfvariant_a) {
        this(wolfvariant_a, SpawnPrioritySelectors.EMPTY);
    }

    @Override
    public List<PriorityProvider.a<SpawnContext, SpawnCondition>> selectors() {
        return this.spawnConditions.selectors();
    }

    public static record a(ClientAsset wild, ClientAsset tame, ClientAsset angry) {

        public static final Codec<WolfVariant.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ClientAsset.CODEC.fieldOf("wild").forGetter(WolfVariant.a::wild), ClientAsset.CODEC.fieldOf("tame").forGetter(WolfVariant.a::tame), ClientAsset.CODEC.fieldOf("angry").forGetter(WolfVariant.a::angry)).apply(instance, WolfVariant.a::new);
        });
    }
}
