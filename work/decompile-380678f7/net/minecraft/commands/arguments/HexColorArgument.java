package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.ARGB;

public class HexColorArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("F00", "FF0000");
    public static final DynamicCommandExceptionType ERROR_INVALID_HEX = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.hexcolor.invalid", object);
    });

    private HexColorArgument() {}

    public static HexColorArgument hexColor() {
        return new HexColorArgument();
    }

    public static Integer getHexColor(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (Integer) commandcontext.getArgument(s, Integer.class);
    }

    public Integer parse(StringReader stringreader) throws CommandSyntaxException {
        String s = stringreader.readUnquotedString();
        Integer integer;

        switch (s.length()) {
            case 3:
                integer = ARGB.color(Integer.valueOf(MessageFormat.format("{0}{0}", s.charAt(0)), 16), Integer.valueOf(MessageFormat.format("{0}{0}", s.charAt(1)), 16), Integer.valueOf(MessageFormat.format("{0}{0}", s.charAt(2)), 16));
                break;
            case 6:
                integer = ARGB.color(Integer.valueOf(s.substring(0, 2), 16), Integer.valueOf(s.substring(2, 4), 16), Integer.valueOf(s.substring(4, 6), 16));
                break;
            default:
                throw HexColorArgument.ERROR_INVALID_HEX.createWithContext(stringreader, s);
        }

        return integer;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return ICompletionProvider.suggest(HexColorArgument.EXAMPLES, suggestionsbuilder);
    }

    public Collection<String> getExamples() {
        return HexColorArgument.EXAMPLES;
    }
}
