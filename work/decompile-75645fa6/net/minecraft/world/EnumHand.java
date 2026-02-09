package net.minecraft.world;

import net.minecraft.world.entity.EnumItemSlot;

public enum EnumHand {

    MAIN_HAND, OFF_HAND;

    private EnumHand() {}

    public EnumItemSlot asEquipmentSlot() {
        return this == EnumHand.MAIN_HAND ? EnumItemSlot.MAINHAND : EnumItemSlot.OFFHAND;
    }
}
