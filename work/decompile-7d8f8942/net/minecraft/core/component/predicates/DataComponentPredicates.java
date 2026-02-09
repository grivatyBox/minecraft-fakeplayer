package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class DataComponentPredicates {

    public static final DataComponentPredicate.b<DamagePredicate> DAMAGE = register("damage", DamagePredicate.CODEC);
    public static final DataComponentPredicate.b<EnchantmentsPredicate.a> ENCHANTMENTS = register("enchantments", EnchantmentsPredicate.a.CODEC);
    public static final DataComponentPredicate.b<EnchantmentsPredicate.b> STORED_ENCHANTMENTS = register("stored_enchantments", EnchantmentsPredicate.b.CODEC);
    public static final DataComponentPredicate.b<PotionsPredicate> POTIONS = register("potion_contents", PotionsPredicate.CODEC);
    public static final DataComponentPredicate.b<CustomDataPredicate> CUSTOM_DATA = register("custom_data", CustomDataPredicate.CODEC);
    public static final DataComponentPredicate.b<ContainerPredicate> CONTAINER = register("container", ContainerPredicate.CODEC);
    public static final DataComponentPredicate.b<BundlePredicate> BUNDLE_CONTENTS = register("bundle_contents", BundlePredicate.CODEC);
    public static final DataComponentPredicate.b<FireworkExplosionPredicate> FIREWORK_EXPLOSION = register("firework_explosion", FireworkExplosionPredicate.CODEC);
    public static final DataComponentPredicate.b<FireworksPredicate> FIREWORKS = register("fireworks", FireworksPredicate.CODEC);
    public static final DataComponentPredicate.b<WritableBookPredicate> WRITABLE_BOOK = register("writable_book_content", WritableBookPredicate.CODEC);
    public static final DataComponentPredicate.b<WrittenBookPredicate> WRITTEN_BOOK = register("written_book_content", WrittenBookPredicate.CODEC);
    public static final DataComponentPredicate.b<AttributeModifiersPredicate> ATTRIBUTE_MODIFIERS = register("attribute_modifiers", AttributeModifiersPredicate.CODEC);
    public static final DataComponentPredicate.b<TrimPredicate> ARMOR_TRIM = register("trim", TrimPredicate.CODEC);
    public static final DataComponentPredicate.b<JukeboxPlayablePredicate> JUKEBOX_PLAYABLE = register("jukebox_playable", JukeboxPlayablePredicate.CODEC);

    public DataComponentPredicates() {}

    private static <T extends DataComponentPredicate> DataComponentPredicate.b<T> register(String s, Codec<T> codec) {
        return (DataComponentPredicate.b) IRegistry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, s, new DataComponentPredicate.b(codec));
    }

    public static DataComponentPredicate.b<?> bootstrap(IRegistry<DataComponentPredicate.b<?>> iregistry) {
        return DataComponentPredicates.DAMAGE;
    }
}
