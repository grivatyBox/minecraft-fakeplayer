package net.minecraft.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.network.codec.StreamCodec;

public record Vector3f(float x, float y, float z) {

    public static final Codec<Vector3f> CODEC = Codec.FLOAT.listOf().comapFlatMap((list) -> {
        return SystemUtils.fixedSize(list, 3).map((list1) -> {
            return new Vector3f((Float) list1.get(0), (Float) list1.get(1), (Float) list1.get(2));
        });
    }, (vector3f) -> {
        return List.of(vector3f.x(), vector3f.y(), vector3f.z());
    });
    public static final StreamCodec<ByteBuf, Vector3f> STREAM_CODEC = new StreamCodec<ByteBuf, Vector3f>() {
        public Vector3f decode(ByteBuf bytebuf) {
            return new Vector3f(bytebuf.readFloat(), bytebuf.readFloat(), bytebuf.readFloat());
        }

        public void encode(ByteBuf bytebuf, Vector3f vector3f) {
            bytebuf.writeFloat(vector3f.x);
            bytebuf.writeFloat(vector3f.y);
            bytebuf.writeFloat(vector3f.z);
        }
    };

    public Vector3f(float f, float f1, float f2) {
        f = !Float.isInfinite(f) && !Float.isNaN(f) ? f % 360.0F : 0.0F;
        f1 = !Float.isInfinite(f1) && !Float.isNaN(f1) ? f1 % 360.0F : 0.0F;
        f2 = !Float.isInfinite(f2) && !Float.isNaN(f2) ? f2 % 360.0F : 0.0F;
        this.x = f;
        this.y = f1;
        this.z = f2;
    }
}
