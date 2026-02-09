package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class FeatureSorter {

    public FeatureSorter() {}

    public static <T> List<FeatureSorter.b> buildFeaturesPerStep(List<T> list, Function<T, List<HolderSet<PlacedFeature>>> function, boolean flag) {
        Object2IntMap<PlacedFeature> object2intmap = new Object2IntOpenHashMap();
        MutableInt mutableint = new MutableInt(0);

        record a(int featureIndex, int step, PlacedFeature feature) {

        }

        Comparator<a> comparator = Comparator.comparingInt(a::step).thenComparingInt(a::featureIndex);
        Map<a, Set<a>> map = new TreeMap(comparator);
        int i = 0;

        for (T t0 : list) {
            List<a> list1 = Lists.newArrayList();
            List<HolderSet<PlacedFeature>> list2 = (List) function.apply(t0);

            i = Math.max(i, list2.size());

            for (int j = 0; j < list2.size(); ++j) {
                for (Holder<PlacedFeature> holder : (HolderSet) list2.get(j)) {
                    PlacedFeature placedfeature = holder.value();

                    list1.add(new a(object2intmap.computeIfAbsent(placedfeature, (object) -> {
                        return mutableint.getAndIncrement();
                    }), j, placedfeature));
                }
            }

            for (int k = 0; k < ((List) list1).size(); ++k) {
                Set<a> set = (Set) map.computeIfAbsent((a) list1.get(k), (a0) -> {
                    return new TreeSet(comparator);
                });

                if (k < list1.size() - 1) {
                    set.add((a) list1.get(k + 1));
                }
            }
        }

        Set<a> set1 = new TreeSet(comparator);
        Set<a> set2 = new TreeSet(comparator);
        List<a> list3 = Lists.newArrayList();

        for (a a0 : map.keySet()) {
            if (!set2.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }

            if (!set1.contains(a0)) {
                Objects.requireNonNull(list3);
                if (Graph.depthFirstSearch(map, set1, set2, list3::add, a0)) {
                    if (!flag) {
                        throw new IllegalStateException("Feature order cycle found");
                    }

                    List<T> list4 = new ArrayList(list);

                    int l;

                    do {
                        l = list4.size();
                        ListIterator<T> listiterator = list4.listIterator();

                        while (listiterator.hasNext()) {
                            T t1 = (T) listiterator.next();

                            listiterator.remove();

                            try {
                                buildFeaturesPerStep(list4, function, false);
                            } catch (IllegalStateException illegalstateexception) {
                                continue;
                            }

                            listiterator.add(t1);
                        }
                    } while (l != ((List) list4).size());

                    throw new IllegalStateException("Feature order cycle found, involved sources: " + String.valueOf(list4));
                }
            }
        }

        Collections.reverse(list3);
        ImmutableList.Builder<FeatureSorter.b> immutablelist_builder = ImmutableList.builder();

        for (int i1 = 0; i1 < i; ++i1) {
            List<PlacedFeature> list5 = (List) list3.stream().filter((a1) -> {
                return a1.step() == i1;
            }).map(a::feature).collect(Collectors.toList());

            immutablelist_builder.add(new FeatureSorter.b(list5));
        }

        return immutablelist_builder.build();
    }

    public static record b(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {

        b(List<PlacedFeature> list) {
            this(list, SystemUtils.createIndexIdentityLookup(list));
        }
    }
}
