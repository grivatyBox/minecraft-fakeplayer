package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum Relative {

    X(0), Y(1), Z(2), Y_ROT(3), X_ROT(4), DELTA_X(5), DELTA_Y(6), DELTA_Z(7), ROTATE_DELTA(8);

    public static final Set<Relative> ALL = Set.of(values());
    public static final Set<Relative> ROTATION = Set.of(Relative.X_ROT, Relative.Y_ROT);
    public static final Set<Relative> DELTA = Set.of(Relative.DELTA_X, Relative.DELTA_Y, Relative.DELTA_Z, Relative.ROTATE_DELTA);
    public static final StreamCodec<ByteBuf, Set<Relative>> SET_STREAM_CODEC = ByteBufCodecs.INT.map(Relative::unpack, Relative::pack);
    private final int bit;

    @SafeVarargs
    public static Set<Relative> union(Set<Relative>... aset) {
        HashSet<Relative> hashset = new HashSet();

        for (Set<Relative> set : aset) {
            hashset.addAll(set);
        }

        return hashset;
    }

    public static Set<Relative> rotation(boolean flag, boolean flag1) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);

        if (flag) {
            set.add(Relative.Y_ROT);
        }

        if (flag1) {
            set.add(Relative.X_ROT);
        }

        return set;
    }

    public static Set<Relative> position(boolean flag, boolean flag1, boolean flag2) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);

        if (flag) {
            set.add(Relative.X);
        }

        if (flag1) {
            set.add(Relative.Y);
        }

        if (flag2) {
            set.add(Relative.Z);
        }

        return set;
    }

    public static Set<Relative> direction(boolean flag, boolean flag1, boolean flag2) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);

        if (flag) {
            set.add(Relative.DELTA_X);
        }

        if (flag1) {
            set.add(Relative.DELTA_Y);
        }

        if (flag2) {
            set.add(Relative.DELTA_Z);
        }

        return set;
    }

    private Relative(final int i) {
        this.bit = i;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int i) {
        return (i & this.getMask()) == this.getMask();
    }

    public static Set<Relative> unpack(int i) {
        Set<Relative> set = EnumSet.noneOf(Relative.class);

        for (Relative relative : values()) {
            if (relative.isSet(i)) {
                set.add(relative);
            }
        }

        return set;
    }

    public static int pack(Set<Relative> set) {
        int i = 0;

        for (Relative relative : set) {
            i |= relative.getMask();
        }

        return i;
    }
}
