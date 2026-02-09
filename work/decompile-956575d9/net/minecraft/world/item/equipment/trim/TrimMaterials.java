package net.minecraft.world.item.equipment.trim;

import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ProvidesTrimMaterial;

public class TrimMaterials {

    public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");
    public static final ResourceKey<TrimMaterial> RESIN = registryKey("resin");

    public TrimMaterials() {}

    public static void bootstrap(BootstrapContext<TrimMaterial> bootstrapcontext) {
        register(bootstrapcontext, TrimMaterials.QUARTZ, ChatModifier.EMPTY.withColor(14931140), MaterialAssetGroup.QUARTZ);
        register(bootstrapcontext, TrimMaterials.IRON, ChatModifier.EMPTY.withColor(15527148), MaterialAssetGroup.IRON);
        register(bootstrapcontext, TrimMaterials.NETHERITE, ChatModifier.EMPTY.withColor(6445145), MaterialAssetGroup.NETHERITE);
        register(bootstrapcontext, TrimMaterials.REDSTONE, ChatModifier.EMPTY.withColor(9901575), MaterialAssetGroup.REDSTONE);
        register(bootstrapcontext, TrimMaterials.COPPER, ChatModifier.EMPTY.withColor(11823181), MaterialAssetGroup.COPPER);
        register(bootstrapcontext, TrimMaterials.GOLD, ChatModifier.EMPTY.withColor(14594349), MaterialAssetGroup.GOLD);
        register(bootstrapcontext, TrimMaterials.EMERALD, ChatModifier.EMPTY.withColor(1155126), MaterialAssetGroup.EMERALD);
        register(bootstrapcontext, TrimMaterials.DIAMOND, ChatModifier.EMPTY.withColor(7269586), MaterialAssetGroup.DIAMOND);
        register(bootstrapcontext, TrimMaterials.LAPIS, ChatModifier.EMPTY.withColor(4288151), MaterialAssetGroup.LAPIS);
        register(bootstrapcontext, TrimMaterials.AMETHYST, ChatModifier.EMPTY.withColor(10116294), MaterialAssetGroup.AMETHYST);
        register(bootstrapcontext, TrimMaterials.RESIN, ChatModifier.EMPTY.withColor(16545810), MaterialAssetGroup.RESIN);
    }

    public static Optional<Holder<TrimMaterial>> getFromIngredient(HolderLookup.a holderlookup_a, ItemStack itemstack) {
        ProvidesTrimMaterial providestrimmaterial = (ProvidesTrimMaterial) itemstack.get(DataComponents.PROVIDES_TRIM_MATERIAL);

        return providestrimmaterial != null ? providestrimmaterial.unwrap(holderlookup_a) : Optional.empty();
    }

    private static void register(BootstrapContext<TrimMaterial> bootstrapcontext, ResourceKey<TrimMaterial> resourcekey, ChatModifier chatmodifier, MaterialAssetGroup materialassetgroup) {
        IChatBaseComponent ichatbasecomponent = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("trim_material", resourcekey.location())).withStyle(chatmodifier);

        bootstrapcontext.register(resourcekey, new TrimMaterial(materialassetgroup, ichatbasecomponent));
    }

    private static ResourceKey<TrimMaterial> registryKey(String s) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, MinecraftKey.withDefaultNamespace(s));
    }
}
