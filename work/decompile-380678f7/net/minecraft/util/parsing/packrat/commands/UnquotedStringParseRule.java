package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class UnquotedStringParseRule implements Rule<StringReader, String> {

    private final int minSize;
    private final DelayedException<CommandSyntaxException> error;

    public UnquotedStringParseRule(int i, DelayedException<CommandSyntaxException> delayedexception) {
        this.minSize = i;
        this.error = delayedexception;
    }

    @Nullable
    @Override
    public String parse(ParseState<StringReader> parsestate) {
        ((StringReader) parsestate.input()).skipWhitespace();
        int i = parsestate.mark();
        String s = ((StringReader) parsestate.input()).readUnquotedString();

        if (s.length() < this.minSize) {
            parsestate.errorCollector().store(i, this.error);
            return null;
        } else {
            return s;
        }
    }
}
