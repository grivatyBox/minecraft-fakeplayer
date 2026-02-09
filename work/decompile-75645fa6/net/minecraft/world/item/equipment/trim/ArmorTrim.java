package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.EquipmentAsset;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) implements TooltipProvider {

    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)).apply(instance, ArmorTrim::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(TrimMaterial.STREAM_CODEC, ArmorTrim::material, TrimPattern.STREAM_CODEC, ArmorTrim::pattern, ArmorTrim::new);
    private static final IChatBaseComponent UPGRADE_TITLE = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.upgrade"))).withStyle(EnumChatFormat.GRAY);

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        consumer.accept(ArmorTrim.UPGRADE_TITLE);
        consumer.accept(CommonComponents.space().append((this.pattern.value()).copyWithStyle(this.material)));
        consumer.accept(CommonComponents.space().append(((TrimMaterial) this.material.value()).description()));
    }

    public MinecraftKey layerAssetId(String s, ResourceKey<EquipmentAsset> resourcekey) {
        MaterialAssetGroup.a materialassetgroup_a = ((TrimMaterial) this.material().value()).assets().assetId(resourcekey);

        return ((TrimPattern) this.pattern().value()).assetId().withPath((s1) -> {
            return s + "/" + s1 + "_" + materialassetgroup_a.suffix();
        });
    }
}
