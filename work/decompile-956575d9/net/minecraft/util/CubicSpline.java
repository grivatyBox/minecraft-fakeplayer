package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C, I extends ToFloatFunction<C>> extends ToFloatFunction<C> {

    @VisibleForDebug
    String parityString();

    CubicSpline<C, I> mapAll(CubicSpline.d<I> cubicspline_d);

    static <C, I extends ToFloatFunction<C>> Codec<CubicSpline<C, I>> codec(Codec<I> codec) {
        MutableObject<Codec<CubicSpline<C, I>>> mutableobject = new MutableObject();
        Codec<a<C, I>> codec1 = RecordCodecBuilder.create((instance) -> {
            RecordCodecBuilder recordcodecbuilder = Codec.FLOAT.fieldOf("location").forGetter(a::location);

            Objects.requireNonNull(mutableobject);
            return instance.group(recordcodecbuilder, Codec.lazyInitialized(mutableobject::getValue).fieldOf("value").forGetter(a::value), Codec.FLOAT.fieldOf("derivative").forGetter(a::derivative)).apply(instance, (f, cubicspline, f1) -> {
                record a<C, I extends ToFloatFunction<C>>(float location, CubicSpline<C, I> value, float derivative) {

                }

                return new a(f, cubicspline, f1);
            });
        });
        Codec<CubicSpline.e<C, I>> codec2 = RecordCodecBuilder.create((instance) -> {
            return instance.group(codec.fieldOf("coordinate").forGetter(CubicSpline.e::coordinate), ExtraCodecs.nonEmptyList(codec1.listOf()).fieldOf("points").forGetter((cubicspline_e) -> {
                return IntStream.range(0, cubicspline_e.locations.length).mapToObj((i) -> {
                    return new a(cubicspline_e.locations()[i], (CubicSpline) cubicspline_e.values().get(i), cubicspline_e.derivatives()[i]);
                }).toList();
            })).apply(instance, (tofloatfunction, list) -> {
                float[] afloat = new float[list.size()];
                ImmutableList.Builder<CubicSpline<C, I>> immutablelist_builder = ImmutableList.builder();
                float[] afloat1 = new float[list.size()];

                for (int i = 0; i < list.size(); ++i) {
                    a<C, I> a0 = (a) list.get(i);

                    afloat[i] = a0.location();
                    immutablelist_builder.add(a0.value());
                    afloat1[i] = a0.derivative();
                }

                return CubicSpline.e.create(tofloatfunction, afloat, immutablelist_builder.build(), afloat1);
            });
        });

        mutableobject.setValue(Codec.either(Codec.FLOAT, codec2).xmap((either) -> {
            return (CubicSpline) either.map(CubicSpline.c::new, (cubicspline_e) -> {
                return cubicspline_e;
            });
        }, (cubicspline) -> {
            Either either;

            if (cubicspline instanceof CubicSpline.c<C, I> cubicspline_c) {
                either = Either.left(cubicspline_c.value());
            } else {
                either = Either.right((CubicSpline.e) cubicspline);
            }

            return either;
        }));
        return (Codec) mutableobject.getValue();
    }

    static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> constant(float f) {
        return new CubicSpline.c<C, I>(f);
    }

    static <C, I extends ToFloatFunction<C>> CubicSpline.b<C, I> builder(I i0) {
        return new CubicSpline.b<C, I>(i0);
    }

    static <C, I extends ToFloatFunction<C>> CubicSpline.b<C, I> builder(I i0, ToFloatFunction<Float> tofloatfunction) {
        return new CubicSpline.b<C, I>(i0, tofloatfunction);
    }

    @VisibleForDebug
    public static record e<C, I extends ToFloatFunction<C>>(I coordinate, float[] locations, List<CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue) implements CubicSpline<C, I> {

        public e(I i0, float[] afloat, List<CubicSpline<C, I>> list, float[] afloat1, float f, float f1) {
            validateSizes(afloat, list, afloat1);
            this.coordinate = i0;
            this.locations = afloat;
            this.values = list;
            this.derivatives = afloat1;
            this.minValue = f;
            this.maxValue = f1;
        }

        static <C, I extends ToFloatFunction<C>> CubicSpline.e<C, I> create(I i0, float[] afloat, List<CubicSpline<C, I>> list, float[] afloat1) {
            validateSizes(afloat, list, afloat1);
            int i = afloat.length - 1;
            float f = Float.POSITIVE_INFINITY;
            float f1 = Float.NEGATIVE_INFINITY;
            float f2 = i0.minValue();
            float f3 = i0.maxValue();

            if (f2 < afloat[0]) {
                float f4 = linearExtend(f2, afloat, ((CubicSpline) list.get(0)).minValue(), afloat1, 0);
                float f5 = linearExtend(f2, afloat, ((CubicSpline) list.get(0)).maxValue(), afloat1, 0);

                f = Math.min(f, Math.min(f4, f5));
                f1 = Math.max(f1, Math.max(f4, f5));
            }

            if (f3 > afloat[i]) {
                float f6 = linearExtend(f3, afloat, ((CubicSpline) list.get(i)).minValue(), afloat1, i);
                float f7 = linearExtend(f3, afloat, ((CubicSpline) list.get(i)).maxValue(), afloat1, i);

                f = Math.min(f, Math.min(f6, f7));
                f1 = Math.max(f1, Math.max(f6, f7));
            }

            for (CubicSpline<C, I> cubicspline : list) {
                f = Math.min(f, cubicspline.minValue());
                f1 = Math.max(f1, cubicspline.maxValue());
            }

            for (int j = 0; j < i; ++j) {
                float f8 = afloat[j];
                float f9 = afloat[j + 1];
                float f10 = f9 - f8;
                CubicSpline<C, I> cubicspline1 = (CubicSpline) list.get(j);
                CubicSpline<C, I> cubicspline2 = (CubicSpline) list.get(j + 1);
                float f11 = cubicspline1.minValue();
                float f12 = cubicspline1.maxValue();
                float f13 = cubicspline2.minValue();
                float f14 = cubicspline2.maxValue();
                float f15 = afloat1[j];
                float f16 = afloat1[j + 1];

                if (f15 != 0.0F || f16 != 0.0F) {
                    float f17 = f15 * f10;
                    float f18 = f16 * f10;
                    float f19 = Math.min(f11, f13);
                    float f20 = Math.max(f12, f14);
                    float f21 = f17 - f14 + f11;
                    float f22 = f17 - f13 + f12;
                    float f23 = -f18 + f13 - f12;
                    float f24 = -f18 + f14 - f11;
                    float f25 = Math.min(f21, f23);
                    float f26 = Math.max(f22, f24);

                    f = Math.min(f, f19 + 0.25F * f25);
                    f1 = Math.max(f1, f20 + 0.25F * f26);
                }
            }

            return new CubicSpline.e<C, I>(i0, afloat, list, afloat1, f, f1);
        }

        private static float linearExtend(float f, float[] afloat, float f1, float[] afloat1, int i) {
            float f2 = afloat1[i];

            return f2 == 0.0F ? f1 : f1 + f2 * (f - afloat[i]);
        }

        private static <C, I extends ToFloatFunction<C>> void validateSizes(float[] afloat, List<CubicSpline<C, I>> list, float[] afloat1) {
            if (afloat.length == list.size() && afloat.length == afloat1.length) {
                if (afloat.length == 0) {
                    throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
                }
            } else {
                throw new IllegalArgumentException("All lengths must be equal, got: " + afloat.length + " " + list.size() + " " + afloat1.length);
            }
        }

        @Override
        public float apply(C c0) {
            float f = this.coordinate.apply(c0);
            int i = findIntervalStart(this.locations, f);
            int j = this.locations.length - 1;

            if (i < 0) {
                return linearExtend(f, this.locations, ((CubicSpline) this.values.get(0)).apply(c0), this.derivatives, 0);
            } else if (i == j) {
                return linearExtend(f, this.locations, ((CubicSpline) this.values.get(j)).apply(c0), this.derivatives, j);
            } else {
                float f1 = this.locations[i];
                float f2 = this.locations[i + 1];
                float f3 = (f - f1) / (f2 - f1);
                ToFloatFunction<C> tofloatfunction = (ToFloatFunction) this.values.get(i);
                ToFloatFunction<C> tofloatfunction1 = (ToFloatFunction) this.values.get(i + 1);
                float f4 = this.derivatives[i];
                float f5 = this.derivatives[i + 1];
                float f6 = tofloatfunction.apply(c0);
                float f7 = tofloatfunction1.apply(c0);
                float f8 = f4 * (f2 - f1) - (f7 - f6);
                float f9 = -f5 * (f2 - f1) + (f7 - f6);
                float f10 = MathHelper.lerp(f3, f6, f7) + f3 * (1.0F - f3) * MathHelper.lerp(f3, f8, f9);

                return f10;
            }
        }

        private static int findIntervalStart(float[] afloat, float f) {
            return MathHelper.binarySearch(0, afloat.length, (i) -> {
                return f < afloat[i];
            }) - 1;
        }

        @VisibleForTesting
        @Override
        public String parityString() {
            String s = String.valueOf(this.coordinate);

            return "Spline{coordinate=" + s + ", locations=" + this.toString(this.locations) + ", derivatives=" + this.toString(this.derivatives) + ", values=" + (String) this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]")) + "}";
        }

        private String toString(float[] afloat) {
            Stream stream = IntStream.range(0, afloat.length).mapToDouble((i) -> {
                return (double) afloat[i];
            }).mapToObj((d0) -> {
                return String.format(Locale.ROOT, "%.3f", d0);
            });

            return "[" + (String) stream.collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public CubicSpline<C, I> mapAll(CubicSpline.d<I> cubicspline_d) {
            return create(cubicspline_d.visit(this.coordinate), this.locations, this.values().stream().map((cubicspline) -> {
                return cubicspline.mapAll(cubicspline_d);
            }).toList(), this.derivatives);
        }
    }

    @VisibleForDebug
    public static record c<C, I extends ToFloatFunction<C>>(float value) implements CubicSpline<C, I> {

        @Override
        public float apply(C c0) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format(Locale.ROOT, "k=%.3f", this.value);
        }

        @Override
        public float minValue() {
            return this.value;
        }

        @Override
        public float maxValue() {
            return this.value;
        }

        @Override
        public CubicSpline<C, I> mapAll(CubicSpline.d<I> cubicspline_d) {
            return this;
        }
    }

    public static final class b<C, I extends ToFloatFunction<C>> {

        private final I coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final FloatList locations;
        private final List<CubicSpline<C, I>> values;
        private final FloatList derivatives;

        protected b(I i0) {
            this(i0, ToFloatFunction.IDENTITY);
        }

        protected b(I i0, ToFloatFunction<Float> tofloatfunction) {
            this.locations = new FloatArrayList();
            this.values = Lists.newArrayList();
            this.derivatives = new FloatArrayList();
            this.coordinate = i0;
            this.valueTransformer = tofloatfunction;
        }

        public CubicSpline.b<C, I> addPoint(float f, float f1) {
            return this.addPoint(f, new CubicSpline.c(this.valueTransformer.apply(f1)), 0.0F);
        }

        public CubicSpline.b<C, I> addPoint(float f, float f1, float f2) {
            return this.addPoint(f, new CubicSpline.c(this.valueTransformer.apply(f1)), f2);
        }

        public CubicSpline.b<C, I> addPoint(float f, CubicSpline<C, I> cubicspline) {
            return this.addPoint(f, cubicspline, 0.0F);
        }

        private CubicSpline.b<C, I> addPoint(float f, CubicSpline<C, I> cubicspline, float f1) {
            if (!this.locations.isEmpty() && f <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            } else {
                this.locations.add(f);
                this.values.add(cubicspline);
                this.derivatives.add(f1);
                return this;
            }
        }

        public CubicSpline<C, I> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            } else {
                return CubicSpline.e.<C, I>create(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
            }
        }
    }

    public interface d<I> {

        I visit(I i0);
    }
}
