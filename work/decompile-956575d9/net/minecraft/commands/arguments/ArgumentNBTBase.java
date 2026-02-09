package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;

public class ArgumentNBTBase extends ParserBasedArgument<NBTBase> {

    private static final Collection<String> EXAMPLES = Arrays.asList("0", "0b", "0l", "0.0", "\"foo\"", "{foo=bar}", "[0]");
    private static final CommandArgumentParser<NBTBase> TAG_PARSER = SnbtGrammar.<NBTBase>createParser(DynamicOpsNBT.INSTANCE);

    private ArgumentNBTBase() {
        super(ArgumentNBTBase.TAG_PARSER);
    }

    public static ArgumentNBTBase nbtTag() {
        return new ArgumentNBTBase();
    }

    public static <S> NBTBase getNbtTag(CommandContext<S> commandcontext, String s) {
        return (NBTBase) commandcontext.getArgument(s, NBTBase.class);
    }

    public Collection<String> getExamples() {
        return ArgumentNBTBase.EXAMPLES;
    }
}
