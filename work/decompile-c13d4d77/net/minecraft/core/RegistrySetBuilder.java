package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;

public class RegistrySetBuilder {

    private final List<RegistrySetBuilder.i<?>> entries = new ArrayList();

    public RegistrySetBuilder() {}

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.c<T> holderlookup_c) {
        return new RegistrySetBuilder.c<T>(holderlookup_c) {
            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey) {
                return holderlookup_c.get(resourcekey);
            }
        };
    }

    static <T> HolderLookup.c<T> lookupFromMap(final ResourceKey<? extends IRegistry<? extends T>> resourcekey, final Lifecycle lifecycle, final Map<ResourceKey<T>, Holder.c<T>> map) {
        return new HolderLookup.c<T>() {
            @Override
            public ResourceKey<? extends IRegistry<? extends T>> key() {
                return resourcekey;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return lifecycle;
            }

            @Override
            public Optional<Holder.c<T>> get(ResourceKey<T> resourcekey1) {
                return Optional.ofNullable((Holder.c) map.get(resourcekey1));
            }

            @Override
            public Stream<Holder.c<T>> listElements() {
                return map.values().stream();
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
                return Optional.empty();
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return Stream.empty();
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends IRegistry<T>> resourcekey, Lifecycle lifecycle, RegistrySetBuilder.g<T> registrysetbuilder_g) {
        this.entries.add(new RegistrySetBuilder.i<>(resourcekey, lifecycle, registrysetbuilder_g));
        return this;
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends IRegistry<T>> resourcekey, RegistrySetBuilder.g<T> registrysetbuilder_g) {
        return this.add(resourcekey, Lifecycle.stable(), registrysetbuilder_g);
    }

    private RegistrySetBuilder.a createState(IRegistryCustom iregistrycustom) {
        RegistrySetBuilder.a registrysetbuilder_a = RegistrySetBuilder.a.create(iregistrycustom, this.entries.stream().map(RegistrySetBuilder.i::key));

        this.entries.forEach((registrysetbuilder_i) -> {
            registrysetbuilder_i.apply(registrysetbuilder_a);
        });
        return registrysetbuilder_a;
    }

    private static HolderLookup.b buildProviderWithContext(IRegistryCustom iregistrycustom, Stream<HolderLookup.c<?>> stream) {
        Stream<HolderLookup.c<?>> stream1 = iregistrycustom.registries().map((iregistrycustom_d) -> {
            return iregistrycustom_d.value().asLookup();
        });

        return HolderLookup.b.create(Stream.concat(stream1, stream));
    }

    public HolderLookup.b build(IRegistryCustom iregistrycustom) {
        RegistrySetBuilder.a registrysetbuilder_a = this.createState(iregistrycustom);
        Stream<HolderLookup.c<?>> stream = this.entries.stream().map((registrysetbuilder_i) -> {
            return registrysetbuilder_i.collectRegisteredValues(registrysetbuilder_a).buildAsLookup(registrysetbuilder_a.owner);
        });
        HolderLookup.b holderlookup_b = buildProviderWithContext(iregistrycustom, stream);

        registrysetbuilder_a.reportNotCollectedHolders();
        registrysetbuilder_a.reportUnclaimedRegisteredValues();
        registrysetbuilder_a.throwOnError();
        return holderlookup_b;
    }

    private HolderLookup.b createLazyFullPatchedRegistries(IRegistryCustom iregistrycustom, HolderLookup.b holderlookup_b, Cloner.a cloner_a, Map<ResourceKey<? extends IRegistry<?>>, RegistrySetBuilder.h<?>> map, HolderLookup.b holderlookup_b1) {
        RegistrySetBuilder.b registrysetbuilder_b = new RegistrySetBuilder.b();
        MutableObject<HolderLookup.b> mutableobject = new MutableObject();
        Stream stream = map.keySet().stream().map((resourcekey) -> {
            return this.createLazyFullPatchedRegistries(registrysetbuilder_b, cloner_a, resourcekey, holderlookup_b1, holderlookup_b, mutableobject);
        });

        Objects.requireNonNull(registrysetbuilder_b);
        List<HolderLookup.c<?>> list = (List) stream.peek(registrysetbuilder_b::add).collect(Collectors.toUnmodifiableList());
        HolderLookup.b holderlookup_b2 = buildProviderWithContext(iregistrycustom, list.stream());

        mutableobject.setValue(holderlookup_b2);
        return holderlookup_b2;
    }

    private <T> HolderLookup.c<T> createLazyFullPatchedRegistries(HolderOwner<T> holderowner, Cloner.a cloner_a, ResourceKey<? extends IRegistry<? extends T>> resourcekey, HolderLookup.b holderlookup_b, HolderLookup.b holderlookup_b1, MutableObject<HolderLookup.b> mutableobject) {
        Cloner<T> cloner = cloner_a.cloner(resourcekey);

        if (cloner == null) {
            throw new NullPointerException("No cloner for " + resourcekey.location());
        } else {
            Map<ResourceKey<T>, Holder.c<T>> map = new HashMap();
            HolderLookup.c<T> holderlookup_c = holderlookup_b.lookupOrThrow(resourcekey);

            holderlookup_c.listElements().forEach((holder_c) -> {
                ResourceKey<T> resourcekey1 = holder_c.key();
                RegistrySetBuilder.d<T> registrysetbuilder_d = new RegistrySetBuilder.d<>(holderowner, resourcekey1);

                registrysetbuilder_d.supplier = () -> {
                    return cloner.clone(holder_c.value(), holderlookup_b, (HolderLookup.b) mutableobject.getValue());
                };
                map.put(resourcekey1, registrysetbuilder_d);
            });
            HolderLookup.c<T> holderlookup_c1 = holderlookup_b1.lookupOrThrow(resourcekey);

            holderlookup_c1.listElements().forEach((holder_c) -> {
                ResourceKey<T> resourcekey1 = holder_c.key();

                map.computeIfAbsent(resourcekey1, (resourcekey2) -> {
                    RegistrySetBuilder.d<T> registrysetbuilder_d = new RegistrySetBuilder.d<>(holderowner, resourcekey1);

                    registrysetbuilder_d.supplier = () -> {
                        return cloner.clone(holder_c.value(), holderlookup_b1, (HolderLookup.b) mutableobject.getValue());
                    };
                    return registrysetbuilder_d;
                });
            });
            Lifecycle lifecycle = holderlookup_c.registryLifecycle().add(holderlookup_c1.registryLifecycle());

            return lookupFromMap(resourcekey, lifecycle, map);
        }
    }

    public RegistrySetBuilder.e buildPatch(IRegistryCustom iregistrycustom, HolderLookup.b holderlookup_b, Cloner.a cloner_a) {
        RegistrySetBuilder.a registrysetbuilder_a = this.createState(iregistrycustom);
        Map<ResourceKey<? extends IRegistry<?>>, RegistrySetBuilder.h<?>> map = new HashMap();

        this.entries.stream().map((registrysetbuilder_i) -> {
            return registrysetbuilder_i.collectRegisteredValues(registrysetbuilder_a);
        }).forEach((registrysetbuilder_h) -> {
            map.put(registrysetbuilder_h.key, registrysetbuilder_h);
        });
        Set<ResourceKey<? extends IRegistry<?>>> set = (Set) iregistrycustom.listRegistries().collect(Collectors.toUnmodifiableSet());

        holderlookup_b.listRegistries().filter((resourcekey) -> {
            return !set.contains(resourcekey);
        }).forEach((resourcekey) -> {
            map.putIfAbsent(resourcekey, new RegistrySetBuilder.h<>(resourcekey, Lifecycle.stable(), Map.of()));
        });
        Stream<HolderLookup.c<?>> stream = map.values().stream().map((registrysetbuilder_h) -> {
            return registrysetbuilder_h.buildAsLookup(registrysetbuilder_a.owner);
        });
        HolderLookup.b holderlookup_b1 = buildProviderWithContext(iregistrycustom, stream);

        registrysetbuilder_a.reportUnclaimedRegisteredValues();
        registrysetbuilder_a.throwOnError();
        HolderLookup.b holderlookup_b2 = this.createLazyFullPatchedRegistries(iregistrycustom, holderlookup_b, cloner_a, map, holderlookup_b1);

        return new RegistrySetBuilder.e(holderlookup_b2, holderlookup_b1);
    }

    private static record i<T> (ResourceKey<? extends IRegistry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.g<T> bootstrap) {

        void apply(RegistrySetBuilder.a registrysetbuilder_a) {
            this.bootstrap.run(registrysetbuilder_a.bootstapContext());
        }

        public RegistrySetBuilder.h<T> collectRegisteredValues(RegistrySetBuilder.a registrysetbuilder_a) {
            Map<ResourceKey<T>, RegistrySetBuilder.k<T>> map = new HashMap();
            Iterator iterator = registrysetbuilder_a.registeredValues.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<ResourceKey<?>, RegistrySetBuilder.f<?>> entry = (Entry) iterator.next();
                ResourceKey<?> resourcekey = (ResourceKey) entry.getKey();

                if (resourcekey.isFor(this.key)) {
                    RegistrySetBuilder.f<T> registrysetbuilder_f = (RegistrySetBuilder.f) entry.getValue();
                    Holder.c<T> holder_c = (Holder.c) registrysetbuilder_a.lookup.holders.remove(resourcekey);

                    map.put(resourcekey, new RegistrySetBuilder.k<>(registrysetbuilder_f, Optional.ofNullable(holder_c)));
                    iterator.remove();
                }
            }

            return new RegistrySetBuilder.h<>(this.key, this.lifecycle, map);
        }
    }

    @FunctionalInterface
    public interface g<T> {

        void run(BootstapContext<T> bootstapcontext);
    }

    private static record a(RegistrySetBuilder.b owner, RegistrySetBuilder.j lookup, Map<MinecraftKey, HolderGetter<?>> registries, Map<ResourceKey<?>, RegistrySetBuilder.f<?>> registeredValues, List<RuntimeException> errors) {

        public static RegistrySetBuilder.a create(IRegistryCustom iregistrycustom, Stream<ResourceKey<? extends IRegistry<?>>> stream) {
            RegistrySetBuilder.b registrysetbuilder_b = new RegistrySetBuilder.b();
            List<RuntimeException> list = new ArrayList();
            RegistrySetBuilder.j registrysetbuilder_j = new RegistrySetBuilder.j(registrysetbuilder_b);
            Builder<MinecraftKey, HolderGetter<?>> builder = ImmutableMap.builder();

            iregistrycustom.registries().forEach((iregistrycustom_d) -> {
                builder.put(iregistrycustom_d.key().location(), RegistrySetBuilder.wrapContextLookup(iregistrycustom_d.value().asLookup()));
            });
            stream.forEach((resourcekey) -> {
                builder.put(resourcekey.location(), registrysetbuilder_j);
            });
            return new RegistrySetBuilder.a(registrysetbuilder_b, registrysetbuilder_j, builder.build(), new HashMap(), list);
        }

        public <T> BootstapContext<T> bootstapContext() {
            return new BootstapContext<T>() {
                @Override
                public Holder.c<T> register(ResourceKey<T> resourcekey, T t0, Lifecycle lifecycle) {
                    RegistrySetBuilder.f<?> registrysetbuilder_f = (RegistrySetBuilder.f) a.this.registeredValues.put(resourcekey, new RegistrySetBuilder.f<>(t0, lifecycle));

                    if (registrysetbuilder_f != null) {
                        a.this.errors.add(new IllegalStateException("Duplicate registration for " + resourcekey + ", new=" + t0 + ", old=" + registrysetbuilder_f.value));
                    }

                    return a.this.lookup.getOrCreate(resourcekey);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends IRegistry<? extends S>> resourcekey) {
                    return (HolderGetter) a.this.registries.getOrDefault(resourcekey.location(), a.this.lookup);
                }
            };
        }

        public void reportUnclaimedRegisteredValues() {
            this.registeredValues.forEach((resourcekey, registrysetbuilder_f) -> {
                this.errors.add(new IllegalStateException("Orpaned value " + registrysetbuilder_f.value + " for key " + resourcekey));
            });
        }

        public void reportNotCollectedHolders() {
            Iterator iterator = this.lookup.holders.keySet().iterator();

            while (iterator.hasNext()) {
                ResourceKey<Object> resourcekey = (ResourceKey) iterator.next();

                this.errors.add(new IllegalStateException("Unreferenced key: " + resourcekey));
            }

        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalstateexception = new IllegalStateException("Errors during registry creation");
                Iterator iterator = this.errors.iterator();

                while (iterator.hasNext()) {
                    RuntimeException runtimeexception = (RuntimeException) iterator.next();

                    illegalstateexception.addSuppressed(runtimeexception);
                }

                throw illegalstateexception;
            }
        }
    }

    private static class b implements HolderOwner<Object> {

        private final Set<HolderOwner<?>> owners = Sets.newIdentityHashSet();

        b() {}

        @Override
        public boolean canSerializeIn(HolderOwner<Object> holderowner) {
            return this.owners.contains(holderowner);
        }

        public void add(HolderOwner<?> holderowner) {
            this.owners.add(holderowner);
        }

        public <T> HolderOwner<T> cast() {
            return this;
        }
    }

    public static record e(HolderLookup.b full, HolderLookup.b patches) {

    }

    private static record h<T> (ResourceKey<? extends IRegistry<? extends T>> key, Lifecycle lifecycle, Map<ResourceKey<T>, RegistrySetBuilder.k<T>> values) {

        public HolderLookup.c<T> buildAsLookup(RegistrySetBuilder.b registrysetbuilder_b) {
            Map<ResourceKey<T>, Holder.c<T>> map = (Map) this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey, (entry) -> {
                RegistrySetBuilder.k<T> registrysetbuilder_k = (RegistrySetBuilder.k) entry.getValue();
                Holder.c<T> holder_c = (Holder.c) registrysetbuilder_k.holder().orElseGet(() -> {
                    return Holder.c.createStandAlone(registrysetbuilder_b.cast(), (ResourceKey) entry.getKey());
                });

                holder_c.bindValue(registrysetbuilder_k.value().value());
                return holder_c;
            }));
            HolderLookup.c<T> holderlookup_c = RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, map);

            registrysetbuilder_b.add(holderlookup_c);
            return holderlookup_c;
        }
    }

    private static class d<T> extends Holder.c<T> {

        @Nullable
        Supplier<T> supplier;

        protected d(HolderOwner<T> holderowner, @Nullable ResourceKey<T> resourcekey) {
            super(Holder.c.a.STAND_ALONE, holderowner, resourcekey, (Object) null);
        }

        @Override
        protected void bindValue(T t0) {
            super.bindValue(t0);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.bindValue(this.supplier.get());
            }

            return super.value();
        }
    }

    private static record k<T> (RegistrySetBuilder.f<T> value, Optional<Holder.c<T>> holder) {

    }

    private static record f<T> (T value, Lifecycle lifecycle) {

    }

    private static class j extends RegistrySetBuilder.c<Object> {

        final Map<ResourceKey<Object>, Holder.c<Object>> holders = new HashMap();

        public j(HolderOwner<Object> holderowner) {
            super(holderowner);
        }

        @Override
        public Optional<Holder.c<Object>> get(ResourceKey<Object> resourcekey) {
            return Optional.of(this.getOrCreate(resourcekey));
        }

        <T> Holder.c<T> getOrCreate(ResourceKey<T> resourcekey) {
            return (Holder.c) this.holders.computeIfAbsent(resourcekey, (resourcekey1) -> {
                return Holder.c.createStandAlone(this.owner, resourcekey1);
            });
        }
    }

    private abstract static class c<T> implements HolderGetter<T> {

        protected final HolderOwner<T> owner;

        protected c(HolderOwner<T> holderowner) {
            this.owner = holderowner;
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
            return Optional.of(HolderSet.emptyNamed(this.owner, tagkey));
        }
    }
}
