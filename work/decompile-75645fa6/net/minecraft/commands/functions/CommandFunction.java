package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;

public interface CommandFunction<T> {

    MinecraftKey id();

    InstantiatedFunction<T> instantiate(@Nullable NBTTagCompound nbttagcompound, CommandDispatcher<T> commanddispatcher) throws FunctionInstantiationException;

    private static boolean shouldConcatenateNextLine(CharSequence charsequence) {
        int i = charsequence.length();

        return i > 0 && charsequence.charAt(i - 1) == '\\';
    }

    static <T extends ExecutionCommandSource<T>> CommandFunction<T> fromLines(MinecraftKey minecraftkey, CommandDispatcher<T> commanddispatcher, T t0, List<String> list) {
        FunctionBuilder<T> functionbuilder = new FunctionBuilder<T>();

        for (int i = 0; i < list.size(); ++i) {
            int j = i + 1;
            String s = ((String) list.get(i)).trim();
            String s1;

            if (shouldConcatenateNextLine(s)) {
                StringBuilder stringbuilder = new StringBuilder(s);

                do {
                    ++i;
                    if (i == list.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }

                    stringbuilder.deleteCharAt(stringbuilder.length() - 1);
                    String s2 = ((String) list.get(i)).trim();

                    stringbuilder.append(s2);
                    checkCommandLineLength(stringbuilder);
                } while (shouldConcatenateNextLine(stringbuilder));

                s1 = stringbuilder.toString();
            } else {
                s1 = s;
            }

            checkCommandLineLength(s1);
            StringReader stringreader = new StringReader(s1);

            if (stringreader.canRead() && stringreader.peek() != '#') {
                if (stringreader.peek() == '/') {
                    stringreader.skip();
                    if (stringreader.peek() == '/') {
                        throw new IllegalArgumentException("Unknown or invalid command '" + s1 + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
                    }

                    String s3 = stringreader.readUnquotedString();

                    throw new IllegalArgumentException("Unknown or invalid command '" + s1 + "' on line " + j + " (did you mean '" + s3 + "'? Do not use a preceding forwards slash.)");
                }

                if (stringreader.peek() == '$') {
                    functionbuilder.addMacro(s1.substring(1), j, t0);
                } else {
                    try {
                        functionbuilder.addCommand(parseCommand(commanddispatcher, t0, stringreader));
                    } catch (CommandSyntaxException commandsyntaxexception) {
                        throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandsyntaxexception.getMessage());
                    }
                }
            }
        }

        return functionbuilder.build(minecraftkey);
    }

    static void checkCommandLineLength(CharSequence charsequence) {
        if (charsequence.length() > 2000000) {
            CharSequence charsequence1 = charsequence.subSequence(0, Math.min(512, 2000000));
            int i = charsequence.length();

            throw new IllegalStateException("Command too long: " + i + " characters, contents: " + String.valueOf(charsequence1) + "...");
        }
    }

    static <T extends ExecutionCommandSource<T>> UnboundEntryAction<T> parseCommand(CommandDispatcher<T> commanddispatcher, T t0, StringReader stringreader) throws CommandSyntaxException {
        ParseResults<T> parseresults = commanddispatcher.parse(stringreader, t0);

        net.minecraft.commands.CommandDispatcher.validateParseResults(parseresults);
        Optional<ContextChain<T>> optional = ContextChain.tryFlatten(parseresults.getContext().build(stringreader.getString()));

        if (optional.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseresults.getReader());
        } else {
            return new BuildContexts.c<T>(stringreader.getString(), (ContextChain) optional.get());
        }
    }
}
