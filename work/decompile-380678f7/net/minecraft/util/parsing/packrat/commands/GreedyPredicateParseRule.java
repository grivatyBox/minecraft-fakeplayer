package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class GreedyPredicateParseRule implements Rule<StringReader, String> {

    private final int minSize;
    private final int maxSize;
    private final DelayedException<CommandSyntaxException> error;

    public GreedyPredicateParseRule(int i, DelayedException<CommandSyntaxException> delayedexception) {
        this(i, Integer.MAX_VALUE, delayedexception);
    }

    public GreedyPredicateParseRule(int i, int j, DelayedException<CommandSyntaxException> delayedexception) {
        this.minSize = i;
        this.maxSize = j;
        this.error = delayedexception;
    }

    @Nullable
    @Override
    public String parse(ParseState<StringReader> parsestate) {
        StringReader stringreader = parsestate.input();
        String s = stringreader.getString();
        int i = stringreader.getCursor();

        int j;

        for (j = i; j < s.length() && this.isAccepted(s.charAt(j)) && j - i < this.maxSize; ++j) {
            ;
        }

        int k = j - i;

        if (k < this.minSize) {
            parsestate.errorCollector().store(parsestate.mark(), this.error);
            return null;
        } else {
            stringreader.setCursor(j);
            return s.substring(i, j);
        }
    }

    protected abstract boolean isAccepted(char c0);
}
