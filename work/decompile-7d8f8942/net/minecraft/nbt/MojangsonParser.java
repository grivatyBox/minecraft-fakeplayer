package net.minecraft.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Objects;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.parsing.packrat.commands.Grammar;

public class MojangsonParser<T> {

    public static final SimpleCommandExceptionType ERROR_TRAILING_DATA = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.nbt.trailing"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_COMPOUND = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.nbt.expected.compound"));
    public static final char ELEMENT_SEPARATOR = ',';
    public static final char NAME_VALUE_SEPARATOR = ':';
    private static final MojangsonParser<NBTBase> NBT_OPS_PARSER = create(DynamicOpsNBT.INSTANCE);
    public static final Codec<NBTTagCompound> FLATTENED_CODEC = Codec.STRING.comapFlatMap((s) -> {
        try {
            NBTBase nbtbase = (NBTBase) MojangsonParser.NBT_OPS_PARSER.parseFully(s);

            if (nbtbase instanceof NBTTagCompound nbttagcompound) {
                return DataResult.success(nbttagcompound, Lifecycle.stable());
            } else {
                return DataResult.error(() -> {
                    return "Expected compound tag, got " + String.valueOf(nbtbase);
                });
            }
        } catch (CommandSyntaxException commandsyntaxexception) {
            Objects.requireNonNull(commandsyntaxexception);
            return DataResult.error(commandsyntaxexception::getMessage);
        }
    }, NBTTagCompound::toString);
    public static final Codec<NBTTagCompound> LENIENT_CODEC = Codec.withAlternative(MojangsonParser.FLATTENED_CODEC, NBTTagCompound.CODEC);
    private final DynamicOps<T> ops;
    private final Grammar<T> grammar;

    private MojangsonParser(DynamicOps<T> dynamicops, Grammar<T> grammar) {
        this.ops = dynamicops;
        this.grammar = grammar;
    }

    public DynamicOps<T> getOps() {
        return this.ops;
    }

    public static <T> MojangsonParser<T> create(DynamicOps<T> dynamicops) {
        return new MojangsonParser<T>(dynamicops, SnbtGrammar.createParser(dynamicops));
    }

    private static NBTTagCompound castToCompoundOrThrow(StringReader stringreader, NBTBase nbtbase) throws CommandSyntaxException {
        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            return nbttagcompound;
        } else {
            throw MojangsonParser.ERROR_EXPECTED_COMPOUND.createWithContext(stringreader);
        }
    }

    public static NBTTagCompound parseCompoundFully(String s) throws CommandSyntaxException {
        StringReader stringreader = new StringReader(s);

        return castToCompoundOrThrow(stringreader, (NBTBase) MojangsonParser.NBT_OPS_PARSER.parseFully(stringreader));
    }

    public T parseFully(String s) throws CommandSyntaxException {
        return (T) this.parseFully(new StringReader(s));
    }

    public T parseFully(StringReader stringreader) throws CommandSyntaxException {
        T t0 = (T) this.grammar.parseForCommands(stringreader);

        stringreader.skipWhitespace();
        if (stringreader.canRead()) {
            throw MojangsonParser.ERROR_TRAILING_DATA.createWithContext(stringreader);
        } else {
            return t0;
        }
    }

    public T parseAsArgument(StringReader stringreader) throws CommandSyntaxException {
        return (T) this.grammar.parseForCommands(stringreader);
    }

    public static NBTTagCompound parseCompoundAsArgument(StringReader stringreader) throws CommandSyntaxException {
        NBTBase nbtbase = MojangsonParser.NBT_OPS_PARSER.parseAsArgument(stringreader);

        return castToCompoundOrThrow(stringreader, nbtbase);
    }
}
