package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class ResourceLookupRule<C, V> implements Rule<StringReader, V>, ResourceSuggestion {

    private final NamedRule<StringReader, MinecraftKey> idParser;
    protected final C context;
    private final DelayedException<CommandSyntaxException> error;

    protected ResourceLookupRule(NamedRule<StringReader, MinecraftKey> namedrule, C c0) {
        this.idParser = namedrule;
        this.context = c0;
        this.error = DelayedException.create(MinecraftKey.ERROR_INVALID);
    }

    @Nullable
    @Override
    public V parse(ParseState<StringReader> parsestate) {
        ((StringReader) parsestate.input()).skipWhitespace();
        int i = parsestate.mark();
        MinecraftKey minecraftkey = (MinecraftKey) parsestate.parse(this.idParser);

        if (minecraftkey != null) {
            try {
                return (V) this.validateElement((ImmutableStringReader) parsestate.input(), minecraftkey);
            } catch (Exception exception) {
                parsestate.errorCollector().store(i, this, exception);
                return null;
            }
        } else {
            parsestate.errorCollector().store(i, this, this.error);
            return null;
        }
    }

    protected abstract V validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception;
}
