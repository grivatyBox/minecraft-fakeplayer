package net.minecraft.world.level.storage;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.DataResult.Success;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTTagType;
import net.minecraft.util.ProblemReporter;

public class TagValueInput implements ValueInput {

    private final ProblemReporter problemReporter;
    private final ValueInputContextHelper context;
    public final NBTTagCompound input;

    private TagValueInput(ProblemReporter problemreporter, ValueInputContextHelper valueinputcontexthelper, NBTTagCompound nbttagcompound) {
        this.problemReporter = problemreporter;
        this.context = valueinputcontexthelper;
        this.input = nbttagcompound;
    }

    public static ValueInput create(ProblemReporter problemreporter, HolderLookup.a holderlookup_a, NBTTagCompound nbttagcompound) {
        return new TagValueInput(problemreporter, new ValueInputContextHelper(holderlookup_a, DynamicOpsNBT.INSTANCE), nbttagcompound);
    }

    public static ValueInput.b create(ProblemReporter problemreporter, HolderLookup.a holderlookup_a, List<NBTTagCompound> list) {
        return new TagValueInput.a(problemreporter, new ValueInputContextHelper(holderlookup_a, DynamicOpsNBT.INSTANCE), list);
    }

    @Override
    public <T> Optional<T> read(String s, Codec<T> codec) {
        NBTBase nbtbase = this.input.get(s);

        if (nbtbase == null) {
            return Optional.empty();
        } else {
            DataResult dataresult = codec.parse(this.context.ops(), nbtbase);

            Objects.requireNonNull(dataresult);
            DataResult dataresult1 = dataresult;
            byte b0 = 0;
            Optional optional;

            //$FF: b0->value
            //0->com/mojang/serialization/DataResult$Success
            //1->com/mojang/serialization/DataResult$Error
            switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, b0)) {
                case 0:
                    DataResult.Success<T> dataresult_success = (Success)dataresult1;

                    optional = Optional.of(dataresult_success.value());
                    break;
                case 1:
                    DataResult.Error<T> dataresult_error = (Error)dataresult1;

                    this.problemReporter.report(new TagValueInput.b(s, nbtbase, dataresult_error));
                    optional = dataresult_error.partialValue();
                    break;
                default:
                    throw new MatchException((String)null, (Throwable)null);
            }

