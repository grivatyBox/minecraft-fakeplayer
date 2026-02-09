package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.IChatBaseComponent;

public record ArgumentParserPosition(boolean relative, double value) {

    private static final char PREFIX_RELATIVE = '~';
    public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.pos.missing.double"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.pos.missing.int"));

    public double get(double d0) {
        return this.relative ? this.value + d0 : this.value;
    }

    public static ArgumentParserPosition parseDouble(StringReader stringreader, boolean flag) throws CommandSyntaxException {
        if (stringreader.canRead() && stringreader.peek() == '^') {
            throw ArgumentVec3.ERROR_MIXED_TYPE.createWithContext(stringreader);
        } else if (!stringreader.canRead()) {
            throw ArgumentParserPosition.ERROR_EXPECTED_DOUBLE.createWithContext(stringreader);
        } else {
            boolean flag1 = isRelative(stringreader);
            int i = stringreader.getCursor();
            double d0 = stringreader.canRead() && stringreader.peek() != ' ' ? stringreader.readDouble() : 0.0D;
            String s = stringreader.getString().substring(i, stringreader.getCursor());

            if (flag1 && s.isEmpty()) {
                return new ArgumentParserPosition(true, 0.0D);
            } else {
                if (!s.contains(".") && !flag1 && flag) {
                    d0 += 0.5D;
                }

                return new ArgumentParserPosition(flag1, d0);
            }
        }
    }

    public static ArgumentParserPosition parseInt(StringReader stringreader) throws CommandSyntaxException {
        if (stringreader.canRead() && stringreader.peek() == '^') {
            throw ArgumentVec3.ERROR_MIXED_TYPE.createWithContext(stringreader);
        } else if (!stringreader.canRead()) {
            throw ArgumentParserPosition.ERROR_EXPECTED_INT.createWithContext(stringreader);
        } else {
            boolean flag = isRelative(stringreader);
            double d0;

            if (stringreader.canRead() && stringreader.peek() != ' ') {
                d0 = flag ? stringreader.readDouble() : (double) stringreader.readInt();
            } else {
                d0 = 0.0D;
            }

            return new ArgumentParserPosition(flag, d0);
        }
    }

    public static boolean isRelative(StringReader stringreader) {
        boolean flag;

        if (stringreader.peek() == '~') {
            flag = true;
            stringreader.skip();
        } else {
            flag = false;
        }

        return flag;
    }

    public boolean isRelative() {
        return this.relative;
    }
}
