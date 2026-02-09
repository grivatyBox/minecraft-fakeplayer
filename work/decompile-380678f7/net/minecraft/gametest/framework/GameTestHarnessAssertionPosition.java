package net.minecraft.gametest.framework;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;

public class GameTestHarnessAssertionPosition extends GameTestHarnessAssertion {

    private final BlockPosition absolutePos;
    private final BlockPosition relativePos;

    public GameTestHarnessAssertionPosition(IChatBaseComponent ichatbasecomponent, BlockPosition blockposition, BlockPosition blockposition1, int i) {
        super(ichatbasecomponent, i);
        this.absolutePos = blockposition;
        this.relativePos = blockposition1;
    }

    @Override
    public IChatBaseComponent getDescription() {
        return IChatBaseComponent.translatable("test.error.position", this.message, this.absolutePos.getX(), this.absolutePos.getY(), this.absolutePos.getZ(), this.relativePos.getX(), this.relativePos.getY(), this.relativePos.getZ(), this.tick);
    }

    @Nullable
    public String getMessageToShowAtBlock() {
        return super.getMessage();
    }

    @Nullable
    public BlockPosition getRelativePos() {
        return this.relativePos;
    }

    @Nullable
    public BlockPosition getAbsolutePos() {
        return this.absolutePos;
    }
}
