package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.Vec3D;

public class LpVec3 {

    private static final int DATA_BITS = 15;
    private static final int DATA_BITS_MASK = 32767;
    private static final double MAX_QUANTIZED_VALUE = 32766.0D;
    private static final int SCALE_BITS = 2;
    private static final int SCALE_BITS_MASK = 3;
    private static final int CONTINUATION_FLAG = 4;
    private static final int X_OFFSET = 3;
    private static final int Y_OFFSET = 18;
    private static final int Z_OFFSET = 33;
    public static final double ABS_MAX_VALUE = 1.7179869183E10D;
    public static final double ABS_MIN_VALUE = 3.051944088384301E-5D;

    public LpVec3() {}

    public static boolean hasContinuationBit(int i) {
        return (i & 4) == 4;
    }

    public static Vec3D read(ByteBuf bytebuf) {
        int i = bytebuf.readUnsignedByte();

        if (i == 0) {
            return Vec3D.ZERO;
        } else {
            int j = bytebuf.readUnsignedByte();
            long k = bytebuf.readUnsignedInt();
            long l = k << 16 | (long) (j << 8) | (long) i;
            long i1 = (long) (i & 3);

            if (hasContinuationBit(i)) {
                i1 |= ((long) VarInt.read(bytebuf) & 4294967295L) << 2;
            }

            return new Vec3D(unpack(l >> 3) * (double) i1, unpack(l >> 18) * (double) i1, unpack(l >> 33) * (double) i1);
        }
    }

    public static void write(ByteBuf bytebuf, Vec3D vec3d) {
        double d0 = sanitize(vec3d.x);
        double d1 = sanitize(vec3d.y);
        double d2 = sanitize(vec3d.z);
        double d3 = MathHelper.absMax(d0, MathHelper.absMax(d1, d2));

        if (d3 < 3.051944088384301E-5D) {
            bytebuf.writeByte(0);
        } else {
            long i = MathHelper.ceilLong(d3);
            boolean flag = (i & 3L) != i;
            long j = flag ? i & 3L | 4L : i;
            long k = pack(d0 / (double) i) << 3;
            long l = pack(d1 / (double) i) << 18;
            long i1 = pack(d2 / (double) i) << 33;
            long j1 = j | k | l | i1;

            bytebuf.writeByte((byte) ((int) j1));
            bytebuf.writeByte((byte) ((int) (j1 >> 8)));
            bytebuf.writeInt((int) (j1 >> 16));
            if (flag) {
                VarInt.write(bytebuf, (int) (i >> 2));
            }

        }
    }

    private static double sanitize(double d0) {
        return Double.isNaN(d0) ? 0.0D : Math.clamp(d0, -1.7179869183E10D, 1.7179869183E10D);
    }

    private static long pack(double d0) {
        return Math.round((d0 * 0.5D + 0.5D) * 32766.0D);
    }

    private static double unpack(long i) {
        return Math.min((double) (i & 32767L), 32766.0D) * 2.0D / 32766.0D - 1.0D;
    }
}
