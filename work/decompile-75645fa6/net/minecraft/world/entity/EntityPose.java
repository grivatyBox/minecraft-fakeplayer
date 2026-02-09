package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public enum EntityPose implements INamable {

    STANDING(0, "standing"), FALL_FLYING(1, "fall_flying"), SLEEPING(2, "sleeping"), SWIMMING(3, "swimming"), SPIN_ATTACK(4, "spin_attack"), CROUCHING(5, "crouching"), LONG_JUMPING(6, "long_jumping"), DYING(7, "dying"), CROAKING(8, "croaking"), USING_TONGUE(9, "using_tongue"), SITTING(10, "sitting"), ROARING(11, "roaring"), SNIFFING(12, "sniffing"), EMERGING(13, "emerging"), DIGGING(14, "digging"), SLIDING(15, "sliding"), SHOOTING(16, "shooting"), INHALING(17, "inhaling");

    public static final IntFunction<EntityPose> BY_ID = ByIdMap.<EntityPose>continuous(EntityPose::id, values(), ByIdMap.a.ZERO);
    public static final Codec<EntityPose> CODEC = INamable.<EntityPose>fromEnum(EntityPose::values);
    public static final StreamCodec<ByteBuf, EntityPose> STREAM_CODEC = ByteBufCodecs.idMapper(EntityPose.BY_ID, EntityPose::id);
    private final int id;
    private final String name;

    private EntityPose(final int i, final String s) {
        this.id = i;
        this.name = s;
    }

    public int id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
