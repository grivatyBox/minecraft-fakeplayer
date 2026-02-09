package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public record SlotsPredicate(Map<SlotRange, CriterionConditionItem> slots) {

    public static final Codec<SlotsPredicate> CODEC = Codec.unboundedMap(SlotRanges.CODEC, CriterionConditionItem.CODEC).xmap(SlotsPredicate::new, SlotsPredicate::slots);

    public boolean matches(Entity entity) {
        for (Map.Entry<SlotRange, CriterionConditionItem> map_entry : this.slots.entrySet()) {
            if (!matchSlots(entity, (CriterionConditionItem) map_entry.getValue(), ((SlotRange) map_entry.getKey()).slots())) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchSlots(Entity entity, CriterionConditionItem criterionconditionitem, IntList intlist) {
        for (int i = 0; i < intlist.size(); ++i) {
            int j = intlist.getInt(i);
            SlotAccess slotaccess = entity.getSlot(j);

            if (criterionconditionitem.test(slotaccess.get())) {
                return true;
            }
        }

        return false;
    }
}
