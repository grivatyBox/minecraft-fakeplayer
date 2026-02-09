package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.EnumBlockRotation;

public class GameTestBatchFactory {

    private static final int MAX_TESTS_PER_BATCH = 50;
    public static final GameTestBatchFactory.a DIRECT = (holder_c, worldserver) -> {
        return Stream.of(new GameTestHarnessInfo(holder_c, EnumBlockRotation.NONE, worldserver, RetryOptions.noRetries()));
    };

    public GameTestBatchFactory() {}

    public static List<GameTestHarnessBatch> divideIntoBatches(Collection<Holder.c<GameTestInstance>> collection, GameTestBatchFactory.a gametestbatchfactory_a, WorldServer worldserver) {
        Map<Holder<TestEnvironmentDefinition>, List<GameTestHarnessInfo>> map = (Map) collection.stream().flatMap((holder_c) -> {
            return gametestbatchfactory_a.decorate(holder_c, worldserver);
        }).collect(Collectors.groupingBy((gametestharnessinfo) -> {
            return gametestharnessinfo.getTest().batch();
        }));

        return map.entrySet().stream().flatMap((entry) -> {
            Holder<TestEnvironmentDefinition> holder = (Holder) entry.getKey();
            List<GameTestHarnessInfo> list = (List) entry.getValue();

            return Streams.mapWithIndex(Lists.partition(list, 50).stream(), (list1, i) -> {
                return toGameTestBatch(list1, holder, (int) i);
            });
        }).toList();
    }

    public static GameTestHarnessRunner.b fromGameTestInfo() {
        return fromGameTestInfo(50);
    }

    public static GameTestHarnessRunner.b fromGameTestInfo(int i) {
        return (collection) -> {
            Map<Holder<TestEnvironmentDefinition>, List<GameTestHarnessInfo>> map = (Map) collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy((gametestharnessinfo) -> {
                return gametestharnessinfo.getTest().batch();
            }));

            return map.entrySet().stream().flatMap((entry) -> {
                Holder<TestEnvironmentDefinition> holder = (Holder) entry.getKey();
                List<GameTestHarnessInfo> list = (List) entry.getValue();

                return Streams.mapWithIndex(Lists.partition(list, i).stream(), (list1, j) -> {
                    return toGameTestBatch(List.copyOf(list1), holder, (int) j);
                });
            }).toList();
        };
    }

    public static GameTestHarnessBatch toGameTestBatch(Collection<GameTestHarnessInfo> collection, Holder<TestEnvironmentDefinition> holder, int i) {
        return new GameTestHarnessBatch(i, collection, holder);
    }

    @FunctionalInterface
    public interface a {

        Stream<GameTestHarnessInfo> decorate(Holder.c<GameTestInstance> holder_c, WorldServer worldserver);
    }
}
