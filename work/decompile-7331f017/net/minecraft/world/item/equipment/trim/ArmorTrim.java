package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.EquipmentModel;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern, boolean showInTooltip) implements TooltipProvider {

    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter((armortrim) -> {
            return armortrim.showInTooltip;
        })).apply(instance, ArmorTrim::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(TrimMaterial.STREAM_CODEC, ArmorTrim::material, TrimPattern.STREAM_CODEC, ArmorTrim::pattern, ByteBufCodecs.BOOL, (armortrim) -> {
        return armortrim.showInTooltip;
    }, ArmorTrim::new);
    private static final IChatBaseComponent UPGRADE_TITLE = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("item", MinecraftKey.withDefaultNamespace("smithing_template.upgrade"))).withStyle(EnumChatFormat.GRAY);

    public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder1) {
        this(holder, holder1, true);
    }

    private static String getColorPaletteSuffix(Holder<TrimMaterial> holder, MinecraftKey minecraftkey) {
        String s = (String) ((TrimMaterial) holder.value()).overrideArmorMaterials().get(minecraftkey);

        return s != null ? s : ((TrimMaterial) holder.value()).assetName();
    }

    public boolean hasPatternAndMaterial(Holder<TrimPattern> holder, Holder<TrimMaterial> holder1) {
        return holder.equals(this.pattern) && holder1.equals(this.material);
    }

    public MinecraftKey getTexture(EquipmentModel.d equipmentmodel_d, MinecraftKey minecraftkey) {
        MinecraftKey minecraftkey1 = ((TrimPattern) this.pattern.value()).assetId();
        String s = getColorPaletteSuffix(this.material, minecraftkey);

        return minecraftkey1.withPath((s1) -> {
            return "trims/entity/" + equipmentmodel_d.getSerializedName() + "/" + s1 + "_" + s;
        });
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        if (this.showInTooltip) {
            consumer.accept(ArmorTrim.UPGRADE_TITLE);
            consumer.accept(CommonComponents.space().append(((TrimPattern) this.pattern.value()).copyWithStyle(this.material)));
            consumer.accept(CommonComponents.space().append(((TrimMaterial) this.material.value()).description()));
        }
    }

    public ArmorTrim withTooltip(boolean flag) {
        return new ArmorTrim(this.material, this.pattern, flag);
    }
}