            return optional;
        }
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> mapcodec) {
        DynamicOps<NBTBase> dynamicops = this.context.ops();
        DataResult dataresult = dynamicops.getMap(this.input).flatMap((maplike) -> {
            return mapcodec.decode(dynamicops, maplike);
        });

        Objects.requireNonNull(dataresult);
        DataResult dataresult1 = dataresult;
        byte b0 = 0;
        Optional optional;

        //$FF: b0->value
        //0->com/mojang/serialization/DataResult$Success
        //1->com/mojang/serialization/DataResult$Error
        switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, b0)) {
            case 0:
                DataResult.Success<T> dataresult_success = (Success)dataresult1;

                optional = Optional.of(dataresult_success.value());
                break;
            case 1:
                DataResult.Error<T> dataresult_error = (Error)dataresult1;

                this.problemReporter.report(new TagValueInput.d(dataresult_error));
                optional = dataresult_error.partialValue();
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        return optional;
    }

    @Nullable
    private <T extends NBTBase> T getOptionalTypedTag(String s, NBTTagType<T> nbttagtype) {
        NBTBase nbtbase = this.input.get(s);

        if (nbtbase == null) {
            return null;
        } else {
            NBTTagType<?> nbttagtype1 = nbtbase.getType();

            if (nbttagtype1 != nbttagtype) {
                this.problemReporter.report(new TagValueInput.i(s, nbttagtype, nbttagtype1));
                return null;
            } else {
                return (T) nbtbase;
            }
        }
    }

    @Nullable
    private NBTNumber getNumericTag(String s) {
        NBTBase nbtbase = this.input.get(s);

        if (nbtbase == null) {
            return null;
        } else if (nbtbase instanceof NBTNumber) {
            NBTNumber nbtnumber = (NBTNumber) nbtbase;

            return nbtnumber;
        } else {
            this.problemReporter.report(new TagValueInput.h(s, nbtbase.getType()));
            return null;
        }
    }

    @Override
    public Optional<ValueInput> child(String s) {
        NBTTagCompound nbttagcompound = (NBTTagCompound) this.getOptionalTypedTag(s, NBTTagCompound.TYPE);

        return nbttagcompound != null ? Optional.of(this.wrapChild(s, nbttagcompound)) : Optional.empty();
    }

    @Override
    public ValueInput childOrEmpty(String s) {
        NBTTagCompound nbttagcompound = (NBTTagCompound) this.getOptionalTypedTag(s, NBTTagCompound.TYPE);

        return nbttagcompound != null ? this.wrapChild(s, nbttagcompound) : this.context.empty();
    }

    @Override
    public Optional<ValueInput.b> childrenList(String s) {
        NBTTagList nbttaglist = (NBTTagList) this.getOptionalTypedTag(s, NBTTagList.TYPE);

        return nbttaglist != null ? Optional.of(this.wrapList(s, this.context, nbttaglist)) : Optional.empty();
    }

    @Override
    public ValueInput.b childrenListOrEmpty(String s) {
        NBTTagList nbttaglist = (NBTTagList) this.getOptionalTypedTag(s, NBTTagList.TYPE);

        return nbttaglist != null ? this.wrapList(s, this.context, nbttaglist) : this.context.emptyList();
    }

    @Override
    public <T> Optional<ValueInput.a<T>> list(String s, Codec<T> codec) {
        NBTTagList nbttaglist = (NBTTagList) this.getOptionalTypedTag(s, NBTTagList.TYPE);

        return nbttaglist != null ? Optional.of(this.wrapTypedList(s, nbttaglist, codec)) : Optional.empty();
    }

    @Override
    public <T> ValueInput.a<T> listOrEmpty(String s, Codec<T> codec) {
        NBTTagList nbttaglist = (NBTTagList) this.getOptionalTypedTag(s, NBTTagList.TYPE);

        return nbttaglist != null ? this.wrapTypedList(s, nbttaglist, codec) : this.context.emptyTypedList();
    }

    @Override
    public boolean getBooleanOr(String s, boolean flag) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.byteValue() != 0 : flag;
    }

    @Override
    public byte getByteOr(String s, byte b0) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.byteValue() : b0;
    }

    @Override
    public int getShortOr(String s, short short0) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.shortValue() : short0;
    }

    @Override
    public Optional<Integer> getInt(String s) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? Optional.of(nbtnumber.intValue()) : Optional.empty();
    }

    @Override
    public int getIntOr(String s, int i) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.intValue() : i;
    }

    @Override
    public long getLongOr(String s, long i) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.longValue() : i;
    }

    @Override
    public Optional<Long> getLong(String s) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? Optional.of(nbtnumber.longValue()) : Optional.empty();
    }

    @Override
    public float getFloatOr(String s, float f) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.floatValue() : f;
    }

    @Override
    public double getDoubleOr(String s, double d0) {
        NBTNumber nbtnumber = this.getNumericTag(s);

        return nbtnumber != null ? nbtnumber.doubleValue() : d0;
    }

    @Override
    public Optional<String> getString(String s) {
        NBTTagString nbttagstring = (NBTTagString) this.getOptionalTypedTag(s, NBTTagString.TYPE);

        return nbttagstring != null ? Optional.of(nbttagstring.value()) : Optional.empty();
    }

    @Override
    public String getStringOr(String s, String s1) {
        NBTTagString nbttagstring = (NBTTagString) this.getOptionalTypedTag(s, NBTTagString.TYPE);

        return nbttagstring != null ? nbttagstring.value() : s1;
    }

    @Override
    public Optional<int[]> getIntArray(String s) {
        NBTTagIntArray nbttagintarray = (NBTTagIntArray) this.getOptionalTypedTag(s, NBTTagIntArray.TYPE);

        return nbttagintarray != null ? Optional.of(nbttagintarray.getAsIntArray()) : Optional.empty();
    }

    @Override
    public HolderLookup.a lookup() {
        return this.context.lookup();
    }

    private ValueInput wrapChild(String s, NBTTagCompound nbttagcompound) {
        return (ValueInput) (nbttagcompound.isEmpty() ? this.context.empty() : new TagValueInput(this.problemReporter.forChild(new ProblemReporter.c(s)), this.context, nbttagcompound));
    }

    static ValueInput wrapChild(ProblemReporter problemreporter, ValueInputContextHelper valueinputcontexthelper, NBTTagCompound nbttagcompound) {
        return (ValueInput) (nbttagcompound.isEmpty() ? valueinputcontexthelper.empty() : new TagValueInput(problemreporter, valueinputcontexthelper, nbttagcompound));
    }

    private ValueInput.b wrapList(String s, ValueInputContextHelper valueinputcontexthelper, NBTTagList nbttaglist) {
        return (ValueInput.b) (nbttaglist.isEmpty() ? valueinputcontexthelper.emptyList() : new TagValueInput.e(this.problemReporter, s, valueinputcontexthelper, nbttaglist));
    }

    private <T> ValueInput.a<T> wrapTypedList(String s, NBTTagList nbttaglist, Codec<T> codec) {
        return (ValueInput.a<T>) (nbttaglist.isEmpty() ? this.context.emptyTypedList() : new TagValueInput.f(this.problemReporter, s, this.context, codec, nbttaglist));
    }

    private static class e implements ValueInput.b {

        private final ProblemReporter problemReporter;
        private final String name;
        final ValueInputContextHelper context;
        private final NBTTagList list;

        e(ProblemReporter problemreporter, String s, ValueInputContextHelper valueinputcontexthelper, NBTTagList nbttaglist) {
            this.problemReporter = problemreporter;
            this.name = s;
            this.context = valueinputcontexthelper;
            this.list = nbttaglist;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        ProblemReporter reporterForChild(int i) {
            return this.problemReporter.forChild(new ProblemReporter.d(this.name, i));
        }

        void reportIndexUnwrapProblem(int i, NBTBase nbtbase) {
            this.problemReporter.report(new TagValueInput.g(this.name, i, NBTTagCompound.TYPE, nbtbase.getType()));
        }

        @Override
        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(), (nbtbase, i) -> {
                if (nbtbase instanceof NBTTagCompound nbttagcompound) {
                    return TagValueInput.wrapChild(this.reporterForChild((int) i), this.context, nbttagcompound);
                } else {
                    this.reportIndexUnwrapProblem((int) i, nbtbase);
                    return null;
                }
            }).filter(Objects::nonNull);
        }

        public Iterator<ValueInput> iterator() {
            final Iterator<NBTBase> iterator = this.list.iterator();

            return new AbstractIterator<ValueInput>() {
                private int index;

                @Nullable
                protected ValueInput computeNext() {
                    while (iterator.hasNext()) {
                        NBTBase nbtbase = (NBTBase) iterator.next();
                        int i = this.index++;

                        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
                            return TagValueInput.wrapChild(e.this.reporterForChild(i), e.this.context, nbttagcompound);
                        }

                        e.this.reportIndexUnwrapProblem(i, nbtbase);
                    }

                    return (ValueInput) this.endOfData();
                }
            };
        }
    }

    private static class f<T> implements ValueInput.a<T> {

        private final ProblemReporter problemReporter;
        private final String name;
        final ValueInputContextHelper context;
        final Codec<T> codec;
        private final NBTTagList list;

        f(ProblemReporter problemreporter, String s, ValueInputContextHelper valueinputcontexthelper, Codec<T> codec, NBTTagList nbttaglist) {
            this.problemReporter = problemreporter;
            this.name = s;
            this.context = valueinputcontexthelper;
            this.codec = codec;
            this.list = nbttaglist;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        void reportIndexUnwrapProblem(int i, NBTBase nbtbase, DataResult.Error<?> dataresult_error) {
            this.problemReporter.report(new TagValueInput.c(this.name, i, nbtbase, dataresult_error));
        }

        @Override
        public Stream<T> stream() {
            return Streams.mapWithIndex(this.list.stream(), (nbtbase, i) -> {
                DataResult dataresult = this.codec.parse(this.context.ops(), nbtbase);

                Objects.requireNonNull(dataresult);
                DataResult dataresult1 = dataresult;
                int j = 0;
                Object object;

                //$FF: j->value
                //0->com/mojang/serialization/DataResult$Success
                //1->com/mojang/serialization/DataResult$Error
                switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, j)) {
                    case 0:
                        DataResult.Success<T> dataresult_success = (Success)dataresult1;

                        object = dataresult_success.value();
                        break;
                    case 1:
                        DataResult.Error<T> dataresult_error = (Error)dataresult1;

                        this.reportIndexUnwrapProblem((int)i, nbtbase, dataresult_error);
                        object = dataresult_error.partialValue().orElse((Object)null);
                        break;
                    default:
                        throw new MatchException((String)null, (Throwable)null);
                }

                return object;
            }).filter(Objects::nonNull);
        }

        public Iterator<T> iterator() {
            final ListIterator<NBTBase> listiterator = this.list.listIterator();

            return new AbstractIterator<T>() {
                @Nullable
                protected T computeNext() {
                    while(true) {
                        if (listiterator.hasNext()) {
                            int i = listiterator.nextIndex();
                            NBTBase nbtbase = (NBTBase)listiterator.next();
                            DataResult dataresult = f.this.codec.parse(f.this.context.ops(), nbtbase);

                            Objects.requireNonNull(dataresult);
                            DataResult dataresult1 = dataresult;
                            byte b0 = 0;

                            //$FF: b0->value
                            //0->com/mojang/serialization/DataResult$Success
                            //1->com/mojang/serialization/DataResult$Error
                            switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, b0)) {
                                case 0:
                                    DataResult.Success<T> dataresult_success = (Success)dataresult1;

                                    return (T)dataresult_success.value();
                                case 1:
                                    DataResult.Error<T> dataresult_error = (Error)dataresult1;

                                    f.this.reportIndexUnwrapProblem(i, nbtbase, dataresult_error);
                                    if (!dataresult_error.partialValue().isPresent()) {
                                        continue;
                                    }

                                    return (T)dataresult_error.partialValue().get();
                                default:
                                    throw new MatchException((String)null, (Throwable)null);
                            }
                        }

                        return (T)this.endOfData();
                    }
                }
            };
        }
    }

    private static class a implements ValueInput.b {

        private final ProblemReporter problemReporter;
        private final ValueInputContextHelper context;
        private final List<NBTTagCompound> list;

        public a(ProblemReporter problemreporter, ValueInputContextHelper valueinputcontexthelper, List<NBTTagCompound> list) {
            this.problemReporter = problemreporter;
            this.context = valueinputcontexthelper;
            this.list = list;
        }

        ValueInput wrapChild(int i, NBTTagCompound nbttagcompound) {
            return TagValueInput.wrapChild(this.problemReporter.forChild(new ProblemReporter.e(i)), this.context, nbttagcompound);
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        @Override
        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(), (nbttagcompound, i) -> {
                return this.wrapChild((int) i, nbttagcompound);
            });
        }

        public Iterator<ValueInput> iterator() {
            final ListIterator<NBTTagCompound> listiterator = this.list.listIterator();

            return new AbstractIterator<ValueInput>() {
                @Nullable
                protected ValueInput computeNext() {
                    if (listiterator.hasNext()) {
                        int i = listiterator.nextIndex();
                        NBTTagCompound nbttagcompound = (NBTTagCompound) listiterator.next();

                        return a.this.wrapChild(i, nbttagcompound);
                    } else {
                        return (ValueInput) this.endOfData();
                    }
                }
            };
        }
    }

    public static record b(String name, NBTBase tag, DataResult.Error<?> error) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.tag);

            return "Failed to decode value '" + s + "' from field '" + this.name + "': " + this.error.message();
        }
    }

    public static record c(String name, int index, NBTBase tag, DataResult.Error<?> error) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.tag);

            return "Failed to decode value '" + s + "' from field '" + this.name + "' at index " + this.index + "': " + this.error.message();
        }
    }

    public static record d(DataResult.Error<?> error) implements ProblemReporter.g {

        @Override
        public String description() {
            return "Failed to decode from map: " + this.error.message();
        }
    }

    public static record i(String name, NBTTagType<?> expected, NBTTagType<?> actual) implements ProblemReporter.g {

        @Override
        public String description() {
            return "Expected field '" + this.name + "' to contain value of type " + this.expected.getName() + ", but got " + this.actual.getName();
        }
    }

    public static record h(String name, NBTTagType<?> actual) implements ProblemReporter.g {

        @Override
        public String description() {
            return "Expected field '" + this.name + "' to contain number, but got " + this.actual.getName();
        }
    }

    public static record g(String name, int index, NBTTagType<?> expected, NBTTagType<?> actual) implements ProblemReporter.g {

        @Override
        public String description() {
            return "Expected list '" + this.name + "' to contain at index " + this.index + " value of type " + this.expected.getName() + ", but got " + this.actual.getName();
        }
    }
}
