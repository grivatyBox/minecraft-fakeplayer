package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.INamable;

public enum CreakingHeartState implements INamable {

    UPROOTED("uprooted"), DORMANT("dormant"), AWAKE("awake");

    private final String name;

    private CreakingHeartState(final String s) {
        this.name = s;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
