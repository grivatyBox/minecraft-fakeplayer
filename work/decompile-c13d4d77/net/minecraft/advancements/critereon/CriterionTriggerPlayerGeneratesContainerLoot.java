package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ExtraCodecs;

public class CriterionTriggerPlayerGeneratesContainerLoot extends CriterionTriggerAbstract<CriterionTriggerPlayerGeneratesContainerLoot.a> {

    public CriterionTriggerPlayerGeneratesContainerLoot() {}

    @Override
    public Codec<CriterionTriggerPlayerGeneratesContainerLoot.a> codec() {
        return CriterionTriggerPlayerGeneratesContainerLoot.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, MinecraftKey minecraftkey) {
        this.trigger(entityplayer, (criteriontriggerplayergeneratescontainerloot_a) -> {
            return criteriontriggerplayergeneratescontainerloot_a.matches(minecraftkey);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, MinecraftKey lootTable) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerPlayerGeneratesContainerLoot.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionEntity.ADVANCEMENT_CODEC, "player").forGetter(CriterionTriggerPlayerGeneratesContainerLoot.a::player), MinecraftKey.CODEC.fieldOf("loot_table").forGetter(CriterionTriggerPlayerGeneratesContainerLoot.a::lootTable)).apply(instance, CriterionTriggerPlayerGeneratesContainerLoot.a::new);
        });

        public static Criterion<CriterionTriggerPlayerGeneratesContainerLoot.a> lootTableUsed(MinecraftKey minecraftkey) {
            return CriterionTriggers.GENERATE_LOOT.createCriterion(new CriterionTriggerPlayerGeneratesContainerLoot.a(Optional.empty(), minecraftkey));
        }

        public boolean matches(MinecraftKey minecraftkey) {
            return this.lootTable.equals(minecraftkey);
        }
    }
}
