package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.SystemUtils;
import net.minecraft.util.RandomSource;

public class WeightedRandom2 {

    private WeightedRandom2() {}

    public static <T> int getTotalWeight(List<T> list, ToIntFunction<T> tointfunction) {
        long i = 0L;

        for (T t0 : list) {
            i += (long) tointfunction.applyAsInt(t0);
        }

        if (i > 2147483647L) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        } else {
            return (int) i;
        }
    }

    public static <T> Optional<T> getRandomItem(RandomSource randomsource, List<T> list, int i, ToIntFunction<T> tointfunction) {
        if (i < 0) {
            throw (IllegalArgumentException) SystemUtils.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        } else if (i == 0) {
            return Optional.empty();
        } else {
            int j = randomsource.nextInt(i);

            return getWeightedItem(list, j, tointfunction);
        }
    }

    public static <T> Optional<T> getWeightedItem(List<T> list, int i, ToIntFunction<T> tointfunction) {
        for (T t0 : list) {
            i -= tointfunction.applyAsInt(t0);
            if (i < 0) {
                return Optional.of(t0);
            }
        }

        return Optional.empty();
    }

    public static <T> Optional<T> getRandomItem(RandomSource randomsource, List<T> list, ToIntFunction<T> tointfunction) {
        return getRandomItem(randomsource, list, getTotalWeight(list, tointfunction), tointfunction);
    }
}
