package net.minecraft.util.parsing.packrat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;

public interface DelayedException<T extends Exception> {

    T create(String s, int i);

    static DelayedException<CommandSyntaxException> create(SimpleCommandExceptionType simplecommandexceptiontype) {
        return (s, i) -> {
            return simplecommandexceptiontype.createWithContext(StringReaderTerms.createReader(s, i));
        };
    }

    static DelayedException<CommandSyntaxException> create(DynamicCommandExceptionType dynamiccommandexceptiontype, String s) {
        return (s1, i) -> {
            return dynamiccommandexceptiontype.createWithContext(StringReaderTerms.createReader(s1, i), s);
        };
    }
}
