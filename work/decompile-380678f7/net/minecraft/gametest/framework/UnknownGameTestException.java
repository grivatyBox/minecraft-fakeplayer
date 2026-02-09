package net.minecraft.gametest.framework;

import net.minecraft.network.chat.IChatBaseComponent;

public class UnknownGameTestException extends GameTestException {

    private final Throwable reason;

    public UnknownGameTestException(Throwable throwable) {
        super(throwable.getMessage());
        this.reason = throwable;
    }

    @Override
    public IChatBaseComponent getDescription() {
        return IChatBaseComponent.translatable("test.error.unknown", this.reason.getMessage());
    }
}
