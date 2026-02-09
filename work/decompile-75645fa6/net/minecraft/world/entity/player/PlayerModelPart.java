package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.INamable;

public enum PlayerModelPart implements INamable {

    CAPE(0, "cape"), JACKET(1, "jacket"), LEFT_SLEEVE(2, "left_sleeve"), RIGHT_SLEEVE(3, "right_sleeve"), LEFT_PANTS_LEG(4, "left_pants_leg"), RIGHT_PANTS_LEG(5, "right_pants_leg"), HAT(6, "hat");

    public static final Codec<PlayerModelPart> CODEC = INamable.<PlayerModelPart>fromEnum(PlayerModelPart::values);
    private final int bit;
    private final int mask;
    private final String id;
    private final IChatBaseComponent name;

    private PlayerModelPart(final int i, final String s) {
        this.bit = i;
        this.mask = 1 << i;
        this.id = s;
        this.name = IChatBaseComponent.translatable("options.modelPart." + s);
    }

    public int getMask() {
        return this.mask;
    }

    public int getBit() {
        return this.bit;
    }

    public String getId() {
        return this.id;
    }

    public IChatBaseComponent getName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
