package net.minecraft.commands;

import com.mojang.brigadier.StringReader;
import net.minecraft.CharPredicate;

public class ParserUtils {

    public ParserUtils() {}

    public static String readWhile(StringReader stringreader, CharPredicate charpredicate) {
        int i = stringreader.getCursor();

        while (stringreader.canRead() && charpredicate.test(stringreader.peek())) {
            stringreader.skip();
        }

        return stringreader.getString().substring(i, stringreader.getCursor());
    }
}
