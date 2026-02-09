package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.INamable;

public enum PlayerModelType implements INamable {

    SLIM("slim", "slim"), WIDE("wide", "default");

    public static final Codec<PlayerModelType> CODEC = INamable.<PlayerModelType>fromEnum(PlayerModelType::values);
    private static final Function<String, PlayerModelType> NAME_LOOKUP = INamable.createNameLookup(values(), (playermodeltype) -> {
        return playermodeltype.legacyServicesId;
    });
    public static final StreamCodec<ByteBuf, PlayerModelType> STREAM_CODEC = ByteBufCodecs.BOOL.map((obool) -> {
        return obool ? PlayerModelType.SLIM : PlayerModelType.WIDE;
    }, (playermodeltype) -> {
        return playermodeltype == PlayerModelType.SLIM;
    });
    private final String id;
    private final String legacyServicesId;

    private PlayerModelType(final String s, final String s1) {
        this.id = s;
        this.legacyServicesId = s1;
    }

    public static PlayerModelType byLegacyServicesName(@Nullable String s) {
        return (PlayerModelType) Objects.requireNonNullElse((PlayerModelType) PlayerModelType.NAME_LOOKUP.apply(s), PlayerModelType.WIDE);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
