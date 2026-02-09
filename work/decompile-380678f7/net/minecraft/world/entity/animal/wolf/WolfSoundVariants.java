package net.minecraft.world.entity.animal.wolf;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;

public class WolfSoundVariants {

    public static final ResourceKey<WolfSoundVariant> CLASSIC = createKey(WolfSoundVariants.a.CLASSIC);
    public static final ResourceKey<WolfSoundVariant> PUGLIN = createKey(WolfSoundVariants.a.PUGLIN);
    public static final ResourceKey<WolfSoundVariant> SAD = createKey(WolfSoundVariants.a.SAD);
    public static final ResourceKey<WolfSoundVariant> ANGRY = createKey(WolfSoundVariants.a.ANGRY);
    public static final ResourceKey<WolfSoundVariant> GRUMPY = createKey(WolfSoundVariants.a.GRUMPY);
    public static final ResourceKey<WolfSoundVariant> BIG = createKey(WolfSoundVariants.a.BIG);
    public static final ResourceKey<WolfSoundVariant> CUTE = createKey(WolfSoundVariants.a.CUTE);

    public WolfSoundVariants() {}

    private static ResourceKey<WolfSoundVariant> createKey(WolfSoundVariants.a wolfsoundvariants_a) {
        return ResourceKey.create(Registries.WOLF_SOUND_VARIANT, MinecraftKey.withDefaultNamespace(wolfsoundvariants_a.getIdentifier()));
    }

    public static void bootstrap(BootstrapContext<WolfSoundVariant> bootstrapcontext) {
        register(bootstrapcontext, WolfSoundVariants.CLASSIC, WolfSoundVariants.a.CLASSIC);
        register(bootstrapcontext, WolfSoundVariants.PUGLIN, WolfSoundVariants.a.PUGLIN);
        register(bootstrapcontext, WolfSoundVariants.SAD, WolfSoundVariants.a.SAD);
        register(bootstrapcontext, WolfSoundVariants.ANGRY, WolfSoundVariants.a.ANGRY);
        register(bootstrapcontext, WolfSoundVariants.GRUMPY, WolfSoundVariants.a.GRUMPY);
        register(bootstrapcontext, WolfSoundVariants.BIG, WolfSoundVariants.a.BIG);
        register(bootstrapcontext, WolfSoundVariants.CUTE, WolfSoundVariants.a.CUTE);
    }

    private static void register(BootstrapContext<WolfSoundVariant> bootstrapcontext, ResourceKey<WolfSoundVariant> resourcekey, WolfSoundVariants.a wolfsoundvariants_a) {
        bootstrapcontext.register(resourcekey, (WolfSoundVariant) SoundEffects.WOLF_SOUNDS.get(wolfsoundvariants_a));
    }

    public static Holder<WolfSoundVariant> pickRandomSoundVariant(IRegistryCustom iregistrycustom, RandomSource randomsource) {
        return (Holder) iregistrycustom.lookupOrThrow(Registries.WOLF_SOUND_VARIANT).getRandom(randomsource).orElseThrow();
    }

    public static enum a {

        CLASSIC("classic", ""), PUGLIN("puglin", "_puglin"), SAD("sad", "_sad"), ANGRY("angry", "_angry"), GRUMPY("grumpy", "_grumpy"), BIG("big", "_big"), CUTE("cute", "_cute");

        private final String identifier;
        private final String soundEventSuffix;

        private a(final String s, final String s1) {
            this.identifier = s;
            this.soundEventSuffix = s1;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public String getSoundEventSuffix() {
            return this.soundEventSuffix;
        }
    }
}
