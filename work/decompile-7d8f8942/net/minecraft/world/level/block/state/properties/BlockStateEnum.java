package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.INamable;

public final class BlockStateEnum<T extends Enum<T> & INamable> extends IBlockState<T> {

    private final List<T> values;
    private final Map<String, T> names;
    private final int[] ordinalToIndex;

    private BlockStateEnum(String s, Class<T> oclass, List<T> list) {
        super(s, oclass);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Trying to make empty EnumProperty '" + s + "'");
        } else {
            this.values = List.copyOf(list);
            T[] at = (T[]) (oclass.getEnumConstants());

            this.ordinalToIndex = new int[at.length];

            for (T t0 : at) {
                this.ordinalToIndex[t0.ordinal()] = list.indexOf(t0);
            }

            ImmutableMap.Builder<String, T> immutablemap_builder = ImmutableMap.builder();

            for (T t1 : list) {
                String s1 = ((INamable) t1).getSerializedName();

                immutablemap_builder.put(s1, t1);
            }

            this.names = immutablemap_builder.buildOrThrow();
        }
    }

    @Override
    public List<T> getPossibleValues() {
        return this.values;
    }

    @Override
    public Optional<T> getValue(String s) {
        return Optional.ofNullable((Enum) this.names.get(s));
    }

    public String getName(T t0) {
        return ((INamable) t0).getSerializedName();
    }

    public int getInternalIndex(T t0) {
        return this.ordinalToIndex[t0.ordinal()];
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            if (object instanceof BlockStateEnum) {
                BlockStateEnum<?> blockstateenum = (BlockStateEnum) object;

                if (super.equals(object)) {
                    return this.values.equals(blockstateenum.values);
                }
            }

            return false;
        }
    }

    @Override
    public int generateHashCode() {
        int i = super.generateHashCode();

        i = 31 * i + this.values.hashCode();
        return i;
    }

    public static <T extends Enum<T> & INamable> BlockStateEnum<T> create(String s, Class<T> oclass) {
        return create(s, oclass, (oenum) -> {
            return true;
        });
    }

    public static <T extends Enum<T> & INamable> BlockStateEnum<T> create(String s, Class<T> oclass, Predicate<T> predicate) {
        return create(s, oclass, (List) Arrays.stream((Enum[]) oclass.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static <T extends Enum<T> & INamable> BlockStateEnum<T> create(String s, Class<T> oclass, T... at) {
        return create(s, oclass, List.of(at));
    }

    public static <T extends Enum<T> & INamable> BlockStateEnum<T> create(String s, Class<T> oclass, List<T> list) {
        return new BlockStateEnum<T>(s, oclass, list);
    }
}
