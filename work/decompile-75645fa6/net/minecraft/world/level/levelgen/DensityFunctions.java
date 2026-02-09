package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NoiseGenerator3Handler;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorNormal;
import org.slf4j.Logger;

public final class DensityFunctions {

    private static final Codec<DensityFunction> CODEC = BuiltInRegistries.DENSITY_FUNCTION_TYPE.byNameCodec().dispatch((densityfunction) -> {
        return densityfunction.codec().codec();
    }, Function.identity());
    protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0D;
    static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange(-1000000.0D, 1000000.0D);
    public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.CODEC).xmap((either) -> {
        return (DensityFunction) either.map(DensityFunctions::constant, Function.identity());
    }, (densityfunction) -> {
        if (densityfunction instanceof DensityFunctions.h densityfunctions_h) {
            return Either.left(densityfunctions_h.value());
        } else {
            return Either.right(densityfunction);
        }
    });

    public static MapCodec<? extends DensityFunction> bootstrap(IRegistry<MapCodec<? extends DensityFunction>> iregistry) {
        register(iregistry, "blend_alpha", DensityFunctions.d.CODEC);
        register(iregistry, "blend_offset", DensityFunctions.f.CODEC);
        register(iregistry, "beardifier", DensityFunctions.b.CODEC);
        register(iregistry, "old_blended_noise", BlendedNoise.CODEC);

        for (DensityFunctions.m.a densityfunctions_m_a : DensityFunctions.m.a.values()) {
            register(iregistry, densityfunctions_m_a.getSerializedName(), densityfunctions_m_a.codec);
        }

        register(iregistry, "noise", DensityFunctions.p.CODEC);
        register(iregistry, "end_islands", DensityFunctions.i.CODEC);
        register(iregistry, "weird_scaled_sampler", DensityFunctions.aa.CODEC);
        register(iregistry, "shifted_noise", DensityFunctions.w.CODEC);
        register(iregistry, "range_choice", DensityFunctions.r.CODEC);
        register(iregistry, "shift_a", DensityFunctions.t.CODEC);
        register(iregistry, "shift_b", DensityFunctions.u.CODEC);
        register(iregistry, "shift", DensityFunctions.s.CODEC);
        register(iregistry, "blend_density", DensityFunctions.e.CODEC);
        register(iregistry, "clamp", DensityFunctions.g.CODEC);

        for (DensityFunctions.l.a densityfunctions_l_a : DensityFunctions.l.a.values()) {
            register(iregistry, densityfunctions_l_a.getSerializedName(), densityfunctions_l_a.codec);
        }

        for (DensityFunctions.z.a densityfunctions_z_a : DensityFunctions.z.a.values()) {
            register(iregistry, densityfunctions_z_a.getSerializedName(), densityfunctions_z_a.codec);
        }

        register(iregistry, "spline", DensityFunctions.x.CODEC);
        register(iregistry, "constant", DensityFunctions.h.CODEC);
        register(iregistry, "y_clamped_gradient", DensityFunctions.ab.CODEC);
        return register(iregistry, "find_top_surface", DensityFunctions.j.CODEC);
    }

    private static MapCodec<? extends DensityFunction> register(IRegistry<MapCodec<? extends DensityFunction>> iregistry, String s, KeyDispatchDataCodec<? extends DensityFunction> keydispatchdatacodec) {
        return (MapCodec) IRegistry.register(iregistry, s, keydispatchdatacodec.codec());
    }

    static <A, O> KeyDispatchDataCodec<O> singleArgumentCodec(Codec<A> codec, Function<A, O> function, Function<O, A> function1) {
        return KeyDispatchDataCodec.<O>of(codec.fieldOf("argument").xmap(function, function1));
    }

    static <O> KeyDispatchDataCodec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> function, Function<O, DensityFunction> function1) {
        return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, function, function1);
    }

    static <O> KeyDispatchDataCodec<O> doubleFunctionArgumentCodec(BiFunction<DensityFunction, DensityFunction, O> bifunction, Function<O, DensityFunction> function, Function<O, DensityFunction> function1) {
        return KeyDispatchDataCodec.<O>of(RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(function), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(function1)).apply(instance, bifunction);
        }));
    }

    static <O> KeyDispatchDataCodec<O> makeCodec(MapCodec<O> mapcodec) {
        return KeyDispatchDataCodec.<O>of(mapcodec);
    }

    private DensityFunctions() {}

    public static DensityFunction interpolated(DensityFunction densityfunction) {
        return new DensityFunctions.m(DensityFunctions.m.a.Interpolated, densityfunction);
    }

    public static DensityFunction flatCache(DensityFunction densityfunction) {
        return new DensityFunctions.m(DensityFunctions.m.a.FlatCache, densityfunction);
    }

    public static DensityFunction cache2d(DensityFunction densityfunction) {
        return new DensityFunctions.m(DensityFunctions.m.a.Cache2D, densityfunction);
    }

    public static DensityFunction cacheOnce(DensityFunction densityfunction) {
        return new DensityFunctions.m(DensityFunctions.m.a.CacheOnce, densityfunction);
    }

    public static DensityFunction cacheAllInCell(DensityFunction densityfunction) {
        return new DensityFunctions.m(DensityFunctions.m.a.CacheAllInCell, densityfunction);
    }

    public static DensityFunction mappedNoise(Holder<NoiseGeneratorNormal.a> holder, @Deprecated double d0, double d1, double d2, double d3) {
        return mapFromUnitTo(new DensityFunctions.p(new DensityFunction.c(holder), d0, d1), d2, d3);
    }

    public static DensityFunction mappedNoise(Holder<NoiseGeneratorNormal.a> holder, double d0, double d1, double d2) {
        return mappedNoise(holder, 1.0D, d0, d1, d2);
    }

    public static DensityFunction mappedNoise(Holder<NoiseGeneratorNormal.a> holder, double d0, double d1) {
        return mappedNoise(holder, 1.0D, 1.0D, d0, d1);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction densityfunction, DensityFunction densityfunction1, double d0, Holder<NoiseGeneratorNormal.a> holder) {
        return new DensityFunctions.w(densityfunction, zero(), densityfunction1, d0, 0.0D, new DensityFunction.c(holder));
    }

    public static DensityFunction noise(Holder<NoiseGeneratorNormal.a> holder) {
        return noise(holder, 1.0D, 1.0D);
    }

    public static DensityFunction noise(Holder<NoiseGeneratorNormal.a> holder, double d0, double d1) {
        return new DensityFunctions.p(new DensityFunction.c(holder), d0, d1);
    }

    public static DensityFunction noise(Holder<NoiseGeneratorNormal.a> holder, double d0) {
        return noise(holder, 1.0D, d0);
    }

    public static DensityFunction rangeChoice(DensityFunction densityfunction, double d0, double d1, DensityFunction densityfunction1, DensityFunction densityfunction2) {
        return new DensityFunctions.r(densityfunction, d0, d1, densityfunction1, densityfunction2);
    }

    public static DensityFunction shiftA(Holder<NoiseGeneratorNormal.a> holder) {
        return new DensityFunctions.t(new DensityFunction.c(holder));
    }

    public static DensityFunction shiftB(Holder<NoiseGeneratorNormal.a> holder) {
        return new DensityFunctions.u(new DensityFunction.c(holder));
    }

    public static DensityFunction shift(Holder<NoiseGeneratorNormal.a> holder) {
        return new DensityFunctions.s(new DensityFunction.c(holder));
    }

    public static DensityFunction blendDensity(DensityFunction densityfunction) {
        return new DensityFunctions.e(densityfunction);
    }

    public static DensityFunction endIslands(long i) {
        return new DensityFunctions.i(i);
    }

    public static DensityFunction weirdScaledSampler(DensityFunction densityfunction, Holder<NoiseGeneratorNormal.a> holder, DensityFunctions.aa.a densityfunctions_aa_a) {
        return new DensityFunctions.aa(densityfunction, new DensityFunction.c(holder), densityfunctions_aa_a);
    }

    public static DensityFunction add(DensityFunction densityfunction, DensityFunction densityfunction1) {
        return DensityFunctions.z.create(DensityFunctions.z.a.ADD, densityfunction, densityfunction1);
    }

    public static DensityFunction mul(DensityFunction densityfunction, DensityFunction densityfunction1) {
        return DensityFunctions.z.create(DensityFunctions.z.a.MUL, densityfunction, densityfunction1);
    }

    public static DensityFunction min(DensityFunction densityfunction, DensityFunction densityfunction1) {
        return DensityFunctions.z.create(DensityFunctions.z.a.MIN, densityfunction, densityfunction1);
    }

    public static DensityFunction max(DensityFunction densityfunction, DensityFunction densityfunction1) {
        return DensityFunctions.z.create(DensityFunctions.z.a.MAX, densityfunction, densityfunction1);
    }

    public static DensityFunction spline(CubicSpline<DensityFunctions.x.b, DensityFunctions.x.a> cubicspline) {
        return new DensityFunctions.x(cubicspline);
    }

    public static DensityFunction zero() {
        return DensityFunctions.h.ZERO;
    }

    public static DensityFunction constant(double d0) {
        return new DensityFunctions.h(d0);
    }

    public static DensityFunction yClampedGradient(int i, int j, double d0, double d1) {
        return new DensityFunctions.ab(i, j, d0, d1);
    }

    public static DensityFunction map(DensityFunction densityfunction, DensityFunctions.l.a densityfunctions_l_a) {
        return DensityFunctions.l.create(densityfunctions_l_a, densityfunction);
    }

    private static DensityFunction mapFromUnitTo(DensityFunction densityfunction, double d0, double d1) {
        double d2 = (d0 + d1) * 0.5D;
        double d3 = (d1 - d0) * 0.5D;

        return add(constant(d2), mul(constant(d3), densityfunction));
    }

    public static DensityFunction blendAlpha() {
        return DensityFunctions.d.INSTANCE;
    }

    public static DensityFunction blendOffset() {
        return DensityFunctions.f.INSTANCE;
    }

    public static DensityFunction lerp(DensityFunction densityfunction, DensityFunction densityfunction1, DensityFunction densityfunction2) {
        if (densityfunction1 instanceof DensityFunctions.h densityfunctions_h) {
            return lerp(densityfunction, densityfunctions_h.value, densityfunction2);
        } else {
            DensityFunction densityfunction3 = cacheOnce(densityfunction);
            DensityFunction densityfunction4 = add(mul(densityfunction3, constant(-1.0D)), constant(1.0D));

            return add(mul(densityfunction1, densityfunction4), mul(densityfunction2, densityfunction3));
        }
    }

    public static DensityFunction lerp(DensityFunction densityfunction, double d0, DensityFunction densityfunction1) {
        return add(mul(densityfunction, add(densityfunction1, constant(-d0))), constant(d0));
    }

    public static DensityFunction findTopSurface(DensityFunction densityfunction, DensityFunction densityfunction1, int i, int j) {
        return new DensityFunctions.j(densityfunction, densityfunction1, i, j);
    }

    private interface y extends DensityFunction {

        DensityFunction input();

        @Override
        default double compute(DensityFunction.b densityfunction_b) {
            return this.transform(densityfunction_b, this.input().compute(densityfunction_b));
        }

        @Override
        default void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            this.input().fillArray(adouble, densityfunction_a);

            for (int i = 0; i < adouble.length; ++i) {
                adouble[i] = this.transform(densityfunction_a.forIndex(i), adouble[i]);
            }

        }

        double transform(DensityFunction.b densityfunction_b, double d0);
    }

    private interface q extends DensityFunction {

        DensityFunction input();

        @Override
        default double compute(DensityFunction.b densityfunction_b) {
            return this.transform(this.input().compute(densityfunction_b));
        }

        @Override
        default void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            this.input().fillArray(adouble, densityfunction_a);

            for (int i = 0; i < adouble.length; ++i) {
                adouble[i] = this.transform(adouble[i]);
            }

        }

        double transform(double d0);
    }

    protected static enum d implements DensityFunction.d {

        INSTANCE;

        public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.<DensityFunction>of(MapCodec.unit(DensityFunctions.d.INSTANCE));

        private d() {}

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return 1.0D;
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            Arrays.fill(adouble, 1.0D);
        }

        @Override
        public double minValue() {
            return 1.0D;
        }

        @Override
        public double maxValue() {
            return 1.0D;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.d.CODEC;
        }
    }

    protected static enum f implements DensityFunction.d {

        INSTANCE;

        public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.<DensityFunction>of(MapCodec.unit(DensityFunctions.f.INSTANCE));

        private f() {}

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return 0.0D;
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            Arrays.fill(adouble, 0.0D);
        }

        @Override
        public double minValue() {
            return 0.0D;
        }

        @Override
        public double maxValue() {
            return 0.0D;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.f.CODEC;
        }
    }

    public interface c extends DensityFunction.d {

        KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.<DensityFunction>of(MapCodec.unit(DensityFunctions.b.INSTANCE));

        @Override
        default KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.c.CODEC;
        }
    }

    protected static enum b implements DensityFunctions.c {

        INSTANCE;

        private b() {}

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return 0.0D;
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            Arrays.fill(adouble, 0.0D);
        }

        @Override
        public double minValue() {
            return 0.0D;
        }

        @Override
        public double maxValue() {
            return 0.0D;
        }
    }

    @VisibleForDebug
    public static record k(Holder<DensityFunction> function) implements DensityFunction {

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return ((DensityFunction) this.function.value()).compute(densityfunction_b);
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            ((DensityFunction) this.function.value()).fillArray(adouble, densityfunction_a);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.k(new Holder.a(((DensityFunction) this.function.value()).mapAll(densityfunction_f))));
        }

        @Override
        public double minValue() {
            return this.function.isBound() ? ((DensityFunction) this.function.value()).minValue() : Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return this.function.isBound() ? ((DensityFunction) this.function.value()).maxValue() : Double.POSITIVE_INFINITY;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
        }
    }

    public interface n extends DensityFunction {

        DensityFunctions.m.a type();

        DensityFunction wrapped();

        @Override
        default KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return this.type().codec;
        }

        @Override
        default DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.m(this.type(), this.wrapped().mapAll(densityfunction_f)));
        }
    }

    protected static record m(DensityFunctions.m.a type, DensityFunction wrapped) implements DensityFunctions.n {

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return this.wrapped.compute(densityfunction_b);
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            this.wrapped.fillArray(adouble, densityfunction_a);
        }

        @Override
        public double minValue() {
            return this.wrapped.minValue();
        }

        @Override
        public double maxValue() {
            return this.wrapped.maxValue();
        }

        static enum a implements INamable {

            Interpolated("interpolated"), FlatCache("flat_cache"), Cache2D("cache_2d"), CacheOnce("cache_once"), CacheAllInCell("cache_all_in_cell");

            private final String name;
            final KeyDispatchDataCodec<DensityFunctions.n> codec = DensityFunctions.<DensityFunctions.n>singleFunctionArgumentCodec((densityfunction) -> {
                return new DensityFunctions.m(this, densityfunction);
            }, DensityFunctions.n::wrapped);

            private a(final String s) {
                this.name = s;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    protected static record p(DensityFunction.c noise, double xzScale, double yScale) implements DensityFunction {

        public static final MapCodec<DensityFunctions.p> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.c.CODEC.fieldOf("noise").forGetter(DensityFunctions.p::noise), Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.p::xzScale), Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.p::yScale)).apply(instance, DensityFunctions.p::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.p> CODEC = DensityFunctions.<DensityFunctions.p>makeCodec(DensityFunctions.p.DATA_CODEC);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return this.noise.getValue((double) densityfunction_b.blockX() * this.xzScale, (double) densityfunction_b.blockY() * this.yScale, (double) densityfunction_b.blockZ() * this.xzScale);
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            densityfunction_a.fillAllDirectly(adouble, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.p(densityfunction_f.visitNoise(this.noise), this.xzScale, this.yScale));
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.p.CODEC;
        }

        /** @deprecated */
        @Deprecated
        public double xzScale() {
            return this.xzScale;
        }
    }

    protected static final class i implements DensityFunction.d {

        public static final KeyDispatchDataCodec<DensityFunctions.i> CODEC = KeyDispatchDataCodec.<DensityFunctions.i>of(MapCodec.unit(new DensityFunctions.i(0L)));
        private static final float ISLAND_THRESHOLD = -0.9F;
        private final NoiseGenerator3Handler islandNoise;

        public i(long i) {
            RandomSource randomsource = new LegacyRandomSource(i);

            randomsource.consumeCount(17292);
            this.islandNoise = new NoiseGenerator3Handler(randomsource);
        }

        private static float getHeightValue(NoiseGenerator3Handler noisegenerator3handler, int i, int j) {
            int k = i / 2;
            int l = j / 2;
            int i1 = i % 2;
            int j1 = j % 2;
            float f = 100.0F - MathHelper.sqrt((float) (i * i + j * j)) * 8.0F;

            f = MathHelper.clamp(f, -100.0F, 80.0F);

            for (int k1 = -12; k1 <= 12; ++k1) {
                for (int l1 = -12; l1 <= 12; ++l1) {
                    long i2 = (long) (k + k1);
                    long j2 = (long) (l + l1);

                    if (i2 * i2 + j2 * j2 > 4096L && noisegenerator3handler.getValue((double) i2, (double) j2) < (double) -0.9F) {
                        float f1 = (MathHelper.abs((float) i2) * 3439.0F + MathHelper.abs((float) j2) * 147.0F) % 13.0F + 9.0F;
                        float f2 = (float) (i1 - k1 * 2);
                        float f3 = (float) (j1 - l1 * 2);
                        float f4 = 100.0F - MathHelper.sqrt(f2 * f2 + f3 * f3) * f1;

                        f4 = MathHelper.clamp(f4, -100.0F, 80.0F);
                        f = Math.max(f, f4);
                    }
                }
            }

            return f;
        }

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return ((double) getHeightValue(this.islandNoise, densityfunction_b.blockX() / 8, densityfunction_b.blockZ() / 8) - 8.0D) / 128.0D;
        }

        @Override
        public double minValue() {
            return -0.84375D;
        }

        @Override
        public double maxValue() {
            return 0.5625D;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.i.CODEC;
        }
    }

    protected static record aa(DensityFunction input, DensityFunction.c noise, DensityFunctions.aa.a rarityValueMapper) implements DensityFunctions.y {

        private static final MapCodec<DensityFunctions.aa> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.aa::input), DensityFunction.c.CODEC.fieldOf("noise").forGetter(DensityFunctions.aa::noise), DensityFunctions.aa.a.CODEC.fieldOf("rarity_value_mapper").forGetter(DensityFunctions.aa::rarityValueMapper)).apply(instance, DensityFunctions.aa::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.aa> CODEC = DensityFunctions.<DensityFunctions.aa>makeCodec(DensityFunctions.aa.DATA_CODEC);

        @Override
        public double transform(DensityFunction.b densityfunction_b, double d0) {
            double d1 = this.rarityValueMapper.mapper.get(d0);

            return d1 * Math.abs(this.noise.getValue((double) densityfunction_b.blockX() / d1, (double) densityfunction_b.blockY() / d1, (double) densityfunction_b.blockZ() / d1));
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.aa(this.input.mapAll(densityfunction_f), densityfunction_f.visitNoise(this.noise), this.rarityValueMapper));
        }

        @Override
        public double minValue() {
            return 0.0D;
        }

        @Override
        public double maxValue() {
            return this.rarityValueMapper.maxRarity * this.noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.aa.CODEC;
        }

        public static enum a implements INamable {

            TYPE1("type_1", NoiseRouterData.a::getSpaghettiRarity3D, 2.0D), TYPE2("type_2", NoiseRouterData.a::getSphaghettiRarity2D, 3.0D);

            public static final Codec<DensityFunctions.aa.a> CODEC = INamable.<DensityFunctions.aa.a>fromEnum(DensityFunctions.aa.a::values);
            private final String name;
            final Double2DoubleFunction mapper;
            final double maxRarity;

            private a(final String s, final Double2DoubleFunction double2doublefunction, final double d0) {
                this.name = s;
                this.mapper = double2doublefunction;
                this.maxRarity = d0;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    protected static record w(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.c noise) implements DensityFunction {

        private static final MapCodec<DensityFunctions.w> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.w::shiftX), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.w::shiftY), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.w::shiftZ), Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.w::xzScale), Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.w::yScale), DensityFunction.c.CODEC.fieldOf("noise").forGetter(DensityFunctions.w::noise)).apply(instance, DensityFunctions.w::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.w> CODEC = DensityFunctions.<DensityFunctions.w>makeCodec(DensityFunctions.w.DATA_CODEC);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            double d0 = (double) densityfunction_b.blockX() * this.xzScale + this.shiftX.compute(densityfunction_b);
            double d1 = (double) densityfunction_b.blockY() * this.yScale + this.shiftY.compute(densityfunction_b);
            double d2 = (double) densityfunction_b.blockZ() * this.xzScale + this.shiftZ.compute(densityfunction_b);

            return this.noise.getValue(d0, d1, d2);
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            densityfunction_a.fillAllDirectly(adouble, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.w(this.shiftX.mapAll(densityfunction_f), this.shiftY.mapAll(densityfunction_f), this.shiftZ.mapAll(densityfunction_f), this.xzScale, this.yScale, densityfunction_f.visitNoise(this.noise)));
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.w.CODEC;
        }
    }

    private static record r(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction {

        public static final MapCodec<DensityFunctions.r> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.r::input), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(DensityFunctions.r::minInclusive), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(DensityFunctions.r::maxExclusive), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(DensityFunctions.r::whenInRange), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(DensityFunctions.r::whenOutOfRange)).apply(instance, DensityFunctions.r::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.r> CODEC = DensityFunctions.<DensityFunctions.r>makeCodec(DensityFunctions.r.DATA_CODEC);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            double d0 = this.input.compute(densityfunction_b);

            return d0 >= this.minInclusive && d0 < this.maxExclusive ? this.whenInRange.compute(densityfunction_b) : this.whenOutOfRange.compute(densityfunction_b);
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            this.input.fillArray(adouble, densityfunction_a);

            for (int i = 0; i < adouble.length; ++i) {
                double d0 = adouble[i];

                if (d0 >= this.minInclusive && d0 < this.maxExclusive) {
                    adouble[i] = this.whenInRange.compute(densityfunction_a.forIndex(i));
                } else {
                    adouble[i] = this.whenOutOfRange.compute(densityfunction_a.forIndex(i));
                }
            }

        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.r(this.input.mapAll(densityfunction_f), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(densityfunction_f), this.whenOutOfRange.mapAll(densityfunction_f)));
        }

        @Override
        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        @Override
        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.r.CODEC;
        }
    }

    interface v extends DensityFunction {

        DensityFunction.c offsetNoise();

        @Override
        default double minValue() {
            return -this.maxValue();
        }

        @Override
        default double maxValue() {
            return this.offsetNoise().maxValue() * 4.0D;
        }

        default double compute(double d0, double d1, double d2) {
            return this.offsetNoise().getValue(d0 * 0.25D, d1 * 0.25D, d2 * 0.25D) * 4.0D;
        }

        @Override
        default void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            densityfunction_a.fillAllDirectly(adouble, this);
        }
    }

    protected static record t(DensityFunction.c offsetNoise) implements DensityFunctions.v {

        static final KeyDispatchDataCodec<DensityFunctions.t> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.c.CODEC, DensityFunctions.t::new, DensityFunctions.t::offsetNoise);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return this.compute((double) densityfunction_b.blockX(), 0.0D, (double) densityfunction_b.blockZ());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.t(densityfunction_f.visitNoise(this.offsetNoise)));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.t.CODEC;
        }
    }

    protected static record u(DensityFunction.c offsetNoise) implements DensityFunctions.v {

        static final KeyDispatchDataCodec<DensityFunctions.u> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.c.CODEC, DensityFunctions.u::new, DensityFunctions.u::offsetNoise);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return this.compute((double) densityfunction_b.blockZ(), (double) densityfunction_b.blockX(), 0.0D);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.u(densityfunction_f.visitNoise(this.offsetNoise)));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.u.CODEC;
        }
    }

    protected static record s(DensityFunction.c offsetNoise) implements DensityFunctions.v {

        static final KeyDispatchDataCodec<DensityFunctions.s> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.c.CODEC, DensityFunctions.s::new, DensityFunctions.s::offsetNoise);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return this.compute((double) densityfunction_b.blockX(), (double) densityfunction_b.blockY(), (double) densityfunction_b.blockZ());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.s(densityfunction_f.visitNoise(this.offsetNoise)));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.s.CODEC;
        }
    }

    private static record e(DensityFunction input) implements DensityFunctions.y {

        static final KeyDispatchDataCodec<DensityFunctions.e> CODEC = DensityFunctions.<DensityFunctions.e>singleFunctionArgumentCodec(DensityFunctions.e::new, DensityFunctions.e::input);

        @Override
        public double transform(DensityFunction.b densityfunction_b, double d0) {
            return densityfunction_b.getBlender().blendDensity(densityfunction_b, d0);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.e(this.input.mapAll(densityfunction_f)));
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.e.CODEC;
        }
    }

    protected static record g(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.q {

        private static final MapCodec<DensityFunctions.g> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(DensityFunctions.g::input), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(DensityFunctions.g::minValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(DensityFunctions.g::maxValue)).apply(instance, DensityFunctions.g::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.g> CODEC = DensityFunctions.<DensityFunctions.g>makeCodec(DensityFunctions.g.DATA_CODEC);

        @Override
        public double transform(double d0) {
            return MathHelper.clamp(d0, this.minValue, this.maxValue);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return new DensityFunctions.g(this.input.mapAll(densityfunction_f), this.minValue, this.maxValue);
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.g.CODEC;
        }
    }

    protected static record l(DensityFunctions.l.a type, DensityFunction input, double minValue, double maxValue) implements DensityFunctions.q {

        public static DensityFunctions.l create(DensityFunctions.l.a densityfunctions_l_a, DensityFunction densityfunction) {
            double d0 = densityfunction.minValue();
            double d1 = densityfunction.maxValue();
            double d2 = transform(densityfunctions_l_a, d0);
            double d3 = transform(densityfunctions_l_a, d1);

            return densityfunctions_l_a == DensityFunctions.l.a.INVERT ? (d0 < 0.0D && d1 > 0.0D ? new DensityFunctions.l(densityfunctions_l_a, densityfunction, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) : new DensityFunctions.l(densityfunctions_l_a, densityfunction, d3, d2)) : (densityfunctions_l_a != DensityFunctions.l.a.ABS && densityfunctions_l_a != DensityFunctions.l.a.SQUARE ? new DensityFunctions.l(densityfunctions_l_a, densityfunction, d2, d3) : new DensityFunctions.l(densityfunctions_l_a, densityfunction, Math.max(0.0D, d0), Math.max(d2, d3)));
        }

        private static double transform(DensityFunctions.l.a densityfunctions_l_a, double d0) {
            double d1;

            switch (densityfunctions_l_a.ordinal()) {
                case 0:
                    d1 = Math.abs(d0);
                    break;
                case 1:
                    d1 = d0 * d0;
                    break;
                case 2:
                    d1 = d0 * d0 * d0;
                    break;
                case 3:
                    d1 = d0 > 0.0D ? d0 : d0 * 0.5D;
                    break;
                case 4:
                    d1 = d0 > 0.0D ? d0 : d0 * 0.25D;
                    break;
                case 5:
                    d1 = 1.0D / d0;
                    break;
                case 6:
                    double d2 = MathHelper.clamp(d0, -1.0D, 1.0D);

                    d1 = d2 / 2.0D - d2 * d2 * d2 / 24.0D;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return d1;
        }

        @Override
        public double transform(double d0) {
            return transform(this.type, d0);
        }

        @Override
        public DensityFunctions.l mapAll(DensityFunction.f densityfunction_f) {
            return create(this.type, this.input.mapAll(densityfunction_f));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return this.type.codec;
        }

        static enum a implements INamable {

            ABS("abs"), SQUARE("square"), CUBE("cube"), HALF_NEGATIVE("half_negative"), QUARTER_NEGATIVE("quarter_negative"), INVERT("invert"), SQUEEZE("squeeze");

            private final String name;
            final KeyDispatchDataCodec<DensityFunctions.l> codec = DensityFunctions.<DensityFunctions.l>singleFunctionArgumentCodec((densityfunction) -> {
                return DensityFunctions.l.create(this, densityfunction);
            }, DensityFunctions.l::input);

            private a(final String s) {
                this.name = s;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    interface z extends DensityFunction {

        Logger LOGGER = LogUtils.getLogger();

        static DensityFunctions.z create(DensityFunctions.z.a densityfunctions_z_a, DensityFunction densityfunction, DensityFunction densityfunction1) {
            double d0 = densityfunction.minValue();
            double d1 = densityfunction1.minValue();
            double d2 = densityfunction.maxValue();
            double d3 = densityfunction1.maxValue();

            if (densityfunctions_z_a == DensityFunctions.z.a.MIN || densityfunctions_z_a == DensityFunctions.z.a.MAX) {
                boolean flag = d0 >= d3;
                boolean flag1 = d1 >= d2;

                if (flag || flag1) {
                    DensityFunctions.z.LOGGER.warn("Creating a {} function between two non-overlapping inputs: {} and {}", new Object[]{densityfunctions_z_a, densityfunction, densityfunction1});
                }
            }

            double d4;

            switch (densityfunctions_z_a.ordinal()) {
                case 0:
                    d4 = d0 + d1;
                    break;
                case 1:
                    d4 = d0 > 0.0D && d1 > 0.0D ? d0 * d1 : (d2 < 0.0D && d3 < 0.0D ? d2 * d3 : Math.min(d0 * d3, d2 * d1));
                    break;
                case 2:
                    d4 = Math.min(d0, d1);
                    break;
                case 3:
                    d4 = Math.max(d0, d1);
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            double d5 = d4;

            switch (densityfunctions_z_a.ordinal()) {
                case 0:
                    d4 = d2 + d3;
                    break;
                case 1:
                    d4 = d0 > 0.0D && d1 > 0.0D ? d2 * d3 : (d2 < 0.0D && d3 < 0.0D ? d0 * d1 : Math.max(d0 * d1, d2 * d3));
                    break;
                case 2:
                    d4 = Math.min(d2, d3);
                    break;
                case 3:
                    d4 = Math.max(d2, d3);
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            double d6 = d4;

            if (densityfunctions_z_a == DensityFunctions.z.a.MUL || densityfunctions_z_a == DensityFunctions.z.a.ADD) {
                if (densityfunction instanceof DensityFunctions.h) {
                    DensityFunctions.h densityfunctions_h = (DensityFunctions.h) densityfunction;

                    return new DensityFunctions.o(densityfunctions_z_a == DensityFunctions.z.a.ADD ? DensityFunctions.o.a.ADD : DensityFunctions.o.a.MUL, densityfunction1, d5, d6, densityfunctions_h.value);
                }

                if (densityfunction1 instanceof DensityFunctions.h) {
                    DensityFunctions.h densityfunctions_h1 = (DensityFunctions.h) densityfunction1;

                    return new DensityFunctions.o(densityfunctions_z_a == DensityFunctions.z.a.ADD ? DensityFunctions.o.a.ADD : DensityFunctions.o.a.MUL, densityfunction, d5, d6, densityfunctions_h1.value);
                }
            }

            return new DensityFunctions.a(densityfunctions_z_a, densityfunction, densityfunction1, d5, d6);
        }

        DensityFunctions.z.a type();

        DensityFunction argument1();

        DensityFunction argument2();

        @Override
        default KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return this.type().codec;
        }

        public static enum a implements INamable {

            ADD("add"), MUL("mul"), MIN("min"), MAX("max");

            final KeyDispatchDataCodec<DensityFunctions.z> codec = DensityFunctions.<DensityFunctions.z>doubleFunctionArgumentCodec((densityfunction, densityfunction1) -> {
                return DensityFunctions.z.create(this, densityfunction, densityfunction1);
            }, DensityFunctions.z::argument1, DensityFunctions.z::argument2);
            private final String name;

            private a(final String s) {
                this.name = s;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    private static record o(DensityFunctions.o.a specificType, DensityFunction input, double minValue, double maxValue, double argument) implements DensityFunctions.q, DensityFunctions.z {

        @Override
        public DensityFunctions.z.a type() {
            return this.specificType == DensityFunctions.o.a.MUL ? DensityFunctions.z.a.MUL : DensityFunctions.z.a.ADD;
        }

        @Override
        public DensityFunction argument1() {
            return DensityFunctions.constant(this.argument);
        }

        @Override
        public DensityFunction argument2() {
            return this.input;
        }

        @Override
        public double transform(double d0) {
            double d1;

            switch (this.specificType.ordinal()) {
                case 0:
                    d1 = d0 * this.argument;
                    break;
                case 1:
                    d1 = d0 + this.argument;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return d1;
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            DensityFunction densityfunction = this.input.mapAll(densityfunction_f);
            double d0 = densityfunction.minValue();
            double d1 = densityfunction.maxValue();
            double d2;
            double d3;

            if (this.specificType == DensityFunctions.o.a.ADD) {
                d2 = d0 + this.argument;
                d3 = d1 + this.argument;
            } else if (this.argument >= 0.0D) {
                d2 = d0 * this.argument;
                d3 = d1 * this.argument;
            } else {
                d2 = d1 * this.argument;
                d3 = d0 * this.argument;
            }

            return new DensityFunctions.o(this.specificType, densityfunction, d2, d3, this.argument);
        }

        static enum a {

            MUL, ADD;

            private a() {}
        }
    }

    private static record a(DensityFunctions.z.a type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue) implements DensityFunctions.z {

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            double d0 = this.argument1.compute(densityfunction_b);
            double d1;

            switch (this.type.ordinal()) {
                case 0:
                    d1 = d0 + this.argument2.compute(densityfunction_b);
                    break;
                case 1:
                    d1 = d0 == 0.0D ? 0.0D : d0 * this.argument2.compute(densityfunction_b);
                    break;
                case 2:
                    d1 = d0 < this.argument2.minValue() ? d0 : Math.min(d0, this.argument2.compute(densityfunction_b));
                    break;
                case 3:
                    d1 = d0 > this.argument2.maxValue() ? d0 : Math.max(d0, this.argument2.compute(densityfunction_b));
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return d1;
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            this.argument1.fillArray(adouble, densityfunction_a);
            switch (this.type.ordinal()) {
                case 0:
                    double[] adouble1 = new double[adouble.length];

                    this.argument2.fillArray(adouble1, densityfunction_a);

                    for (int i = 0; i < adouble.length; ++i) {
                        adouble[i] += adouble1[i];
                    }
                    break;
                case 1:
                    for (int j = 0; j < adouble.length; ++j) {
                        double d0 = adouble[j];

                        adouble[j] = d0 == 0.0D ? 0.0D : d0 * this.argument2.compute(densityfunction_a.forIndex(j));
                    }
                    break;
                case 2:
                    double d1 = this.argument2.minValue();

                    for (int k = 0; k < adouble.length; ++k) {
                        double d2 = adouble[k];

                        adouble[k] = d2 < d1 ? d2 : Math.min(d2, this.argument2.compute(densityfunction_a.forIndex(k)));
                    }
                    break;
                case 3:
                    double d3 = this.argument2.maxValue();

                    for (int l = 0; l < adouble.length; ++l) {
                        double d4 = adouble[l];

                        adouble[l] = d4 > d3 ? d4 : Math.max(d4, this.argument2.compute(densityfunction_a.forIndex(l)));
                    }
            }

        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(DensityFunctions.z.create(this.type, this.argument1.mapAll(densityfunction_f), this.argument2.mapAll(densityfunction_f)));
        }
    }

    public static record x(CubicSpline<DensityFunctions.x.b, DensityFunctions.x.a> spline) implements DensityFunction {

        private static final Codec<CubicSpline<DensityFunctions.x.b, DensityFunctions.x.a>> SPLINE_CODEC = CubicSpline.codec(DensityFunctions.x.a.CODEC);
        private static final MapCodec<DensityFunctions.x> DATA_CODEC = DensityFunctions.x.SPLINE_CODEC.fieldOf("spline").xmap(DensityFunctions.x::new, DensityFunctions.x::spline);
        public static final KeyDispatchDataCodec<DensityFunctions.x> CODEC = DensityFunctions.<DensityFunctions.x>makeCodec(DensityFunctions.x.DATA_CODEC);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return (double) this.spline.apply(new DensityFunctions.x.b(densityfunction_b));
        }

        @Override
        public double minValue() {
            return (double) this.spline.minValue();
        }

        @Override
        public double maxValue() {
            return (double) this.spline.maxValue();
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            densityfunction_a.fillAllDirectly(adouble, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.x(this.spline.mapAll((densityfunctions_x_a) -> {
                return densityfunctions_x_a.mapAll(densityfunction_f);
            })));
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.x.CODEC;
        }

        public static record a(Holder<DensityFunction> function) implements BoundedFloatFunction<DensityFunctions.x.b> {

            public static final Codec<DensityFunctions.x.a> CODEC = DensityFunction.CODEC.xmap(DensityFunctions.x.a::new, DensityFunctions.x.a::function);

            public String toString() {
                Optional<ResourceKey<DensityFunction>> optional = this.function.unwrapKey();

                if (optional.isPresent()) {
                    ResourceKey<DensityFunction> resourcekey = (ResourceKey) optional.get();

                    if (resourcekey == NoiseRouterData.CONTINENTS) {
                        return "continents";
                    }

                    if (resourcekey == NoiseRouterData.EROSION) {
                        return "erosion";
                    }

                    if (resourcekey == NoiseRouterData.RIDGES) {
                        return "weirdness";
                    }

                    if (resourcekey == NoiseRouterData.RIDGES_FOLDED) {
                        return "ridges";
                    }
                }

                return "Coordinate[" + String.valueOf(this.function) + "]";
            }

            public float apply(DensityFunctions.x.b densityfunctions_x_b) {
                return (float) ((DensityFunction) this.function.value()).compute(densityfunctions_x_b.context());
            }

            @Override
            public float minValue() {
                return this.function.isBound() ? (float) ((DensityFunction) this.function.value()).minValue() : Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return this.function.isBound() ? (float) ((DensityFunction) this.function.value()).maxValue() : Float.POSITIVE_INFINITY;
            }

            public DensityFunctions.x.a mapAll(DensityFunction.f densityfunction_f) {
                return new DensityFunctions.x.a(new Holder.a(((DensityFunction) this.function.value()).mapAll(densityfunction_f)));
            }
        }

        public static record b(DensityFunction.b context) {

        }
    }

    private static record h(double value) implements DensityFunction.d {

        static final KeyDispatchDataCodec<DensityFunctions.h> CODEC = DensityFunctions.singleArgumentCodec(DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.h::new, DensityFunctions.h::value);
        static final DensityFunctions.h ZERO = new DensityFunctions.h(0.0D);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return this.value;
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            Arrays.fill(adouble, this.value);
        }

        @Override
        public double minValue() {
            return this.value;
        }

        @Override
        public double maxValue() {
            return this.value;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.h.CODEC;
        }
    }

    private static record ab(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.d {

        private static final MapCodec<DensityFunctions.ab> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.intRange(DimensionManager.MIN_Y * 2, DimensionManager.MAX_Y * 2).fieldOf("from_y").forGetter(DensityFunctions.ab::fromY), Codec.intRange(DimensionManager.MIN_Y * 2, DimensionManager.MAX_Y * 2).fieldOf("to_y").forGetter(DensityFunctions.ab::toY), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(DensityFunctions.ab::fromValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(DensityFunctions.ab::toValue)).apply(instance, DensityFunctions.ab::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.ab> CODEC = DensityFunctions.<DensityFunctions.ab>makeCodec(DensityFunctions.ab.DATA_CODEC);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            return MathHelper.clampedMap((double) densityfunction_b.blockY(), (double) this.fromY, (double) this.toY, this.fromValue, this.toValue);
        }

        @Override
        public double minValue() {
            return Math.min(this.fromValue, this.toValue);
        }

        @Override
        public double maxValue() {
            return Math.max(this.fromValue, this.toValue);
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.ab.CODEC;
        }
    }

    private static record j(DensityFunction density, DensityFunction upperBound, int lowerBound, int cellHeight) implements DensityFunction {

        private static final MapCodec<DensityFunctions.j> DATA_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("density").forGetter(DensityFunctions.j::density), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("upper_bound").forGetter(DensityFunctions.j::upperBound), Codec.intRange(DimensionManager.MIN_Y * 2, DimensionManager.MAX_Y * 2).fieldOf("lower_bound").forGetter(DensityFunctions.j::lowerBound), ExtraCodecs.POSITIVE_INT.fieldOf("cell_height").forGetter(DensityFunctions.j::cellHeight)).apply(instance, DensityFunctions.j::new);
        });
        public static final KeyDispatchDataCodec<DensityFunctions.j> CODEC = DensityFunctions.<DensityFunctions.j>makeCodec(DensityFunctions.j.DATA_CODEC);

        @Override
        public double compute(DensityFunction.b densityfunction_b) {
            int i = MathHelper.floor(this.upperBound.compute(densityfunction_b) / (double) this.cellHeight) * this.cellHeight;

            if (i <= this.lowerBound) {
                return (double) this.lowerBound;
            } else {
                for (int j = i; j >= this.lowerBound; j -= this.cellHeight) {
                    if (this.density.compute(new DensityFunction.e(densityfunction_b.blockX(), j, densityfunction_b.blockZ())) > 0.0D) {
                        return (double) j;
                    }
                }

                return (double) this.lowerBound;
            }
        }

        @Override
        public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
            densityfunction_a.fillAllDirectly(adouble, this);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.f densityfunction_f) {
            return densityfunction_f.apply(new DensityFunctions.j(this.density.mapAll(densityfunction_f), this.upperBound.mapAll(densityfunction_f), this.lowerBound, this.cellHeight));
        }

        @Override
        public double minValue() {
            return (double) this.lowerBound;
        }

        @Override
        public double maxValue() {
            return Math.max((double) this.lowerBound, this.upperBound.maxValue());
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.j.CODEC;
        }
    }
}
