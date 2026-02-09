package net.minecraft.gametest.framework;

import net.minecraft.network.chat.IChatBaseComponent;

public class GameTestHarnessTimeout extends GameTestException {

    protected final IChatBaseComponent message;

    public GameTestHarnessTimeout(IChatBaseComponent ichatbasecomponent) {
        super(ichatbasecomponent.getString());
        this.message = ichatbasecomponent;
    }

    @Override
    public IChatBaseComponent getDescription() {
        return this.message;
    }
}
