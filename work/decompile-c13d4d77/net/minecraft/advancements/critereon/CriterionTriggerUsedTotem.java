package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public class CriterionTriggerUsedTotem extends CriterionTriggerAbstract<CriterionTriggerUsedTotem.a> {

    public CriterionTriggerUsedTotem() {}

    @Override
    public Codec<CriterionTriggerUsedTotem.a> codec() {
        return CriterionTriggerUsedTotem.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, ItemStack itemstack) {
        this.trigger(entityplayer, (criteriontriggerusedtotem_a) -> {
            return criteriontriggerusedtotem_a.matches(itemstack);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<CriterionConditionItem> item) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerUsedTotem.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionEntity.ADVANCEMENT_CODEC, "player").forGetter(CriterionTriggerUsedTotem.a::player), ExtraCodecs.strictOptionalField(CriterionConditionItem.CODEC, "item").forGetter(CriterionTriggerUsedTotem.a::item)).apply(instance, CriterionTriggerUsedTotem.a::new);
        });

        public static Criterion<CriterionTriggerUsedTotem.a> usedTotem(CriterionConditionItem criterionconditionitem) {
            return CriterionTriggers.USED_TOTEM.createCriterion(new CriterionTriggerUsedTotem.a(Optional.empty(), Optional.of(criterionconditionitem)));
        }

        public static Criterion<CriterionTriggerUsedTotem.a> usedTotem(IMaterial imaterial) {
            return CriterionTriggers.USED_TOTEM.createCriterion(new CriterionTriggerUsedTotem.a(Optional.empty(), Optional.of(CriterionConditionItem.a.item().of(imaterial).build())));
        }

        public boolean matches(ItemStack itemstack) {
            return this.item.isEmpty() || ((CriterionConditionItem) this.item.get()).matches(itemstack);
        }
    }
}
