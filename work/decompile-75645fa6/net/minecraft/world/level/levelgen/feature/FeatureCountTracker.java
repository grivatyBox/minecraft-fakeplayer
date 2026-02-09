package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<WorldServer, FeatureCountTracker.b> data = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5L, TimeUnit.MINUTES).build(new CacheLoader<WorldServer, FeatureCountTracker.b>() {
        public FeatureCountTracker.b load(WorldServer worldserver) {
            return new FeatureCountTracker.b(Object2IntMaps.synchronize(new Object2IntOpenHashMap()), new MutableInt(0));
        }
    });

    public FeatureCountTracker() {}

    public static void chunkDecorated(WorldServer worldserver) {
        try {
            ((FeatureCountTracker.b) FeatureCountTracker.data.get(worldserver)).chunksWithFeatures().increment();
        } catch (Exception exception) {
            FeatureCountTracker.LOGGER.error("Failed to increment chunk count", exception);
        }

    }

    public static void featurePlaced(WorldServer worldserver, WorldGenFeatureConfigured<?, ?> worldgenfeatureconfigured, Optional<PlacedFeature> optional) {
        try {
            ((FeatureCountTracker.b) FeatureCountTracker.data.get(worldserver)).featureData().computeInt(new FeatureCountTracker.a(worldgenfeatureconfigured, optional), (featurecounttracker_a, integer) -> {
                return integer == null ? 1 : integer + 1;
            });
        } catch (Exception exception) {
            FeatureCountTracker.LOGGER.error("Failed to increment feature count", exception);
        }

    }

    public static void clearCounts() {
        FeatureCountTracker.data.invalidateAll();
        FeatureCountTracker.LOGGER.debug("Cleared feature counts");
    }

    public static void logCounts() {
        FeatureCountTracker.LOGGER.debug("Logging feature counts:");
        FeatureCountTracker.data.asMap().forEach((worldserver, featurecounttracker_b) -> {
            String s = worldserver.dimension().location().toString();
            boolean flag = worldserver.getServer().isRunning();
            IRegistry<PlacedFeature> iregistry = worldserver.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
            String s1 = (flag ? "running" : "dead") + " " + s;
            Integer integer = featurecounttracker_b.chunksWithFeatures().getValue();

            FeatureCountTracker.LOGGER.debug("{} total_chunks: {}", s1, integer);
            featurecounttracker_b.featureData().forEach((featurecounttracker_a, integer1) -> {
                Logger logger = FeatureCountTracker.LOGGER;
                Object[] aobject = new Object[]{s1, String.format(Locale.ROOT, "%10d", integer1), String.format(Locale.ROOT, "%10f", (double) integer1 / (double) integer), null, null, null};
                Optional optional = featurecounttracker_a.topFeature();

                Objects.requireNonNull(iregistry);
                aobject[3] = optional.flatMap(iregistry::getResourceKey).map(ResourceKey::location);
                aobject[4] = featurecounttracker_a.feature().feature();
                aobject[5] = featurecounttracker_a.feature();
                logger.debug("{} {} {} {} {} {}", aobject);
            });
        });
    }

    private static record a(WorldGenFeatureConfigured<?, ?> feature, Optional<PlacedFeature> topFeature) {

    }

    private static record b(Object2IntMap<FeatureCountTracker.a> featureData, MutableInt chunksWithFeatures) {

    }
}
