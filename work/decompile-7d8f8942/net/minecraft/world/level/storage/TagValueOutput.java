package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.DataResult.Success;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ProblemReporter;

public class TagValueOutput implements ValueOutput {

    private final ProblemReporter problemReporter;
    private final DynamicOps<NBTBase> ops;
    private final NBTTagCompound output;

    TagValueOutput(ProblemReporter problemreporter, DynamicOps<NBTBase> dynamicops, NBTTagCompound nbttagcompound) {
        this.problemReporter = problemreporter;
        this.ops = dynamicops;
        this.output = nbttagcompound;
    }

    public static TagValueOutput createWithContext(ProblemReporter problemreporter, HolderLookup.a holderlookup_a) {
        return new TagValueOutput(problemreporter, holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), new NBTTagCompound());
    }

    public static TagValueOutput createWithoutContext(ProblemReporter problemreporter) {
        return new TagValueOutput(problemreporter, DynamicOpsNBT.INSTANCE, new NBTTagCompound());
    }

    @Override
    public <T> void store(String s, Codec<T> codec, T t0) {
        DataResult dataresult = codec.encodeStart(this.ops, t0);

        Objects.requireNonNull(dataresult);
        DataResult dataresult1 = dataresult;
        byte b0 = 0;

        //$FF: b0->value
        //0->com/mojang/serialization/DataResult$Success
        //1->com/mojang/serialization/DataResult$Error
        switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, b0)) {
            case 0:
                DataResult.Success<NBTBase> dataresult_success = (Success)dataresult1;

                this.output.put(s, (NBTBase)dataresult_success.value());
                break;
            case 1:
                DataResult.Error<NBTBase> dataresult_error = (Error)dataresult1;

                this.problemReporter.report(new TagValueOutput.a(s, t0, dataresult_error));
                dataresult_error.partialValue().ifPresent((nbtbase) -> {
                    this.output.put(s, nbtbase);
                });
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

    }

    @Override
    public <T> void storeNullable(String s, Codec<T> codec, @Nullable T t0) {
        if (t0 != null) {
            this.store(s, codec, t0);
        }

    }

    @Override
    public <T> void store(MapCodec<T> mapcodec, T t0) {
        DataResult dataresult = mapcodec.encoder().encodeStart(this.ops, t0);

        Objects.requireNonNull(dataresult);
        DataResult dataresult1 = dataresult;
        byte b0 = 0;

        //$FF: b0->value
        //0->com/mojang/serialization/DataResult$Success
        //1->com/mojang/serialization/DataResult$Error
        switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, b0)) {
            case 0:
                DataResult.Success<NBTBase> dataresult_success = (Success)dataresult1;

                this.output.merge((NBTTagCompound)dataresult_success.value());
                break;
            case 1:
                DataResult.Error<NBTBase> dataresult_error = (Error)dataresult1;

                this.problemReporter.report(new TagValueOutput.c(t0, dataresult_error));
                dataresult_error.partialValue().ifPresent((nbtbase) -> {
                    this.output.merge((NBTTagCompound)nbtbase);
                });
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

    }

    @Override
    public void putBoolean(String s, boolean flag) {
        this.output.putBoolean(s, flag);
    }

    @Override
    public void putByte(String s, byte b0) {
        this.output.putByte(s, b0);
    }

    @Override
    public void putShort(String s, short short0) {
        this.output.putShort(s, short0);
    }

    @Override
    public void putInt(String s, int i) {
        this.output.putInt(s, i);
    }

    @Override
    public void putLong(String s, long i) {
        this.output.putLong(s, i);
    }

    @Override
    public void putFloat(String s, float f) {
        this.output.putFloat(s, f);
    }

    @Override
    public void putDouble(String s, double d0) {
        this.output.putDouble(s, d0);
    }

    @Override
    public void putString(String s, String s1) {
        this.output.putString(s, s1);
    }

    @Override
    public void putIntArray(String s, int[] aint) {
        this.output.putIntArray(s, aint);
    }

    private ProblemReporter reporterForChild(String s) {
        return this.problemReporter.forChild(new ProblemReporter.c(s));
    }

    @Override
    public ValueOutput child(String s) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.output.put(s, nbttagcompound);
        return new TagValueOutput(this.reporterForChild(s), this.ops, nbttagcompound);
    }

    @Override
    public ValueOutput.b childrenList(String s) {
        NBTTagList nbttaglist = new NBTTagList();

        this.output.put(s, nbttaglist);
        return new TagValueOutput.d(s, this.problemReporter, this.ops, nbttaglist);
    }

    @Override
    public <T> ValueOutput.a<T> list(String s, Codec<T> codec) {
        NBTTagList nbttaglist = new NBTTagList();

        this.output.put(s, nbttaglist);
        return new TagValueOutput.e<T>(this.problemReporter, s, this.ops, codec, nbttaglist);
    }

    @Override
    public void discard(String s) {
        this.output.remove(s);
    }

    @Override
    public boolean isEmpty() {
        return this.output.isEmpty();
    }

    public NBTTagCompound buildResult() {
        return this.output;
    }

    private static class d implements ValueOutput.b {

        private final String fieldName;
        private final ProblemReporter problemReporter;
        private final DynamicOps<NBTBase> ops;
        private final NBTTagList output;

        d(String s, ProblemReporter problemreporter, DynamicOps<NBTBase> dynamicops, NBTTagList nbttaglist) {
            this.fieldName = s;
            this.problemReporter = problemreporter;
            this.ops = dynamicops;
            this.output = nbttaglist;
        }

        @Override
        public ValueOutput addChild() {
            int i = this.output.size();
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            this.output.add(nbttagcompound);
            return new TagValueOutput(this.problemReporter.forChild(new ProblemReporter.d(this.fieldName, i)), this.ops, nbttagcompound);
        }

        @Override
        public void discardLast() {
            this.output.removeLast();
        }

        @Override
        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }

    private static class e<T> implements ValueOutput.a<T> {

        private final ProblemReporter problemReporter;
        private final String name;
        private final DynamicOps<NBTBase> ops;
        private final Codec<T> codec;
        private final NBTTagList output;

        e(ProblemReporter problemreporter, String s, DynamicOps<NBTBase> dynamicops, Codec<T> codec, NBTTagList nbttaglist) {
            this.problemReporter = problemreporter;
            this.name = s;
            this.ops = dynamicops;
            this.codec = codec;
            this.output = nbttaglist;
        }

        @Override
        public void add(T t0) {
            DataResult dataresult = this.codec.encodeStart(this.ops, t0);

            Objects.requireNonNull(dataresult);
            DataResult dataresult1 = dataresult;
            byte b0 = 0;

            //$FF: b0->value
            //0->com/mojang/serialization/DataResult$Success
            //1->com/mojang/serialization/DataResult$Error
            switch (dataresult1.typeSwitch<invokedynamic>(dataresult1, b0)) {
                case 0:
                    DataResult.Success<NBTBase> dataresult_success = (Success)dataresult1;

                    this.output.add((NBTBase)dataresult_success.value());
                    break;
                case 1:
                    DataResult.Error<NBTBase> dataresult_error = (Error)dataresult1;

                    this.problemReporter.report(new TagValueOutput.b(this.name, t0, dataresult_error));
                    Optional optional = dataresult_error.partialValue();
                    NBTTagList nbttaglist = this.output;

                    Objects.requireNonNull(this.output);
                    optional.ifPresent(nbttaglist::add);
                    break;
                default:
                    throw new MatchException((String)null, (Throwable)null);
            }

        }

        @Override
        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }

    public static record a(String name, Object value, DataResult.Error<?> error) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.value);

            return "Failed to encode value '" + s + "' to field '" + this.name + "': " + this.error.message();
        }
    }

    public static record b(String name, Object value, DataResult.Error<?> error) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.value);

            return "Failed to append value '" + s + "' to list '" + this.name + "': " + this.error.message();
        }
    }

    public static record c(Object value, DataResult.Error<?> error) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.value);

            return "Failed to merge value '" + s + "' to an object: " + this.error.message();
        }
    }
}
