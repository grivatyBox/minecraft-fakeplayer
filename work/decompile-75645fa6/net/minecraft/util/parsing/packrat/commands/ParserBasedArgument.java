package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

public abstract class ParserBasedArgument<T> implements ArgumentType<T> {

    private final CommandArgumentParser<T> parser;

    public ParserBasedArgument(CommandArgumentParser<T> commandargumentparser) {
        this.parser = commandargumentparser;
    }

    public T parse(StringReader stringreader) throws CommandSyntaxException {
        return this.parser.parseForCommands(stringreader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return this.parser.parseForSuggestions(suggestionsbuilder);
    }
}
