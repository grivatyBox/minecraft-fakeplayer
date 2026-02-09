package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.CriterionConditionItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record ChestLock(CriterionConditionItem predicate) {

    public static final ChestLock NO_LOCK = new ChestLock(CriterionConditionItem.a.item().build());
    public static final Codec<ChestLock> CODEC = CriterionConditionItem.CODEC.xmap(ChestLock::new, ChestLock::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack itemstack) {
        return this.predicate.test(itemstack);
    }

    public void addToTag(ValueOutput valueoutput) {
        if (this != ChestLock.NO_LOCK) {
            valueoutput.store("lock", ChestLock.CODEC, this);
        }

    }

    public static ChestLock fromTag(ValueInput valueinput) {
        return (ChestLock) valueinput.read("lock", ChestLock.CODEC).orElse(ChestLock.NO_LOCK);
    }
}
