package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.MathHelper;

public interface CriterionConditionValue<T extends Number & Comparable<T>> {

    SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.range.empty"));
    SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.range.swapped"));

    CriterionConditionValue.a<T> bounds();

    default Optional<T> min() {
        return this.bounds().min;
    }

    default Optional<T> max() {
        return this.bounds().max;
    }

    default boolean isAny() {
        return this.bounds().isAny();
    }

    public static record IntegerRange(CriterionConditionValue.a<Integer> bounds, CriterionConditionValue.a<Long> boundsSqr) implements CriterionConditionValue<Integer> {

        public static final CriterionConditionValue.IntegerRange ANY = new CriterionConditionValue.IntegerRange(CriterionConditionValue.a.any());
        public static final Codec<CriterionConditionValue.IntegerRange> CODEC = CriterionConditionValue.a.createCodec(Codec.INT).validate(CriterionConditionValue.a::validateSwappedBoundsInCodec).xmap(CriterionConditionValue.IntegerRange::new, CriterionConditionValue.IntegerRange::bounds);
        public static final StreamCodec<ByteBuf, CriterionConditionValue.IntegerRange> STREAM_CODEC = CriterionConditionValue.a.createStreamCodec(ByteBufCodecs.INT).map(CriterionConditionValue.IntegerRange::new, CriterionConditionValue.IntegerRange::bounds);

        private IntegerRange(CriterionConditionValue.a<Integer> criterionconditionvalue_a) {
            this(criterionconditionvalue_a, criterionconditionvalue_a.map((integer) -> {
                return MathHelper.square(integer.longValue());
            }));
        }

        public static CriterionConditionValue.IntegerRange exactly(int i) {
            return new CriterionConditionValue.IntegerRange(CriterionConditionValue.a.exactly(i));
        }

        public static CriterionConditionValue.IntegerRange between(int i, int j) {
            return new CriterionConditionValue.IntegerRange(CriterionConditionValue.a.between(i, j));
        }

        public static CriterionConditionValue.IntegerRange atLeast(int i) {
            return new CriterionConditionValue.IntegerRange(CriterionConditionValue.a.atLeast(i));
        }

        public static CriterionConditionValue.IntegerRange atMost(int i) {
            return new CriterionConditionValue.IntegerRange(CriterionConditionValue.a.atMost(i));
        }

        public boolean matches(int i) {
            return this.bounds.min.isPresent() && (Integer) this.bounds.min.get() > i ? false : this.bounds.max.isEmpty() || (Integer) this.bounds.max.get() >= i;
        }

        public boolean matchesSqr(long i) {
            return this.boundsSqr.min.isPresent() && (Long) this.boundsSqr.min.get() > i ? false : this.boundsSqr.max.isEmpty() || (Long) this.boundsSqr.max.get() >= i;
        }

        public static CriterionConditionValue.IntegerRange fromReader(StringReader stringreader) throws CommandSyntaxException {
            int i = stringreader.getCursor();
            Function function = Integer::parseInt;
            BuiltInExceptionProvider builtinexceptionprovider = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

            Objects.requireNonNull(builtinexceptionprovider);
            CriterionConditionValue.a<Integer> criterionconditionvalue_a = CriterionConditionValue.a.<Integer>fromReader(stringreader, function, builtinexceptionprovider::readerInvalidInt);

            if (criterionconditionvalue_a.areSwapped()) {
                stringreader.setCursor(i);
                throw CriterionConditionValue.IntegerRange.ERROR_SWAPPED.createWithContext(stringreader);
            } else {
                return new CriterionConditionValue.IntegerRange(criterionconditionvalue_a);
            }
        }
    }

    public static record DoubleRange(CriterionConditionValue.a<Double> bounds, CriterionConditionValue.a<Double> boundsSqr) implements CriterionConditionValue<Double> {

        public static final CriterionConditionValue.DoubleRange ANY = new CriterionConditionValue.DoubleRange(CriterionConditionValue.a.any());
        public static final Codec<CriterionConditionValue.DoubleRange> CODEC = CriterionConditionValue.a.createCodec(Codec.DOUBLE).validate(CriterionConditionValue.a::validateSwappedBoundsInCodec).xmap(CriterionConditionValue.DoubleRange::new, CriterionConditionValue.DoubleRange::bounds);
        public static final StreamCodec<ByteBuf, CriterionConditionValue.DoubleRange> STREAM_CODEC = CriterionConditionValue.a.createStreamCodec(ByteBufCodecs.DOUBLE).map(CriterionConditionValue.DoubleRange::new, CriterionConditionValue.DoubleRange::bounds);

        private DoubleRange(CriterionConditionValue.a<Double> criterionconditionvalue_a) {
            this(criterionconditionvalue_a, criterionconditionvalue_a.map(MathHelper::square));
        }

        public static CriterionConditionValue.DoubleRange exactly(double d0) {
            return new CriterionConditionValue.DoubleRange(CriterionConditionValue.a.exactly(d0));
        }

