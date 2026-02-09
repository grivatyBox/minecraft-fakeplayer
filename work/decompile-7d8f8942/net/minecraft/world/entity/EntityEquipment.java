package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public class EntityEquipment {

    public static final Codec<EntityEquipment> CODEC = Codec.unboundedMap(EnumItemSlot.CODEC, ItemStack.CODEC).xmap((map) -> {
        EnumMap<EnumItemSlot, ItemStack> enummap = new EnumMap(EnumItemSlot.class);

        enummap.putAll(map);
        return new EntityEquipment(enummap);
    }, (entityequipment) -> {
        Map<EnumItemSlot, ItemStack> map = new EnumMap(entityequipment.items);

        map.values().removeIf(ItemStack::isEmpty);
        return map;
    });
    private final EnumMap<EnumItemSlot, ItemStack> items;

    private EntityEquipment(EnumMap<EnumItemSlot, ItemStack> enummap) {
        this.items = enummap;
    }

    public EntityEquipment() {
        this(new EnumMap(EnumItemSlot.class));
    }

    public ItemStack set(EnumItemSlot enumitemslot, ItemStack itemstack) {
        itemstack.getItem().verifyComponentsAfterLoad(itemstack);
        return (ItemStack) Objects.requireNonNullElse((ItemStack) this.items.put(enumitemslot, itemstack), ItemStack.EMPTY);
    }

    public ItemStack get(EnumItemSlot enumitemslot) {
        return (ItemStack) this.items.getOrDefault(enumitemslot, ItemStack.EMPTY);
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.items.values()) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void tick(Entity entity) {
        for (Map.Entry<EnumItemSlot, ItemStack> map_entry : this.items.entrySet()) {
            ItemStack itemstack = (ItemStack) map_entry.getValue();

            if (!itemstack.isEmpty()) {
                itemstack.inventoryTick(entity.level(), entity, (EnumItemSlot) map_entry.getKey());
            }
        }

    }

    public void setAll(EntityEquipment entityequipment) {
        this.items.clear();
        this.items.putAll(entityequipment.items);
    }

    public void dropAll(EntityLiving entityliving) {
        for (ItemStack itemstack : this.items.values()) {
            entityliving.drop(itemstack, true, false);
        }

        this.clear();
    }

    public void clear() {
        this.items.replaceAll((enumitemslot, itemstack) -> {
            return ItemStack.EMPTY;
        });
    }
}
