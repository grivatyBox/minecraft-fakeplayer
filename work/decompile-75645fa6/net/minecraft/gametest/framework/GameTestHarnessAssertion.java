package net.minecraft.gametest.framework;

import net.minecraft.network.chat.IChatBaseComponent;

public class GameTestHarnessAssertion extends GameTestException {

    protected final IChatBaseComponent message;
    protected final int tick;

    public GameTestHarnessAssertion(IChatBaseComponent ichatbasecomponent, int i) {
        super(ichatbasecomponent.getString());
        this.message = ichatbasecomponent;
        this.tick = i;
    }

    @Override
    public IChatBaseComponent getDescription() {
        return IChatBaseComponent.translatable("test.error.tick", this.message, this.tick);
    }

    public String getMessage() {
        return this.getDescription().getString();
    }
}
