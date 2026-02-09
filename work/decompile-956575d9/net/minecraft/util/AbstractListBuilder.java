package net.minecraft.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.function.UnaryOperator;

abstract class AbstractListBuilder<T, B> implements ListBuilder<T> {

    private final DynamicOps<T> ops;
    protected DataResult<B> builder = DataResult.success(this.initBuilder(), Lifecycle.stable());

    protected AbstractListBuilder(DynamicOps<T> dynamicops) {
        this.ops = dynamicops;
    }

    public DynamicOps<T> ops() {
        return this.ops;
    }

    protected abstract B initBuilder();

    protected abstract B append(B b0, T t0);

    protected abstract DataResult<T> build(B b0, T t0);

    public ListBuilder<T> add(T t0) {
        this.builder = this.builder.map((object) -> {
            return this.append(object, t0);
        });
        return this;
    }

    public ListBuilder<T> add(DataResult<T> dataresult) {
        this.builder = this.builder.apply2stable(this::append, dataresult);
        return this;
    }

    public ListBuilder<T> withErrorsFrom(DataResult<?> dataresult) {
        this.builder = this.builder.flatMap((object) -> {
            return dataresult.map((object1) -> {
                return object;
            });
        });
        return this;
    }

    public ListBuilder<T> mapError(UnaryOperator<String> unaryoperator) {
        this.builder = this.builder.mapError(unaryoperator);
        return this;
    }

    public DataResult<T> build(T t0) {
        DataResult<T> dataresult = this.builder.flatMap((object) -> {
            return this.build(object, t0);
        });

        this.builder = DataResult.success(this.initBuilder(), Lifecycle.stable());
        return dataresult;
    }
}
