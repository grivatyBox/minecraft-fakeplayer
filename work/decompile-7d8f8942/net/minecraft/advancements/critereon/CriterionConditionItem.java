package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public record CriterionConditionItem(Optional<HolderSet<Item>> items, CriterionConditionValue.IntegerRange count, DataComponentMatchers components) implements Predicate<ItemStack> {

    public static final Codec<CriterionConditionItem> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(CriterionConditionItem::items), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("count", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionItem::count), DataComponentMatchers.CODEC.forGetter(CriterionConditionItem::components)).apply(instance, CriterionConditionItem::new);
    });

    public boolean test(ItemStack itemstack) {
        return this.items.isPresent() && !itemstack.is((HolderSet) this.items.get()) ? false : (!this.count.matches(itemstack.getCount()) ? false : this.components.test((DataComponentGetter) itemstack));
    }

    public static class a {

        private Optional<HolderSet<Item>> items = Optional.empty();
        private CriterionConditionValue.IntegerRange count;
        private DataComponentMatchers components;

        public a() {
            this.count = CriterionConditionValue.IntegerRange.ANY;
            this.components = DataComponentMatchers.ANY;
        }

        public static CriterionConditionItem.a item() {
            return new CriterionConditionItem.a();
        }

        public CriterionConditionItem.a of(HolderGetter<Item> holdergetter, IMaterial... aimaterial) {
            this.items = Optional.of(HolderSet.direct((imaterial) -> {
                return imaterial.asItem().builtInRegistryHolder();
            }, aimaterial));
            return this;
        }

        public CriterionConditionItem.a of(HolderGetter<Item> holdergetter, TagKey<Item> tagkey) {
            this.items = Optional.of(holdergetter.getOrThrow(tagkey));
            return this;
        }

        public CriterionConditionItem.a withCount(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.count = criterionconditionvalue_integerrange;
            return this;
        }

        public CriterionConditionItem.a withComponents(DataComponentMatchers datacomponentmatchers) {
            this.components = datacomponentmatchers;
            return this;
        }

        public CriterionConditionItem build() {
            return new CriterionConditionItem(this.items, this.count, this.components);
        }
    }
}
