package net.minecraft.world.level.block.state.properties;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public enum TestBlockMode implements INamable {

    START(0, "start"), LOG(1, "log"), FAIL(2, "fail"), ACCEPT(3, "accept");

    private static final IntFunction<TestBlockMode> BY_ID = ByIdMap.<TestBlockMode>continuous((testblockmode) -> {
        return testblockmode.id;
    }, values(), ByIdMap.a.ZERO);
    public static final Codec<TestBlockMode> CODEC = INamable.<TestBlockMode>fromEnum(TestBlockMode::values);
    public static final StreamCodec<ByteBuf, TestBlockMode> STREAM_CODEC = ByteBufCodecs.idMapper(TestBlockMode.BY_ID, (testblockmode) -> {
        return testblockmode.id;
    });
    private final int id;
    private final String name;
    private final IChatBaseComponent displayName;
    private final IChatBaseComponent detailedMessage;

    private TestBlockMode(final int i, final String s) {
        this.id = i;
        this.name = s;
        this.displayName = IChatBaseComponent.translatable("test_block.mode." + s);
        this.detailedMessage = IChatBaseComponent.translatable("test_block.mode_info." + s);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public IChatBaseComponent getDetailedMessage() {
        return this.detailedMessage;
    }
}
