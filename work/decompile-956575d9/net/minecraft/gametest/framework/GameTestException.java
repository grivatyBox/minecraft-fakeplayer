package net.minecraft.gametest.framework;

import net.minecraft.network.chat.IChatBaseComponent;

public abstract class GameTestException extends RuntimeException {

    public GameTestException(String s) {
        super(s);
    }

    public abstract IChatBaseComponent getDescription();
}
