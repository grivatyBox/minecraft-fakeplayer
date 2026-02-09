package net.minecraft.util.random;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.util.ExtraCodecs;
import org.slf4j.Logger;

public record Weighted<T>(T value, int weight) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public Weighted(T t0, int i) {
        if (i < 0) {
            throw (IllegalArgumentException) SystemUtils.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
        } else {
            if (i == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
                Weighted.LOGGER.warn("Found 0 weight, make sure this is intentional!");
            }

            this.value = t0;
            this.weight = i;
        }
    }

    public static <E> Codec<Weighted<E>> codec(Codec<E> codec) {
        return codec(codec.fieldOf("data"));
    }

    public static <E> Codec<Weighted<E>> codec(MapCodec<E> mapcodec) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(mapcodec.forGetter(Weighted::value), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(Weighted::weight)).apply(instance, Weighted::new);
        });
    }

    public <U> Weighted<U> map(Function<T, U> function) {
        return new Weighted<U>(function.apply(this.value()), this.weight);
    }
}
