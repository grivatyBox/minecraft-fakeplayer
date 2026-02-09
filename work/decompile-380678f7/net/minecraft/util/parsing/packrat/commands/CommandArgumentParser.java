package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandArgumentParser<T> {

    T parseForCommands(StringReader stringreader) throws CommandSyntaxException;

    CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsbuilder);

    default <S> CommandArgumentParser<S> mapResult(final Function<T, S> function) {
        return new CommandArgumentParser<S>() {
            @Override
            public S parseForCommands(StringReader stringreader) throws CommandSyntaxException {
                return (S) function.apply(CommandArgumentParser.this.parseForCommands(stringreader));
            }

            @Override
            public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsbuilder) {
                return CommandArgumentParser.this.parseForSuggestions(suggestionsbuilder);
            }
        };
    }

    default <T, O> CommandArgumentParser<T> withCodec(final DynamicOps<O> dynamicops, final CommandArgumentParser<O> commandargumentparser, final Codec<T> codec, final DynamicCommandExceptionType dynamiccommandexceptiontype) {
        return new CommandArgumentParser<T>() {
            @Override
            public T parseForCommands(StringReader stringreader) throws CommandSyntaxException {
                int i = stringreader.getCursor();
                O o0 = commandargumentparser.parseForCommands(stringreader);
                DataResult<T> dataresult = codec.parse(dynamicops, o0);

                return (T) dataresult.getOrThrow((s) -> {
                    stringreader.setCursor(i);
                    return dynamiccommandexceptiontype.createWithContext(stringreader, s);
                });
            }

            @Override
            public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsbuilder) {
                return CommandArgumentParser.this.parseForSuggestions(suggestionsbuilder);
            }
        };
    }
}