        public static CriterionConditionValue.DoubleRange between(double d0, double d1) {
            return new CriterionConditionValue.DoubleRange(CriterionConditionValue.a.between(d0, d1));
        }

        public static CriterionConditionValue.DoubleRange atLeast(double d0) {
            return new CriterionConditionValue.DoubleRange(CriterionConditionValue.a.atLeast(d0));
        }

        public static CriterionConditionValue.DoubleRange atMost(double d0) {
            return new CriterionConditionValue.DoubleRange(CriterionConditionValue.a.atMost(d0));
        }

        public boolean matches(double d0) {
            return this.bounds.min.isPresent() && (Double) this.bounds.min.get() > d0 ? false : this.bounds.max.isEmpty() || (Double) this.bounds.max.get() >= d0;
        }

        public boolean matchesSqr(double d0) {
            return this.boundsSqr.min.isPresent() && (Double) this.boundsSqr.min.get() > d0 ? false : this.boundsSqr.max.isEmpty() || (Double) this.boundsSqr.max.get() >= d0;
        }

        public static CriterionConditionValue.DoubleRange fromReader(StringReader stringreader) throws CommandSyntaxException {
            int i = stringreader.getCursor();
            Function function = Double::parseDouble;
            BuiltInExceptionProvider builtinexceptionprovider = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

            Objects.requireNonNull(builtinexceptionprovider);
            CriterionConditionValue.a<Double> criterionconditionvalue_a = CriterionConditionValue.a.<Double>fromReader(stringreader, function, builtinexceptionprovider::readerInvalidDouble);

            if (criterionconditionvalue_a.areSwapped()) {
                stringreader.setCursor(i);
                throw CriterionConditionValue.DoubleRange.ERROR_SWAPPED.createWithContext(stringreader);
            } else {
                return new CriterionConditionValue.DoubleRange(criterionconditionvalue_a);
            }
        }
    }

    public static record c(CriterionConditionValue.a<Float> bounds) implements CriterionConditionValue<Float> {

        public static final CriterionConditionValue.c ANY = new CriterionConditionValue.c(CriterionConditionValue.a.any());
        public static final Codec<CriterionConditionValue.c> CODEC = CriterionConditionValue.a.createCodec(Codec.FLOAT).xmap(CriterionConditionValue.c::new, CriterionConditionValue.c::bounds);
        public static final StreamCodec<ByteBuf, CriterionConditionValue.c> STREAM_CODEC = CriterionConditionValue.a.createStreamCodec(ByteBufCodecs.FLOAT).map(CriterionConditionValue.c::new, CriterionConditionValue.c::bounds);

        public static CriterionConditionValue.c fromReader(StringReader stringreader) throws CommandSyntaxException {
            Function function = Float::parseFloat;
            BuiltInExceptionProvider builtinexceptionprovider = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

            Objects.requireNonNull(builtinexceptionprovider);
            CriterionConditionValue.a<Float> criterionconditionvalue_a = CriterionConditionValue.a.<Float>fromReader(stringreader, function, builtinexceptionprovider::readerInvalidFloat);

            return new CriterionConditionValue.c(criterionconditionvalue_a);
        }
    }

    public static record a<T extends Number & Comparable<T>>(Optional<T> min, Optional<T> max) {

        public boolean isAny() {
            return this.min().isEmpty() && this.max().isEmpty();
        }

        public DataResult<CriterionConditionValue.a<T>> validateSwappedBoundsInCodec() {
            return this.areSwapped() ? DataResult.error(() -> {
                String s = String.valueOf(this.min());

                return "Swapped bounds in range: " + s + " is higher than " + String.valueOf(this.max());
            }) : DataResult.success(this);
        }

        public boolean areSwapped() {
            return this.min.isPresent() && this.max.isPresent() && ((Comparable) ((Number) this.min.get())).compareTo((Number) this.max.get()) > 0;
        }

        public Optional<T> asPoint() {
            Optional<T> optional = this.min();
            Optional<T> optional1 = this.max();

            return optional.equals(optional1) ? optional : Optional.empty();
        }

        public static <T extends Number & Comparable<T>> CriterionConditionValue.a<T> any() {
            return new CriterionConditionValue.a<T>(Optional.empty(), Optional.empty());
        }

        public static <T extends Number & Comparable<T>> CriterionConditionValue.a<T> exactly(T t0) {
            Optional<T> optional = Optional.of(t0);

            return new CriterionConditionValue.a<T>(optional, optional);
        }

        public static <T extends Number & Comparable<T>> CriterionConditionValue.a<T> between(T t0, T t1) {
            return new CriterionConditionValue.a<T>(Optional.of(t0), Optional.of(t1));
        }

        public static <T extends Number & Comparable<T>> CriterionConditionValue.a<T> atLeast(T t0) {
            return new CriterionConditionValue.a<T>(Optional.of(t0), Optional.empty());
        }

