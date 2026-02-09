package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.IBlockState;

public class BlockStateList<O, S extends IBlockDataHolder<O, S>> {

    static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, IBlockState<?>> propertiesByName;
    private final ImmutableList<S> states;

    protected BlockStateList(Function<O, S> function, O o0, BlockStateList.b<O, S> blockstatelist_b, Map<String, IBlockState<?>> map) {
        this.owner = o0;
        this.propertiesByName = ImmutableSortedMap.copyOf(map);
        Supplier<S> supplier = () -> {
            return (IBlockDataHolder) function.apply(o0);
        };
        MapCodec<S> mapcodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

        Map.Entry<String, IBlockState<?>> map_entry;

        for (UnmodifiableIterator unmodifiableiterator = this.propertiesByName.entrySet().iterator(); unmodifiableiterator.hasNext(); mapcodec = appendPropertyCodec(mapcodec, supplier, (String) map_entry.getKey(), (IBlockState) map_entry.getValue())) {
            map_entry = (Entry) unmodifiableiterator.next();
        }

        Map<Map<IBlockState<?>, Comparable<?>>, S> map1 = Maps.newLinkedHashMap();
        List<S> list = Lists.newArrayList();
        Stream<List<Pair<IBlockState<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

        IBlockState<?> iblockstate;

        for (UnmodifiableIterator unmodifiableiterator1 = this.propertiesByName.values().iterator(); unmodifiableiterator1.hasNext();stream = stream.flatMap((list1) -> {
            return iblockstate.getPossibleValues().stream().map((comparable) -> {
                List<Pair<IBlockState<?>, Comparable<?>>> list2 = Lists.newArrayList(list1);

                list2.add(Pair.of(iblockstate, comparable));
                return list2;
            });
        })) {
            iblockstate = (IBlockState) unmodifiableiterator1.next();
        }

        stream.forEach((list1) -> {
            Reference2ObjectArrayMap<IBlockState<?>, Comparable<?>> reference2objectarraymap = new Reference2ObjectArrayMap(list1.size());

            for (Pair<IBlockState<?>, Comparable<?>> pair : list1) {
                reference2objectarraymap.put((IBlockState) pair.getFirst(), (Comparable) pair.getSecond());
            }

            S s0 = blockstatelist_b.create(o0, reference2objectarraymap, mapcodec);

            map1.put(reference2objectarraymap, s0);
            list.add(s0);
        });

        for (S s0 : list) {
            ((IBlockDataHolder) s0).populateNeighbours(map1);
        }

        this.states = ImmutableList.copyOf(list);
    }

    private static <S extends IBlockDataHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(MapCodec<S> mapcodec, Supplier<S> supplier, String s, IBlockState<T> iblockstate) {
        return Codec.mapPair(mapcodec, iblockstate.valueCodec().fieldOf(s).orElseGet((s1) -> {
        }, () -> {
            return iblockstate.value((IBlockDataHolder) supplier.get());
        })).xmap((pair) -> {
            return (IBlockDataHolder) ((IBlockDataHolder) pair.getFirst()).setValue(iblockstate, ((IBlockState.a) pair.getSecond()).value());
        }, (iblockdataholder) -> {
            return Pair.of(iblockdataholder, iblockstate.value(iblockdataholder));
        });
    }

    public ImmutableList<S> getPossibleStates() {
        return this.states;
    }

    public S any() {
        return (S) (this.states.get(0));
    }

    public O getOwner() {
        return this.owner;
    }

    public Collection<IBlockState<?>> getProperties() {
        return this.propertiesByName.values();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map(IBlockState::getName).collect(Collectors.toList())).toString();
    }

    @Nullable
    public IBlockState<?> getProperty(String s) {
        return (IBlockState) this.propertiesByName.get(s);
    }

    public static class a<O, S extends IBlockDataHolder<O, S>> {

        private final O owner;
        private final Map<String, IBlockState<?>> properties = Maps.newHashMap();

        public a(O o0) {
            this.owner = o0;
        }

        public BlockStateList.a<O, S> add(IBlockState<?>... aiblockstate) {
            for (IBlockState<?> iblockstate : aiblockstate) {
                this.validateProperty(iblockstate);
                this.properties.put(iblockstate.getName(), iblockstate);
            }

            return this;
        }

        private <T extends Comparable<T>> void validateProperty(IBlockState<T> iblockstate) {
            String s = iblockstate.getName();

            if (!BlockStateList.NAME_PATTERN.matcher(s).matches()) {
                String s1 = String.valueOf(this.owner);

                throw new IllegalArgumentException(s1 + " has invalidly named property: " + s);
            } else {
                Collection<T> collection = iblockstate.getPossibleValues();

                if (collection.size() <= 1) {
                    String s2 = String.valueOf(this.owner);

                    throw new IllegalArgumentException(s2 + " attempted use property " + s + " with <= 1 possible values");
                } else {
                    for (T t0 : collection) {
                        String s3 = iblockstate.getName(t0);

                        if (!BlockStateList.NAME_PATTERN.matcher(s3).matches()) {
                            throw new IllegalArgumentException(String.valueOf(this.owner) + " has property: " + s + " with invalidly named value: " + s3);
                        }
                    }

                    if (this.properties.containsKey(s)) {
                        String s4 = String.valueOf(this.owner);

                        throw new IllegalArgumentException(s4 + " has duplicate property: " + s);
                    }
                }
            }
        }

        public BlockStateList<O, S> create(Function<O, S> function, BlockStateList.b<O, S> blockstatelist_b) {
            return new BlockStateList<O, S>(function, this.owner, blockstatelist_b, this.properties);
        }
    }

    public interface b<O, S> {

        S create(O o0, Reference2ObjectArrayMap<IBlockState<?>, Comparable<?>> reference2objectarraymap, MapCodec<S> mapcodec);
    }
}
