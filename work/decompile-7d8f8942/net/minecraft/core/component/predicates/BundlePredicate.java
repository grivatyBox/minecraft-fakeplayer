package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.CriterionConditionItem;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public record BundlePredicate(Optional<CollectionPredicate<ItemStack, CriterionConditionItem>> items) implements SingleComponentItemPredicate<BundleContents> {

    public static final Codec<BundlePredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(CriterionConditionItem.CODEC).optionalFieldOf("items").forGetter(BundlePredicate::items)).apply(instance, BundlePredicate::new);
    });

    @Override
    public DataComponentType<BundleContents> componentType() {
        return DataComponents.BUNDLE_CONTENTS;
    }

    public boolean matches(BundleContents bundlecontents) {
        return !this.items.isPresent() || ((CollectionPredicate) this.items.get()).test(bundlecontents.items());
    }
}