        public static <T extends Number & Comparable<T>> CriterionConditionValue.a<T> atMost(T t0) {
            return new CriterionConditionValue.a<T>(Optional.empty(), Optional.of(t0));
        }

        public <U extends Number & Comparable<U>> CriterionConditionValue.a<U> map(Function<T, U> function) {
            return new CriterionConditionValue.a<U>(this.min.map(function), this.max.map(function));
        }

        static <T extends Number & Comparable<T>> Codec<CriterionConditionValue.a<T>> createCodec(Codec<T> codec) {
            Codec<CriterionConditionValue.a<T>> codec1 = RecordCodecBuilder.create((instance) -> {
                return instance.group(codec.optionalFieldOf("min").forGetter(CriterionConditionValue.a::min), codec.optionalFieldOf("max").forGetter(CriterionConditionValue.a::max)).apply(instance, CriterionConditionValue.a::new);
            });

            return Codec.either(codec1, codec).xmap((either) -> {
                return (CriterionConditionValue.a) either.map((criterionconditionvalue_a) -> {
                    return criterionconditionvalue_a;
                }, (object) -> {
                    return exactly((Number) object);
                });
            }, (criterionconditionvalue_a) -> {
                Optional<T> optional = criterionconditionvalue_a.asPoint();

                return optional.isPresent() ? Either.right((Number) optional.get()) : Either.left(criterionconditionvalue_a);
            });
        }

        static <B extends ByteBuf, T extends Number & Comparable<T>> StreamCodec<B, CriterionConditionValue.a<T>> createStreamCodec(final StreamCodec<B, T> streamcodec) {
            return new StreamCodec<B, CriterionConditionValue.a<T>>() {
                private static final int MIN_FLAG = 1;
                private static final int MAX_FLAG = 2;

                public CriterionConditionValue.a<T> decode(B b0) {
                    byte b1 = b0.readByte();
                    Optional<T> optional = (b1 & 1) != 0 ? Optional.of((Number) streamcodec.decode(b0)) : Optional.empty();
                    Optional<T> optional1 = (b1 & 2) != 0 ? Optional.of((Number) streamcodec.decode(b0)) : Optional.empty();

                    return new CriterionConditionValue.a<T>(optional, optional1);
                }

                public void encode(B b0, CriterionConditionValue.a<T> criterionconditionvalue_a) {
                    Optional<T> optional = criterionconditionvalue_a.min();
                    Optional<T> optional1 = criterionconditionvalue_a.max();

                    b0.writeByte((optional.isPresent() ? 1 : 0) | (optional1.isPresent() ? 2 : 0));
                    optional.ifPresent((number) -> {
                        streamcodec.encode(b0, number);
                    });
                    optional1.ifPresent((number) -> {
                        streamcodec.encode(b0, number);
                    });
                }
            };
        }

        public static <T extends Number & Comparable<T>> CriterionConditionValue.a<T> fromReader(StringReader stringreader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
            if (!stringreader.canRead()) {
                throw CriterionConditionValue.ERROR_EMPTY.createWithContext(stringreader);
            } else {
                int i = stringreader.getCursor();

                try {
                    Optional<T> optional = readNumber(stringreader, function, supplier);
                    Optional<T> optional1;

                    if (stringreader.canRead(2) && stringreader.peek() == '.' && stringreader.peek(1) == '.') {
                        stringreader.skip();
                        stringreader.skip();
                        optional1 = readNumber(stringreader, function, supplier);
                    } else {
                        optional1 = optional;
                    }

                    if (optional.isEmpty() && optional1.isEmpty()) {
                        throw CriterionConditionValue.ERROR_EMPTY.createWithContext(stringreader);
                    } else {
                        return new CriterionConditionValue.a<T>(optional, optional1);
                    }
                } catch (CommandSyntaxException commandsyntaxexception) {
                    stringreader.setCursor(i);
                    throw new CommandSyntaxException(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i);
                }
            }
        }

        private static <T extends Number> Optional<T> readNumber(StringReader stringreader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
            int i = stringreader.getCursor();

            while (stringreader.canRead() && isAllowedInputChar(stringreader)) {
                stringreader.skip();
            }

            String s = stringreader.getString().substring(i, stringreader.getCursor());

            if (s.isEmpty()) {
                return Optional.empty();
            } else {
                try {
                    return Optional.of((Number) function.apply(s));
                } catch (NumberFormatException numberformatexception) {
                    throw ((DynamicCommandExceptionType) supplier.get()).createWithContext(stringreader, s);
                }
            }
        }

        private static boolean isAllowedInputChar(StringReader stringreader) {
            char c0 = stringreader.peek();

            return (c0 < '0' || c0 > '9') && c0 != '-' ? (c0 != '.' ? false : !stringreader.canRead(2) || stringreader.peek(1) != '.') : true;
        }
    }
}
