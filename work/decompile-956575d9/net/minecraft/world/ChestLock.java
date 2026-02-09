package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.CriterionConditionItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;

public record ChestLock(CriterionConditionItem predicate) {

    public static final ChestLock NO_LOCK = new ChestLock(CriterionConditionItem.a.item().build());
    public static final Codec<ChestLock> CODEC = CriterionConditionItem.CODEC.xmap(ChestLock::new, ChestLock::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack itemstack) {
        return this.predicate.test(itemstack);
    }

    public void addToTag(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        if (this != ChestLock.NO_LOCK) {
            nbttagcompound.store("lock", ChestLock.CODEC, holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), this);
        }

    }

    public static ChestLock fromTag(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        return (ChestLock) nbttagcompound.read("lock", ChestLock.CODEC, holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE)).orElse(ChestLock.NO_LOCK);
    }
}
