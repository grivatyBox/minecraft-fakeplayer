package net.minecraft.world.item.equipment.trim;

import net.minecraft.SystemUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class TrimPatterns {

    public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = registryKey("host");
    public static final ResourceKey<TrimPattern> FLOW = registryKey("flow");
    public static final ResourceKey<TrimPattern> BOLT = registryKey("bolt");

    public TrimPatterns() {}

    public static void bootstrap(BootstrapContext<TrimPattern> bootstrapcontext) {
        register(bootstrapcontext, TrimPatterns.SENTRY);
        register(bootstrapcontext, TrimPatterns.DUNE);
        register(bootstrapcontext, TrimPatterns.COAST);
        register(bootstrapcontext, TrimPatterns.WILD);
        register(bootstrapcontext, TrimPatterns.WARD);
        register(bootstrapcontext, TrimPatterns.EYE);
        register(bootstrapcontext, TrimPatterns.VEX);
        register(bootstrapcontext, TrimPatterns.TIDE);
        register(bootstrapcontext, TrimPatterns.SNOUT);
        register(bootstrapcontext, TrimPatterns.RIB);
        register(bootstrapcontext, TrimPatterns.SPIRE);
        register(bootstrapcontext, TrimPatterns.WAYFINDER);
        register(bootstrapcontext, TrimPatterns.SHAPER);
        register(bootstrapcontext, TrimPatterns.SILENCE);
        register(bootstrapcontext, TrimPatterns.RAISER);
        register(bootstrapcontext, TrimPatterns.HOST);
        register(bootstrapcontext, TrimPatterns.FLOW);
        register(bootstrapcontext, TrimPatterns.BOLT);
    }

    public static void register(BootstrapContext<TrimPattern> bootstrapcontext, ResourceKey<TrimPattern> resourcekey) {
        TrimPattern trimpattern = new TrimPattern(defaultAssetId(resourcekey), IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("trim_pattern", resourcekey.location())), false);

        bootstrapcontext.register(resourcekey, trimpattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String s) {
        return ResourceKey.create(Registries.TRIM_PATTERN, MinecraftKey.withDefaultNamespace(s));
    }

    public static MinecraftKey defaultAssetId(ResourceKey<TrimPattern> resourcekey) {
        return resourcekey.location();
    }
}
