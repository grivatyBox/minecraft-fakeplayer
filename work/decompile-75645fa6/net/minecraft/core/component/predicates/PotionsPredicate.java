package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;

public record PotionsPredicate(HolderSet<PotionRegistry> potions) implements SingleComponentItemPredicate<PotionContents> {

    public static final Codec<PotionsPredicate> CODEC = RegistryCodecs.homogeneousList(Registries.POTION).xmap(PotionsPredicate::new, PotionsPredicate::potions);

    @Override
    public DataComponentType<PotionContents> componentType() {
        return DataComponents.POTION_CONTENTS;
    }

    public boolean matches(PotionContents potioncontents) {
        Optional<Holder<PotionRegistry>> optional = potioncontents.potion();

        return !optional.isEmpty() && this.potions.contains((Holder) optional.get());
    }

    public static DataComponentPredicate potions(HolderSet<PotionRegistry> holderset) {
        return new PotionsPredicate(holderset);
    }
}
