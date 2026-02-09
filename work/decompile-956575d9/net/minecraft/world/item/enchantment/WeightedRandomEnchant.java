package net.minecraft.world.item.enchantment;

import net.minecraft.core.Holder;

public record WeightedRandomEnchant(Holder<Enchantment> enchantment, int level) {

    public int weight() {
        return ((Enchantment) this.enchantment().value()).getWeight();
    }
}
