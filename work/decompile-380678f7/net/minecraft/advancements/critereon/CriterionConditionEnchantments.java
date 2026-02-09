package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record CriterionConditionEnchantments(Optional<HolderSet<Enchantment>> enchantments, CriterionConditionValue.IntegerRange level) {

    public static final Codec<CriterionConditionEnchantments> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(CriterionConditionEnchantments::enchantments), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("levels", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionEnchantments::level)).apply(instance, CriterionConditionEnchantments::new);
    });

    public CriterionConditionEnchantments(Holder<Enchantment> holder, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        this(Optional.of(HolderSet.direct(holder)), criterionconditionvalue_integerrange);
    }

    public CriterionConditionEnchantments(HolderSet<Enchantment> holderset, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        this(Optional.of(holderset), criterionconditionvalue_integerrange);
    }

    public boolean containedIn(ItemEnchantments itemenchantments) {
        if (this.enchantments.isPresent()) {
            for (Holder<Enchantment> holder : (HolderSet) this.enchantments.get()) {
                if (this.matchesEnchantment(itemenchantments, holder)) {
                    return true;
                }
            }

            return false;
        } else if (this.level != CriterionConditionValue.IntegerRange.ANY) {
            for (Object2IntMap.Entry<Holder<Enchantment>> object2intmap_entry : itemenchantments.entrySet()) {
                if (this.level.matches(object2intmap_entry.getIntValue())) {
                    return true;
                }
            }

            return false;
        } else {
            return !itemenchantments.isEmpty();
        }
    }

    private boolean matchesEnchantment(ItemEnchantments itemenchantments, Holder<Enchantment> holder) {
        int i = itemenchantments.getLevel(holder);

        return i == 0 ? false : (this.level == CriterionConditionValue.IntegerRange.ANY ? true : this.level.matches(i));
    }
}
