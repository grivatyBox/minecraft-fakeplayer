package net.minecraft.world.entity.ai.attributes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public record AttributeModifier(MinecraftKey id, double amount, AttributeModifier.Operation operation) {

    public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("id").forGetter(AttributeModifier::id), Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount), AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)).apply(instance, AttributeModifier::new);
    });
    public static final Codec<AttributeModifier> CODEC = AttributeModifier.MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AttributeModifier> STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, AttributeModifier::id, ByteBufCodecs.DOUBLE, AttributeModifier::amount, AttributeModifier.Operation.STREAM_CODEC, AttributeModifier::operation, AttributeModifier::new);

    public boolean is(MinecraftKey minecraftkey) {
        return minecraftkey.equals(this.id);
    }

    public static enum Operation implements INamable {

        ADD_VALUE("add_value", 0), ADD_MULTIPLIED_BASE("add_multiplied_base", 1), ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

        public static final IntFunction<AttributeModifier.Operation> BY_ID = ByIdMap.<AttributeModifier.Operation>continuous(AttributeModifier.Operation::id, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, AttributeModifier.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(AttributeModifier.Operation.BY_ID, AttributeModifier.Operation::id);
        public static final Codec<AttributeModifier.Operation> CODEC = INamable.<AttributeModifier.Operation>fromEnum(AttributeModifier.Operation::values);
        private final String name;
        private final int id;

        private Operation(final String s, final int i) {
            this.name = s;
            this.id = i;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
