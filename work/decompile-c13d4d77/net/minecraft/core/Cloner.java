package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.JavaOps;

public class Cloner<T> {

    private final Codec<T> directCodec;

    Cloner(Codec<T> codec) {
        this.directCodec = codec;
    }

    public T clone(T t0, HolderLookup.b holderlookup_b, HolderLookup.b holderlookup_b1) {
        DynamicOps<Object> dynamicops = RegistryOps.create(JavaOps.INSTANCE, holderlookup_b);
        DynamicOps<Object> dynamicops1 = RegistryOps.create(JavaOps.INSTANCE, holderlookup_b1);
        Object object = SystemUtils.getOrThrow(this.directCodec.encodeStart(dynamicops, t0), (s) -> {
            return new IllegalStateException("Failed to encode: " + s);
        });

        return SystemUtils.getOrThrow(this.directCodec.parse(dynamicops1, object), (s) -> {
            return new IllegalStateException("Failed to decode: " + s);
        });
    }

    public static class a {

        private final Map<ResourceKey<? extends IRegistry<?>>, Cloner<?>> codecs = new HashMap();

        public a() {}

        public <T> Cloner.a addCodec(ResourceKey<? extends IRegistry<? extends T>> resourcekey, Codec<T> codec) {
            this.codecs.put(resourcekey, new Cloner<>(codec));
            return this;
        }

        @Nullable
        public <T> Cloner<T> cloner(ResourceKey<? extends IRegistry<? extends T>> resourcekey) {
            return (Cloner) this.codecs.get(resourcekey);
        }
    }
}
