package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface BoundedFloatFunction<C> {

    BoundedFloatFunction<Float> IDENTITY = createUnlimited((f) -> {
        return f;
    });

    float apply(C c0);

    float minValue();

    float maxValue();

    static BoundedFloatFunction<Float> createUnlimited(final Float2FloatFunction float2floatfunction) {
        return new BoundedFloatFunction<Float>() {
            public float apply(Float ofloat) {
                return (Float) float2floatfunction.apply(ofloat);
            }

            @Override
            public float minValue() {
                return Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return Float.POSITIVE_INFINITY;
            }
        };
    }

    default <C2> BoundedFloatFunction<C2> comap(final Function<C2, C> function) {
        return new BoundedFloatFunction<C2>() {
            @Override
            public float apply(C2 c2) {
                return BoundedFloatFunction.this.apply(function.apply(c2));
            }

            @Override
            public float minValue() {
                return BoundedFloatFunction.this.minValue();
            }

            @Override
            public float maxValue() {
                return BoundedFloatFunction.this.maxValue();
            }
        };
    }
}
