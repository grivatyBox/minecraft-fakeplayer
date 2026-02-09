package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;

public record Grammar<T>(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) implements CommandArgumentParser<T> {

    public Grammar(Dictionary<StringReader> dictionary, NamedRule<StringReader, T> namedrule) {
        dictionary.checkAllBound();
        this.rules = dictionary;
        this.top = namedrule;
    }

    public Optional<T> parse(ParseState<StringReader> parsestate) {
        return parsestate.<T>parseTopRule(this.top);
    }

    @Override
    public T parseForCommands(StringReader stringreader) throws CommandSyntaxException {
        ErrorCollector.a<StringReader> errorcollector_a = new ErrorCollector.a<StringReader>();
        StringReaderParserState stringreaderparserstate = new StringReaderParserState(errorcollector_a, stringreader);
        Optional<T> optional = this.parse(stringreaderparserstate);

        if (optional.isPresent()) {
            return (T) optional.get();
        } else {
            List<ErrorEntry<StringReader>> list = errorcollector_a.entries();
            List<Exception> list1 = list.stream().mapMulti((errorentry, consumer) -> {
                Object object = errorentry.reason();

                if (object instanceof DelayedException<?> delayedexception) {
                    consumer.accept(delayedexception.create(stringreader.getString(), errorentry.cursor()));
                } else {
                    object = errorentry.reason();
                    if (object instanceof Exception exception) {
                        consumer.accept(exception);
                    }
                }

            }).toList();

            for (Exception exception : list1) {
                if (exception instanceof CommandSyntaxException) {
                    CommandSyntaxException commandsyntaxexception = (CommandSyntaxException) exception;

                    throw commandsyntaxexception;
                }
            }

            if (list1.size() == 1) {
                Object object = list1.get(0);

                if (object instanceof RuntimeException) {
                    RuntimeException runtimeexception = (RuntimeException) object;

                    throw runtimeexception;
                }
            }

            Stream stream = list.stream().map(ErrorEntry::toString);

            throw new IllegalStateException("Failed to parse: " + (String) stream.collect(Collectors.joining(", ")));
        }
    }

    @Override
    public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsbuilder) {
        StringReader stringreader = new StringReader(suggestionsbuilder.getInput());

        stringreader.setCursor(suggestionsbuilder.getStart());
        ErrorCollector.a<StringReader> errorcollector_a = new ErrorCollector.a<StringReader>();
        StringReaderParserState stringreaderparserstate = new StringReaderParserState(errorcollector_a, stringreader);

        this.parse(stringreaderparserstate);
        List<ErrorEntry<StringReader>> list = errorcollector_a.entries();

        if (list.isEmpty()) {
            return suggestionsbuilder.buildFuture();
        } else {
            SuggestionsBuilder suggestionsbuilder1 = suggestionsbuilder.createOffset(errorcollector_a.cursor());

            for (ErrorEntry<StringReader> errorentry : list) {
                SuggestionSupplier suggestionsupplier = errorentry.suggestions();

                if (suggestionsupplier instanceof ResourceSuggestion) {
                    ResourceSuggestion resourcesuggestion = (ResourceSuggestion) suggestionsupplier;

                    ICompletionProvider.suggestResource(resourcesuggestion.possibleResources(), suggestionsbuilder1);
                } else {
                    ICompletionProvider.suggest(errorentry.suggestions().possibleValues(stringreaderparserstate), suggestionsbuilder1);
                }
            }

            return suggestionsbuilder1.buildFuture();
        }
    }
}
