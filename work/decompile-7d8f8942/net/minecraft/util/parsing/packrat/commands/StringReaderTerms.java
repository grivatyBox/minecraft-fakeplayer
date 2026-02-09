package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {

    static Term<StringReader> word(String s) {
        return new StringReaderTerms.b(s);
    }

    static Term<StringReader> character(final char c0) {
        return new StringReaderTerms.a(CharList.of(c0)) {
            @Override
            protected boolean isAccepted(char c1) {
                return c0 == c1;
            }
        };
    }

    static Term<StringReader> characters(final char c0, final char c1) {
        return new StringReaderTerms.a(CharList.of(c0, c1)) {
            @Override
            protected boolean isAccepted(char c2) {
                return c2 == c0 || c2 == c1;
            }
        };
    }

    static StringReader createReader(String s, int i) {
        StringReader stringreader = new StringReader(s);

        stringreader.setCursor(i);
        return stringreader;
    }

    public static final class b implements Term<StringReader> {

        private final String value;
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public b(String s) {
            this.value = s;
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), s);
            this.suggestions = (parsestate) -> {
                return Stream.of(s);
            };
        }

        @Override
        public boolean parse(ParseState<StringReader> parsestate, Scope scope, Control control) {
            ((StringReader) parsestate.input()).skipWhitespace();
            int i = parsestate.mark();
            String s = ((StringReader) parsestate.input()).readUnquotedString();

            if (!s.equals(this.value)) {
                parsestate.errorCollector().store(i, this.suggestions, this.error);
                return false;
            } else {
                return true;
            }
        }

        public String toString() {
            return "terminal[" + this.value + "]";
        }
    }

    public abstract static class a implements Term<StringReader> {

        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public a(CharList charlist) {
            String s = (String) charlist.intStream().mapToObj(Character::toString).collect(Collectors.joining("|"));

            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), String.valueOf(s));
            this.suggestions = (parsestate) -> {
                return charlist.intStream().mapToObj(Character::toString);
            };
        }

        @Override
        public boolean parse(ParseState<StringReader> parsestate, Scope scope, Control control) {
            ((StringReader) parsestate.input()).skipWhitespace();
            int i = parsestate.mark();

            if (((StringReader) parsestate.input()).canRead() && this.isAccepted(((StringReader) parsestate.input()).read())) {
                return true;
            } else {
                parsestate.errorCollector().store(i, this.suggestions, this.error);
                return false;
            }
        }

        protected abstract boolean isAccepted(char c0);
    }
}
