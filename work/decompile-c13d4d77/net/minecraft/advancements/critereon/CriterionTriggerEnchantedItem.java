package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class CriterionTriggerEnchantedItem extends CriterionTriggerAbstract<CriterionTriggerEnchantedItem.a> {

    public CriterionTriggerEnchantedItem() {}

    @Override
    public Codec<CriterionTriggerEnchantedItem.a> codec() {
        return CriterionTriggerEnchantedItem.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack, int i) {
        this.trigger(entityplayer, (criteriontriggerenchanteditem_a) -> {
            return criteriontriggerenchanteditem_a.matches(itemstack, i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item, CriterionConditionValue.IntegerRange levels) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerEnchantedItem.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionEntity.ADVANCEMENT_CODEC, "player").forGetter(CriterionTriggerEnchantedItem.a::player), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "item").forGetter(CriterionTriggerEnchantedItem.a::item), ExtraCodecs.strictOptionalField(CriterionConditionValue.IntegerRange.CODEC, "levels", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerEnchantedItem.a::levels)).apply(instance, CriterionTriggerEnchantedItem.a::new);
        });

        public static Criterion<CriterionTriggerEnchantedItem.a> enchantedItem() {
            return CriterionTriggers.ENCHANTED_ITEM.createCriterion(new CriterionTriggerEnchantedItem.a(Optional.empty(), Optional.empty(), CriterionConditionValue.IntegerRange.ANY));
        }

        public boolean matches(ItemStack itemstack, int i) {
            return this.item.isPresent() && !((CriterionConditionItem) this.item.get()).matches(itemstack) ? false : this.levels.matches(i);
        }
    }
}
