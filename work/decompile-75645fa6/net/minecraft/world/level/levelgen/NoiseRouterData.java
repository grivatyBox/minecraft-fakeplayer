package net.minecraft.world.level.levelgen;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorNormal;

public class NoiseRouterData {

    public static final float GLOBAL_OFFSET = -0.50375F;
    private static final float ORE_THICKNESS = 0.08F;
    private static final double VEININESS_FREQUENCY = 1.5D;
    private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5D;
    private static final double SURFACE_DENSITY_THRESHOLD = 1.5625D;
    private static final double CHEESE_NOISE_TARGET = -0.703125D;
    public static final double NOISE_ZERO = 0.390625D;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    public static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private static final int DENSITY_Y_ANCHOR_BOTTOM = -64;
    private static final int DENSITY_Y_ANCHOR_TOP = 320;
    private static final double DENSITY_Y_BOTTOM = 1.5D;
    private static final double DENSITY_Y_TOP = -1.5D;
    private static final int OVERWORLD_BOTTOM_SLIDE_HEIGHT = 24;
    private static final double BASE_DENSITY_MULTIPLIER = 4.0D;
    private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0D);
    private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
    private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
    private static final ResourceKey<DensityFunction> Y = createKey("y");
    private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD = createKey("overworld/base_3d_noise");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER = createKey("nether/base_3d_noise");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE_END = createKey("end/base_3d_noise");
    public static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
    public static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
    public static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
    public static final ResourceKey<DensityFunction> RIDGES_FOLDED = createKey("overworld/ridges_folded");
    public static final ResourceKey<DensityFunction> OFFSET = createKey("overworld/offset");
    public static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS = createKey("overworld/jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
    public static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
    public static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
    private static final ResourceKey<DensityFunction> OFFSET_LARGE = createKey("overworld_large_biomes/offset");
    private static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
    private static final ResourceKey<DensityFunction> JAGGEDNESS_LARGE = createKey("overworld_large_biomes/jaggedness");
    private static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
    private static final ResourceKey<DensityFunction> OFFSET_AMPLIFIED = createKey("overworld_amplified/offset");
    private static final ResourceKey<DensityFunction> FACTOR_AMPLIFIED = createKey("overworld_amplified/factor");
    private static final ResourceKey<DensityFunction> JAGGEDNESS_AMPLIFIED = createKey("overworld_amplified/jaggedness");
    private static final ResourceKey<DensityFunction> DEPTH_AMPLIFIED = createKey("overworld_amplified/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_AMPLIFIED = createKey("overworld_amplified/sloped_cheese");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
    private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
    private static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
    private static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
    private static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

    public NoiseRouterData() {}

    private static ResourceKey<DensityFunction> createKey(String s) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, MinecraftKey.withDefaultNamespace(s));
    }

    public static Holder<? extends DensityFunction> bootstrap(BootstrapContext<DensityFunction> bootstrapcontext) {
        HolderGetter<NoiseGeneratorNormal.a> holdergetter = bootstrapcontext.<NoiseGeneratorNormal.a>lookup(Registries.NOISE);
        HolderGetter<DensityFunction> holdergetter1 = bootstrapcontext.<DensityFunction>lookup(Registries.DENSITY_FUNCTION);

        bootstrapcontext.register(NoiseRouterData.ZERO, DensityFunctions.zero());
        int i = DimensionManager.MIN_Y * 2;
        int j = DimensionManager.MAX_Y * 2;

        bootstrapcontext.register(NoiseRouterData.Y, DensityFunctions.yClampedGradient(i, j, (double) i, (double) j));
        DensityFunction densityfunction = registerAndWrap(bootstrapcontext, NoiseRouterData.SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(holdergetter.getOrThrow(Noises.SHIFT)))));
        DensityFunction densityfunction1 = registerAndWrap(bootstrapcontext, NoiseRouterData.SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(holdergetter.getOrThrow(Noises.SHIFT)))));

        bootstrapcontext.register(NoiseRouterData.BASE_3D_NOISE_OVERWORLD, BlendedNoise.createUnseeded(0.25D, 0.125D, 80.0D, 160.0D, 8.0D));
        bootstrapcontext.register(NoiseRouterData.BASE_3D_NOISE_NETHER, BlendedNoise.createUnseeded(0.25D, 0.375D, 80.0D, 60.0D, 8.0D));
        bootstrapcontext.register(NoiseRouterData.BASE_3D_NOISE_END, BlendedNoise.createUnseeded(0.25D, 0.25D, 80.0D, 160.0D, 4.0D));
        Holder<DensityFunction> holder = bootstrapcontext.register(NoiseRouterData.CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.CONTINENTALNESS))));
        Holder<DensityFunction> holder1 = bootstrapcontext.register(NoiseRouterData.EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.EROSION))));
        DensityFunction densityfunction2 = registerAndWrap(bootstrapcontext, NoiseRouterData.RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.RIDGE))));

        bootstrapcontext.register(NoiseRouterData.RIDGES_FOLDED, peaksAndValleys(densityfunction2));
        DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter.getOrThrow(Noises.JAGGED), 1500.0D, 0.0D);

        registerTerrainNoises(bootstrapcontext, holdergetter1, densityfunction3, holder, holder1, NoiseRouterData.OFFSET, NoiseRouterData.FACTOR, NoiseRouterData.JAGGEDNESS, NoiseRouterData.DEPTH, NoiseRouterData.SLOPED_CHEESE, false);
        Holder<DensityFunction> holder2 = bootstrapcontext.register(NoiseRouterData.CONTINENTS_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.CONTINENTALNESS_LARGE))));
        Holder<DensityFunction> holder3 = bootstrapcontext.register(NoiseRouterData.EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.EROSION_LARGE))));

        registerTerrainNoises(bootstrapcontext, holdergetter1, densityfunction3, holder2, holder3, NoiseRouterData.OFFSET_LARGE, NoiseRouterData.FACTOR_LARGE, NoiseRouterData.JAGGEDNESS_LARGE, NoiseRouterData.DEPTH_LARGE, NoiseRouterData.SLOPED_CHEESE_LARGE, false);
        registerTerrainNoises(bootstrapcontext, holdergetter1, densityfunction3, holder, holder1, NoiseRouterData.OFFSET_AMPLIFIED, NoiseRouterData.FACTOR_AMPLIFIED, NoiseRouterData.JAGGEDNESS_AMPLIFIED, NoiseRouterData.DEPTH_AMPLIFIED, NoiseRouterData.SLOPED_CHEESE_AMPLIFIED, true);
        bootstrapcontext.register(NoiseRouterData.SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), getFunction(holdergetter1, NoiseRouterData.BASE_3D_NOISE_END)));
        bootstrapcontext.register(NoiseRouterData.SPAGHETTI_ROUGHNESS_FUNCTION, spaghettiRoughnessFunction(holdergetter));
        bootstrapcontext.register(NoiseRouterData.SPAGHETTI_2D_THICKNESS_MODULATOR, DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.SPAGHETTI_2D_THICKNESS), 2.0D, 1.0D, -0.6D, -1.3D)));
        bootstrapcontext.register(NoiseRouterData.SPAGHETTI_2D, spaghetti2D(holdergetter1, holdergetter));
        bootstrapcontext.register(NoiseRouterData.ENTRANCES, entrances(holdergetter1, holdergetter));
        bootstrapcontext.register(NoiseRouterData.NOODLE, noodle(holdergetter1, holdergetter));
        return bootstrapcontext.register(NoiseRouterData.PILLARS, pillars(holdergetter));
    }

    private static void registerTerrainNoises(BootstrapContext<DensityFunction> bootstrapcontext, HolderGetter<DensityFunction> holdergetter, DensityFunction densityfunction, Holder<DensityFunction> holder, Holder<DensityFunction> holder1, ResourceKey<DensityFunction> resourcekey, ResourceKey<DensityFunction> resourcekey1, ResourceKey<DensityFunction> resourcekey2, ResourceKey<DensityFunction> resourcekey3, ResourceKey<DensityFunction> resourcekey4, boolean flag) {
        DensityFunctions.x.a densityfunctions_x_a = new DensityFunctions.x.a(holder);
        DensityFunctions.x.a densityfunctions_x_a1 = new DensityFunctions.x.a(holder1);
        DensityFunctions.x.a densityfunctions_x_a2 = new DensityFunctions.x.a(holdergetter.getOrThrow(NoiseRouterData.RIDGES));
        DensityFunctions.x.a densityfunctions_x_a3 = new DensityFunctions.x.a(holdergetter.getOrThrow(NoiseRouterData.RIDGES_FOLDED));
        DensityFunction densityfunction1 = registerAndWrap(bootstrapcontext, resourcekey, splineWithBlending(DensityFunctions.add(DensityFunctions.constant((double) -0.50375F), DensityFunctions.spline(TerrainProvider.overworldOffset(densityfunctions_x_a, densityfunctions_x_a1, densityfunctions_x_a3, flag))), DensityFunctions.blendOffset()));
        DensityFunction densityfunction2 = registerAndWrap(bootstrapcontext, resourcekey1, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldFactor(densityfunctions_x_a, densityfunctions_x_a1, densityfunctions_x_a2, densityfunctions_x_a3, flag)), NoiseRouterData.BLENDING_FACTOR));
        DensityFunction densityfunction3 = registerAndWrap(bootstrapcontext, resourcekey3, offsetToDepth(densityfunction1));
        DensityFunction densityfunction4 = registerAndWrap(bootstrapcontext, resourcekey2, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldJaggedness(densityfunctions_x_a, densityfunctions_x_a1, densityfunctions_x_a2, densityfunctions_x_a3, flag)), NoiseRouterData.BLENDING_JAGGEDNESS));
        DensityFunction densityfunction5 = DensityFunctions.mul(densityfunction4, densityfunction.halfNegative());
        DensityFunction densityfunction6 = noiseGradientDensity(densityfunction2, DensityFunctions.add(densityfunction3, densityfunction5));

        bootstrapcontext.register(resourcekey4, DensityFunctions.add(densityfunction6, getFunction(holdergetter, NoiseRouterData.BASE_3D_NOISE_OVERWORLD)));
    }

    private static DensityFunction offsetToDepth(DensityFunction densityfunction) {
        return DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5D, -1.5D), densityfunction);
    }

    private static DensityFunction registerAndWrap(BootstrapContext<DensityFunction> bootstrapcontext, ResourceKey<DensityFunction> resourcekey, DensityFunction densityfunction) {
        return new DensityFunctions.k(bootstrapcontext.register(resourcekey, densityfunction));
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> holdergetter, ResourceKey<DensityFunction> resourcekey) {
        return new DensityFunctions.k(holdergetter.getOrThrow(resourcekey));
    }

    private static DensityFunction peaksAndValleys(DensityFunction densityfunction) {
        return DensityFunctions.mul(DensityFunctions.add(DensityFunctions.add(densityfunction.abs(), DensityFunctions.constant(-0.6666666666666666D)).abs(), DensityFunctions.constant(-0.3333333333333333D)), DensityFunctions.constant(-3.0D));
    }

    public static float peaksAndValleys(float f) {
        return -(Math.abs(Math.abs(f) - 0.6666667F) - 0.33333334F) * 3.0F;
    }

    private static DensityFunction spaghettiRoughnessFunction(HolderGetter<NoiseGeneratorNormal.a> holdergetter) {
        DensityFunction densityfunction = DensityFunctions.noise(holdergetter.getOrThrow(Noises.SPAGHETTI_ROUGHNESS));
        DensityFunction densityfunction1 = DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0D, -0.1D);

        return DensityFunctions.cacheOnce(DensityFunctions.mul(densityfunction1, DensityFunctions.add(densityfunction.abs(), DensityFunctions.constant(-0.4D))));
    }

    private static DensityFunction entrances(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1) {
        DensityFunction densityfunction = DensityFunctions.cacheOnce(DensityFunctions.noise(holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_RARITY), 2.0D, 1.0D));
        DensityFunction densityfunction1 = DensityFunctions.mappedNoise(holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_THICKNESS), -0.065D, -0.088D);
        DensityFunction densityfunction2 = DensityFunctions.weirdScaledSampler(densityfunction, holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_1), DensityFunctions.aa.a.TYPE1);
        DensityFunction densityfunction3 = DensityFunctions.weirdScaledSampler(densityfunction, holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_2), DensityFunctions.aa.a.TYPE1);
        DensityFunction densityfunction4 = DensityFunctions.add(DensityFunctions.max(densityfunction2, densityfunction3), densityfunction1).clamp(-1.0D, 1.0D);
        DensityFunction densityfunction5 = getFunction(holdergetter, NoiseRouterData.SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction densityfunction6 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.CAVE_ENTRANCE), 0.75D, 0.5D);
        DensityFunction densityfunction7 = DensityFunctions.add(DensityFunctions.add(densityfunction6, DensityFunctions.constant(0.37D)), DensityFunctions.yClampedGradient(-10, 30, 0.3D, 0.0D));

        return DensityFunctions.cacheOnce(DensityFunctions.min(densityfunction7, DensityFunctions.add(densityfunction5, densityfunction4)));
    }

    private static DensityFunction noodle(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1) {
        DensityFunction densityfunction = getFunction(holdergetter, NoiseRouterData.Y);
        int i = -64;
        int j = -60;
        int k = 320;
        DensityFunction densityfunction1 = yLimitedInterpolatable(densityfunction, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.NOODLE), 1.0D, 1.0D), -60, 320, -1);
        DensityFunction densityfunction2 = yLimitedInterpolatable(densityfunction, DensityFunctions.mappedNoise(holdergetter1.getOrThrow(Noises.NOODLE_THICKNESS), 1.0D, 1.0D, -0.05D, -0.1D), -60, 320, 0);
        double d0 = 2.6666666666666665D;
        DensityFunction densityfunction3 = yLimitedInterpolatable(densityfunction, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.NOODLE_RIDGE_A), 2.6666666666666665D, 2.6666666666666665D), -60, 320, 0);
        DensityFunction densityfunction4 = yLimitedInterpolatable(densityfunction, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.NOODLE_RIDGE_B), 2.6666666666666665D, 2.6666666666666665D), -60, 320, 0);
        DensityFunction densityfunction5 = DensityFunctions.mul(DensityFunctions.constant(1.5D), DensityFunctions.max(densityfunction3.abs(), densityfunction4.abs()));

        return DensityFunctions.rangeChoice(densityfunction1, -1000000.0D, 0.0D, DensityFunctions.constant(64.0D), DensityFunctions.add(densityfunction2, densityfunction5));
    }

    private static DensityFunction pillars(HolderGetter<NoiseGeneratorNormal.a> holdergetter) {
        double d0 = 25.0D;
        double d1 = 0.3D;
        DensityFunction densityfunction = DensityFunctions.noise(holdergetter.getOrThrow(Noises.PILLAR), 25.0D, 0.3D);
        DensityFunction densityfunction1 = DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.PILLAR_RARENESS), 0.0D, -2.0D);
        DensityFunction densityfunction2 = DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.PILLAR_THICKNESS), 0.0D, 1.1D);
        DensityFunction densityfunction3 = DensityFunctions.add(DensityFunctions.mul(densityfunction, DensityFunctions.constant(2.0D)), densityfunction1);

        return DensityFunctions.cacheOnce(DensityFunctions.mul(densityfunction3, densityfunction2.cube()));
    }

    private static DensityFunction spaghetti2D(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1) {
        DensityFunction densityfunction = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.SPAGHETTI_2D_MODULATOR), 2.0D, 1.0D);
        DensityFunction densityfunction1 = DensityFunctions.weirdScaledSampler(densityfunction, holdergetter1.getOrThrow(Noises.SPAGHETTI_2D), DensityFunctions.aa.a.TYPE2);
        DensityFunction densityfunction2 = DensityFunctions.mappedNoise(holdergetter1.getOrThrow(Noises.SPAGHETTI_2D_ELEVATION), 0.0D, (double) Math.floorDiv(-64, 8), 8.0D);
        DensityFunction densityfunction3 = getFunction(holdergetter, NoiseRouterData.SPAGHETTI_2D_THICKNESS_MODULATOR);
        DensityFunction densityfunction4 = DensityFunctions.add(densityfunction2, DensityFunctions.yClampedGradient(-64, 320, 8.0D, -40.0D)).abs();
        DensityFunction densityfunction5 = DensityFunctions.add(densityfunction4, densityfunction3).cube();
        double d0 = 0.083D;
        DensityFunction densityfunction6 = DensityFunctions.add(densityfunction1, DensityFunctions.mul(DensityFunctions.constant(0.083D), densityfunction3));

        return DensityFunctions.max(densityfunction6, densityfunction5).clamp(-1.0D, 1.0D);
    }

    private static DensityFunction underground(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1, DensityFunction densityfunction) {
        DensityFunction densityfunction1 = getFunction(holdergetter, NoiseRouterData.SPAGHETTI_2D);
        DensityFunction densityfunction2 = getFunction(holdergetter, NoiseRouterData.SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.CAVE_LAYER), 8.0D);
        DensityFunction densityfunction4 = DensityFunctions.mul(DensityFunctions.constant(4.0D), densityfunction3.square());
        DensityFunction densityfunction5 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666D);
        DensityFunction densityfunction6 = DensityFunctions.add(DensityFunctions.add(DensityFunctions.constant(0.27D), densityfunction5).clamp(-1.0D, 1.0D), DensityFunctions.add(DensityFunctions.constant(1.5D), DensityFunctions.mul(DensityFunctions.constant(-0.64D), densityfunction)).clamp(0.0D, 0.5D));
        DensityFunction densityfunction7 = DensityFunctions.add(densityfunction4, densityfunction6);
        DensityFunction densityfunction8 = DensityFunctions.min(DensityFunctions.min(densityfunction7, getFunction(holdergetter, NoiseRouterData.ENTRANCES)), DensityFunctions.add(densityfunction1, densityfunction2));
        DensityFunction densityfunction9 = getFunction(holdergetter, NoiseRouterData.PILLARS);
        DensityFunction densityfunction10 = DensityFunctions.rangeChoice(densityfunction9, -1000000.0D, 0.03D, DensityFunctions.constant(-1000000.0D), densityfunction9);

        return DensityFunctions.max(densityfunction8, densityfunction10);
    }

    private static DensityFunction postProcess(DensityFunction densityfunction) {
        DensityFunction densityfunction1 = DensityFunctions.blendDensity(densityfunction);

        return DensityFunctions.mul(DensityFunctions.interpolated(densityfunction1), DensityFunctions.constant(0.64D)).squeeze();
    }

    private static DensityFunction remap(DensityFunction densityfunction, double d0, double d1, double d2, double d3) {
        double d4 = (d3 - d2) / (d1 - d0);
        double d5 = d2 - d0 * d4;

        return DensityFunctions.add(DensityFunctions.mul(densityfunction, DensityFunctions.constant(d4)), DensityFunctions.constant(d5));
    }

    protected static NoiseRouter overworld(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1, boolean flag, boolean flag1) {
        DensityFunction densityfunction = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_BARRIER), 0.5D);
        DensityFunction densityfunction1 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67D);
        DensityFunction densityfunction2 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143D);
        DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_LAVA));
        DensityFunction densityfunction4 = getFunction(holdergetter, NoiseRouterData.SHIFT_X);
        DensityFunction densityfunction5 = getFunction(holdergetter, NoiseRouterData.SHIFT_Z);
        DensityFunction densityfunction6 = DensityFunctions.shiftedNoise2d(densityfunction4, densityfunction5, 0.25D, holdergetter1.getOrThrow(flag ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE));
        DensityFunction densityfunction7 = DensityFunctions.shiftedNoise2d(densityfunction4, densityfunction5, 0.25D, holdergetter1.getOrThrow(flag ? Noises.VEGETATION_LARGE : Noises.VEGETATION));
        DensityFunction densityfunction8 = getFunction(holdergetter, flag ? NoiseRouterData.OFFSET_LARGE : (flag1 ? NoiseRouterData.OFFSET_AMPLIFIED : NoiseRouterData.OFFSET));
        DensityFunction densityfunction9 = getFunction(holdergetter, flag ? NoiseRouterData.FACTOR_LARGE : (flag1 ? NoiseRouterData.FACTOR_AMPLIFIED : NoiseRouterData.FACTOR));
        DensityFunction densityfunction10 = getFunction(holdergetter, flag ? NoiseRouterData.DEPTH_LARGE : (flag1 ? NoiseRouterData.DEPTH_AMPLIFIED : NoiseRouterData.DEPTH));
        DensityFunction densityfunction11 = preliminarySurfaceLevel(densityfunction8, densityfunction9, flag1);
        DensityFunction densityfunction12 = getFunction(holdergetter, flag ? NoiseRouterData.SLOPED_CHEESE_LARGE : (flag1 ? NoiseRouterData.SLOPED_CHEESE_AMPLIFIED : NoiseRouterData.SLOPED_CHEESE));
        DensityFunction densityfunction13 = DensityFunctions.min(densityfunction12, DensityFunctions.mul(DensityFunctions.constant(5.0D), getFunction(holdergetter, NoiseRouterData.ENTRANCES)));
        DensityFunction densityfunction14 = DensityFunctions.rangeChoice(densityfunction12, -1000000.0D, 1.5625D, densityfunction13, underground(holdergetter, holdergetter1, densityfunction12));
        DensityFunction densityfunction15 = DensityFunctions.min(postProcess(slideOverworld(flag1, densityfunction14)), getFunction(holdergetter, NoiseRouterData.NOODLE));
        DensityFunction densityfunction16 = getFunction(holdergetter, NoiseRouterData.Y);
        int i = Stream.of(OreVeinifier.a.values()).mapToInt((oreveinifier_a) -> {
            return oreveinifier_a.minY;
        }).min().orElse(-DimensionManager.MIN_Y * 2);
        int j = Stream.of(OreVeinifier.a.values()).mapToInt((oreveinifier_a) -> {
            return oreveinifier_a.maxY;
        }).max().orElse(-DimensionManager.MIN_Y * 2);
        DensityFunction densityfunction17 = yLimitedInterpolatable(densityfunction16, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_VEININESS), 1.5D, 1.5D), i, j, 0);
        float f = 4.0F;
        DensityFunction densityfunction18 = yLimitedInterpolatable(densityfunction16, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_VEIN_A), 4.0D, 4.0D), i, j, 0).abs();
        DensityFunction densityfunction19 = yLimitedInterpolatable(densityfunction16, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_VEIN_B), 4.0D, 4.0D), i, j, 0).abs();
        DensityFunction densityfunction20 = DensityFunctions.add(DensityFunctions.constant((double) -0.08F), DensityFunctions.max(densityfunction18, densityfunction19));
        DensityFunction densityfunction21 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_GAP));

        return new NoiseRouter(densityfunction, densityfunction1, densityfunction2, densityfunction3, densityfunction6, densityfunction7, getFunction(holdergetter, flag ? NoiseRouterData.CONTINENTS_LARGE : NoiseRouterData.CONTINENTS), getFunction(holdergetter, flag ? NoiseRouterData.EROSION_LARGE : NoiseRouterData.EROSION), densityfunction10, getFunction(holdergetter, NoiseRouterData.RIDGES), densityfunction11, densityfunction15, densityfunction17, densityfunction20, densityfunction21);
    }

    private static NoiseRouter noNewCaves(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1, DensityFunction densityfunction) {
        DensityFunction densityfunction1 = getFunction(holdergetter, NoiseRouterData.SHIFT_X);
        DensityFunction densityfunction2 = getFunction(holdergetter, NoiseRouterData.SHIFT_Z);
        DensityFunction densityfunction3 = DensityFunctions.shiftedNoise2d(densityfunction1, densityfunction2, 0.25D, holdergetter1.getOrThrow(Noises.TEMPERATURE));
        DensityFunction densityfunction4 = DensityFunctions.shiftedNoise2d(densityfunction1, densityfunction2, 0.25D, holdergetter1.getOrThrow(Noises.VEGETATION));
        DensityFunction densityfunction5 = postProcess(densityfunction);

        return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction3, densityfunction4, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction5, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    private static DensityFunction slideOverworld(boolean flag, DensityFunction densityfunction) {
        return slide(densityfunction, -64, 384, flag ? 16 : 80, flag ? 0 : 64, -0.078125D, 0, 24, flag ? 0.4D : 0.1171875D);
    }

    private static DensityFunction slideNetherLike(HolderGetter<DensityFunction> holdergetter, int i, int j) {
        return slide(getFunction(holdergetter, NoiseRouterData.BASE_3D_NOISE_NETHER), i, j, 24, 0, 0.9375D, -8, 24, 2.5D);
    }

    private static DensityFunction slideEndLike(DensityFunction densityfunction, int i, int j) {
        return slide(densityfunction, i, j, 72, -184, -23.4375D, 4, 32, -0.234375D);
    }

    protected static NoiseRouter nether(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1) {
        return noNewCaves(holdergetter, holdergetter1, slideNetherLike(holdergetter, 0, 128));
    }

    protected static NoiseRouter caves(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1) {
        return noNewCaves(holdergetter, holdergetter1, slideNetherLike(holdergetter, -64, 192));
    }

    protected static NoiseRouter floatingIslands(HolderGetter<DensityFunction> holdergetter, HolderGetter<NoiseGeneratorNormal.a> holdergetter1) {
        return noNewCaves(holdergetter, holdergetter1, slideEndLike(getFunction(holdergetter, NoiseRouterData.BASE_3D_NOISE_END), 0, 256));
    }

    private static DensityFunction slideEnd(DensityFunction densityfunction) {
        return slideEndLike(densityfunction, 0, 128);
    }

    protected static NoiseRouter end(HolderGetter<DensityFunction> holdergetter) {
        DensityFunction densityfunction = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
        DensityFunction densityfunction1 = postProcess(slideEnd(getFunction(holdergetter, NoiseRouterData.SLOPED_CHEESE_END)));

        return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction1, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    protected static NoiseRouter none() {
        return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    private static DensityFunction splineWithBlending(DensityFunction densityfunction, DensityFunction densityfunction1) {
        DensityFunction densityfunction2 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityfunction1, densityfunction);

        return DensityFunctions.flatCache(DensityFunctions.cache2d(densityfunction2));
    }

    private static DensityFunction noiseGradientDensity(DensityFunction densityfunction, DensityFunction densityfunction1) {
        DensityFunction densityfunction2 = DensityFunctions.mul(densityfunction1, densityfunction);

        return DensityFunctions.mul(DensityFunctions.constant(4.0D), densityfunction2.quarterNegative());
    }

    private static DensityFunction preliminarySurfaceLevel(DensityFunction densityfunction, DensityFunction densityfunction1, boolean flag) {
        DensityFunction densityfunction2 = DensityFunctions.cache2d(densityfunction1);
        DensityFunction densityfunction3 = DensityFunctions.cache2d(densityfunction);
        DensityFunction densityfunction4 = remap(DensityFunctions.add(DensityFunctions.mul(DensityFunctions.constant(0.2734375D), densityfunction2.invert()), DensityFunctions.mul(DensityFunctions.constant(-1.0D), densityfunction3)), 1.5D, -1.5D, -64.0D, 320.0D);

        densityfunction4 = densityfunction4.clamp(-40.0D, 320.0D);
        DensityFunction densityfunction5 = DensityFunctions.add(slideOverworld(flag, DensityFunctions.add(noiseGradientDensity(densityfunction2, offsetToDepth(densityfunction3)), DensityFunctions.constant(-0.703125D)).clamp(-64.0D, 64.0D)), DensityFunctions.constant(-0.390625D));

        return DensityFunctions.findTopSurface(densityfunction5, densityfunction4, -64, NoiseSettings.OVERWORLD_NOISE_SETTINGS.getCellHeight());
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction densityfunction, DensityFunction densityfunction1, int i, int j, int k) {
        return DensityFunctions.interpolated(DensityFunctions.rangeChoice(densityfunction, (double) i, (double) (j + 1), densityfunction1, DensityFunctions.constant((double) k)));
    }

    private static DensityFunction slide(DensityFunction densityfunction, int i, int j, int k, int l, double d0, int i1, int j1, double d1) {
        DensityFunction densityfunction1 = DensityFunctions.yClampedGradient(i + j - k, i + j - l, 1.0D, 0.0D);
        DensityFunction densityfunction2 = DensityFunctions.lerp(densityfunction1, d0, densityfunction);
        DensityFunction densityfunction3 = DensityFunctions.yClampedGradient(i + i1, i + j1, 0.0D, 1.0D);

        densityfunction2 = DensityFunctions.lerp(densityfunction3, d1, densityfunction2);
        return densityfunction2;
    }

    protected static final class a {

        protected a() {}

        protected static double getSphaghettiRarity2D(double d0) {
            return d0 < -0.75D ? 0.5D : (d0 < -0.5D ? 0.75D : (d0 < 0.5D ? 1.0D : (d0 < 0.75D ? 2.0D : 3.0D)));
        }

        protected static double getSpaghettiRarity3D(double d0) {
            return d0 < -0.5D ? 0.75D : (d0 < 0.0D ? 1.0D : (d0 < 0.5D ? 1.5D : 2.0D));
        }
    }
}
