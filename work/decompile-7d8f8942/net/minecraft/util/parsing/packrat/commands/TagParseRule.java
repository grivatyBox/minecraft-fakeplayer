package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class TagParseRule<T> implements Rule<StringReader, Dynamic<?>> {

    private final MojangsonParser<T> parser;

    public TagParseRule(DynamicOps<T> dynamicops) {
        this.parser = MojangsonParser.<T>create(dynamicops);
    }

    @Nullable
    @Override
    public Dynamic<T> parse(ParseState<StringReader> parsestate) {
        ((StringReader) parsestate.input()).skipWhitespace();
        int i = parsestate.mark();

        try {
            return new Dynamic(this.parser.getOps(), this.parser.parseAsArgument(parsestate.input()));
        } catch (Exception exception) {
            parsestate.errorCollector().store(i, exception);
            return null;
        }
    }
}
