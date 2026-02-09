package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class ResourceLocationParseRule implements Rule<StringReader, MinecraftKey> {

    public static final Rule<StringReader, MinecraftKey> INSTANCE = new ResourceLocationParseRule();

    private ResourceLocationParseRule() {}

    @Nullable
    @Override
    public MinecraftKey parse(ParseState<StringReader> parsestate) {
        ((StringReader) parsestate.input()).skipWhitespace();

        try {
            return MinecraftKey.readNonEmpty(parsestate.input());
        } catch (CommandSyntaxException commandsyntaxexception) {
            return null;
        }
    }
}
