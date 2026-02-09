package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Function;
import net.minecraft.advancements.critereon.CriterionConditionEnchantments;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class EnchantmentsPredicate implements SingleComponentItemPredicate<ItemEnchantments> {

    private final List<CriterionConditionEnchantments> enchantments;

    protected EnchantmentsPredicate(List<CriterionConditionEnchantments> list) {
        this.enchantments = list;
    }

    public static <T extends EnchantmentsPredicate> Codec<T> codec(Function<List<CriterionConditionEnchantments>, T> function) {
        return CriterionConditionEnchantments.CODEC.listOf().xmap(function, EnchantmentsPredicate::enchantments);
    }

    protected List<CriterionConditionEnchantments> enchantments() {
        return this.enchantments;
    }

    public boolean matches(ItemEnchantments itemenchantments) {
        for (CriterionConditionEnchantments criterionconditionenchantments : this.enchantments) {
            if (!criterionconditionenchantments.containedIn(itemenchantments)) {
                return false;
            }
        }

        return true;
    }

    public static EnchantmentsPredicate.a enchantments(List<CriterionConditionEnchantments> list) {
        return new EnchantmentsPredicate.a(list);
    }

    public static EnchantmentsPredicate.b storedEnchantments(List<CriterionConditionEnchantments> list) {
        return new EnchantmentsPredicate.b(list);
    }

    public static class a extends EnchantmentsPredicate {

        public static final Codec<EnchantmentsPredicate.a> CODEC = codec(EnchantmentsPredicate.a::new);

        protected a(List<CriterionConditionEnchantments> list) {
            super(list);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.ENCHANTMENTS;
        }
    }

    public static class b extends EnchantmentsPredicate {

        public static final Codec<EnchantmentsPredicate.b> CODEC = codec(EnchantmentsPredicate.b::new);

        protected b(List<CriterionConditionEnchantments> list) {
            super(list);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.STORED_ENCHANTMENTS;
        }
    }
}
