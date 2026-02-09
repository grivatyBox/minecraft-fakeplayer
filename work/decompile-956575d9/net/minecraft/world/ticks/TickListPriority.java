package net.minecraft.world.ticks;

import com.mojang.serialization.Codec;

public enum TickListPriority {

    EXTREMELY_HIGH(-3), VERY_HIGH(-2), HIGH(-1), NORMAL(0), LOW(1), VERY_LOW(2), EXTREMELY_LOW(3);

    public static final Codec<TickListPriority> CODEC = Codec.INT.xmap(TickListPriority::byValue, TickListPriority::getValue);
    private final int value;

    private TickListPriority(final int i) {
        this.value = i;
    }

    public static TickListPriority byValue(int i) {
        for (TickListPriority ticklistpriority : values()) {
            if (ticklistpriority.value == i) {
                return ticklistpriority;
            }
        }

        if (i < TickListPriority.EXTREMELY_HIGH.value) {
            return TickListPriority.EXTREMELY_HIGH;
        } else {
            return TickListPriority.EXTREMELY_LOW;
        }
    }

    public int getValue() {
        return this.value;
    }
}
