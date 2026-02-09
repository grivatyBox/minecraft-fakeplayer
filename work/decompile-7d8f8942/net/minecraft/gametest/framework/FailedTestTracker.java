package net.minecraft.gametest.framework;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;

public class FailedTestTracker {

    private static final Set<Holder.c<GameTestInstance>> LAST_FAILED_TESTS = Sets.newHashSet();

    public FailedTestTracker() {}

    public static Stream<Holder.c<GameTestInstance>> getLastFailedTests() {
        return FailedTestTracker.LAST_FAILED_TESTS.stream();
    }

    public static void rememberFailedTest(Holder.c<GameTestInstance> holder_c) {
        FailedTestTracker.LAST_FAILED_TESTS.add(holder_c);
    }

    public static void forgetFailedTests() {
        FailedTestTracker.LAST_FAILED_TESTS.clear();
    }
}
