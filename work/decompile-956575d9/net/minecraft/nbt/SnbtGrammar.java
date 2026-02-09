package net.minecraft.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.chars.CharList;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.IntStream.Builder;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.GreedyPatternParseRule;
import net.minecraft.util.parsing.packrat.commands.GreedyPredicateParseRule;
import net.minecraft.util.parsing.packrat.commands.NumberRunParseRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.UnquotedStringParseRule;

public class SnbtGrammar {

    private static final DynamicCommandExceptionType ERROR_NUMBER_PARSE_FAILURE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("snbt.parser.number_parse_failure", object);
    });
    static final DynamicCommandExceptionType ERROR_EXPECTED_HEX_ESCAPE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("snbt.parser.expected_hex_escape", object);
    });
    private static final DynamicCommandExceptionType ERROR_INVALID_CODEPOINT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("snbt.parser.invalid_codepoint", object);
    });
    private static final DynamicCommandExceptionType ERROR_NO_SUCH_OPERATION = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("snbt.parser.no_such_operation", object);
    });
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_INTEGER_TYPE = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_integer_type")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_FLOAT_TYPE = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_float_type")));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NON_NEGATIVE_NUMBER = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_non_negative_number")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_CHARACTER_NAME = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.invalid_character_name")));
    static final DelayedException<CommandSyntaxException> ERROR_INVALID_ARRAY_ELEMENT_TYPE = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.invalid_array_element_type")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_UNQUOTED_START = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.invalid_unquoted_start")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_UNQUOTED_STRING = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_unquoted_string")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_STRING_CONTENTS = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.invalid_string_contents")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_BINARY_NUMERAL = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_binary_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_UNDESCORE_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.undescore_not_allowed")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_DECIMAL_NUMERAL = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_decimal_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_HEX_NUMERAL = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_hex_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_EMPTY_KEY = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.empty_key")));
    private static final DelayedException<CommandSyntaxException> ERROR_LEADING_ZERO_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.leading_zero_not_allowed")));
    private static final DelayedException<CommandSyntaxException> ERROR_INFINITY_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.infinity_not_allowed")));
    private static final HexFormat HEX_ESCAPE = HexFormat.of().withUpperCase();
    private static final NumberRunParseRule BINARY_NUMERAL = new NumberRunParseRule(SnbtGrammar.ERROR_EXPECTED_BINARY_NUMERAL, SnbtGrammar.ERROR_UNDESCORE_NOT_ALLOWED) {
        @Override
        protected boolean isAccepted(char c0) {
            boolean flag;

            switch (c0) {
                case '0':
                case '1':
                case '_':
                    flag = true;
                    break;
                default:
                    flag = false;
            }

            return flag;
        }
    };
    private static final NumberRunParseRule DECIMAL_NUMERAL = new NumberRunParseRule(SnbtGrammar.ERROR_EXPECTED_DECIMAL_NUMERAL, SnbtGrammar.ERROR_UNDESCORE_NOT_ALLOWED) {
        @Override
        protected boolean isAccepted(char c0) {
            boolean flag;

            switch (c0) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '_':
                    flag = true;
                    break;
                default:
                    flag = false;
            }

            return flag;
        }
    };
    private static final NumberRunParseRule HEX_NUMERAL = new NumberRunParseRule(SnbtGrammar.ERROR_EXPECTED_HEX_NUMERAL, SnbtGrammar.ERROR_UNDESCORE_NOT_ALLOWED) {
        @Override
        protected boolean isAccepted(char c0) {
            boolean flag;

            switch (c0) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    flag = true;
                    break;
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '`':
                default:
                    flag = false;
            }

            return flag;
        }
    };
    private static final GreedyPredicateParseRule PLAIN_STRING_CHUNK = new GreedyPredicateParseRule(1, SnbtGrammar.ERROR_INVALID_STRING_CONTENTS) {
        @Override
        protected boolean isAccepted(char c0) {
            boolean flag;

            switch (c0) {
                case '"':
                case '\'':
                case '\\':
                    flag = false;
                    break;
                default:
                    flag = true;
            }

            return flag;
        }
    };
    private static final StringReaderTerms.a NUMBER_LOOKEAHEAD = new StringReaderTerms.a(CharList.of()) {
        @Override
        protected boolean isAccepted(char c0) {
            return SnbtGrammar.canStartNumber(c0);
        }
    };
    private static final Pattern UNICODE_NAME = Pattern.compile("[-a-zA-Z0-9 ]+");

    public SnbtGrammar() {}

    static DelayedException<CommandSyntaxException> createNumberParseError(NumberFormatException numberformatexception) {
        return DelayedException.create(SnbtGrammar.ERROR_NUMBER_PARSE_FAILURE, numberformatexception.getMessage());
    }

    @Nullable
    public static String escapeControlCharacters(char c0) {
        String s;

        switch (c0) {
            case '\b':
                s = "b";
                break;
            case '\t':
                s = "t";
                break;
            case '\n':
                s = "n";
                break;
            case '\u000b':
            default:
                s = c0 < ' ' ? "x" + SnbtGrammar.HEX_ESCAPE.toHexDigits((byte) c0) : null;
                break;
            case '\f':
                s = "f";
                break;
            case '\r':
                s = "r";
        }

        return s;
    }

    private static boolean isAllowedToStartUnquotedString(char c0) {
        return !canStartNumber(c0);
    }

    static boolean canStartNumber(char c0) {
        boolean flag;

        switch (c0) {
            case '+':
            case '-':
            case '.':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                flag = true;
                break;
            case ',':
            case '/':
            default:
                flag = false;
        }

        return flag;
    }

    static boolean needsUnderscoreRemoval(String s) {
        return s.indexOf(95) != -1;
    }

    private static void cleanAndAppend(StringBuilder stringbuilder, String s) {
        cleanAndAppend(stringbuilder, s, needsUnderscoreRemoval(s));
    }

    static void cleanAndAppend(StringBuilder stringbuilder, String s, boolean flag) {
        if (flag) {
            for (char c0 : s.toCharArray()) {
                if (c0 != '_') {
                    stringbuilder.append(c0);
                }
            }
        } else {
            stringbuilder.append(s);
        }

    }

    static short parseUnsignedShort(String s, int i) {
        int j = Integer.parseInt(s, i);

        if (j >> 16 == 0) {
            return (short) j;
        } else {
            throw new NumberFormatException("out of range: " + j);
        }
    }

    @Nullable
    private static <T> T createFloat(DynamicOps<T> dynamicops, SnbtGrammar.e snbtgrammar_e, @Nullable String s, @Nullable String s1, @Nullable SnbtGrammar.f<String> snbtgrammar_f, @Nullable SnbtGrammar.i snbtgrammar_i, ParseState<?> parsestate) {
        StringBuilder stringbuilder = new StringBuilder();

        snbtgrammar_e.append(stringbuilder);
        if (s != null) {
            cleanAndAppend(stringbuilder, s);
        }

        if (s1 != null) {
            stringbuilder.append('.');
            cleanAndAppend(stringbuilder, s1);
        }

        if (snbtgrammar_f != null) {
            stringbuilder.append('e');
            snbtgrammar_f.sign().append(stringbuilder);
            cleanAndAppend(stringbuilder, (String)snbtgrammar_f.value);
        }

        try {
            String s2 = stringbuilder.toString();
            byte b0 = 0;
            Object object;

            //$FF: b0->value
            //0->FLOAT
            //1->DOUBLE
            switch (snbtgrammar_i.enumSwitch<invokedynamic>(snbtgrammar_i, b0)) {
                case -1:
                    object = convertDouble(dynamicops, parsestate, s2);
                    break;
                case 0:
                    object = convertFloat(dynamicops, parsestate, s2);
                    break;
                case 1:
                    object = convertDouble(dynamicops, parsestate, s2);
                    break;
                default:
                    parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_EXPECTED_FLOAT_TYPE);
                    object = null;
            }

            return (T)object;
        } catch (NumberFormatException numberformatexception) {
            parsestate.errorCollector().store(parsestate.mark(), createNumberParseError(numberformatexception));
            return null;
        }
    }

    @Nullable
    private static <T> T convertFloat(DynamicOps<T> dynamicops, ParseState<?> parsestate, String s) {
        float f = Float.parseFloat(s);

        if (!Float.isFinite(f)) {
            parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_INFINITY_NOT_ALLOWED);
            return null;
        } else {
            return (T) dynamicops.createFloat(f);
        }
    }

    @Nullable
    private static <T> T convertDouble(DynamicOps<T> dynamicops, ParseState<?> parsestate, String s) {
        double d0 = Double.parseDouble(s);

        if (!Double.isFinite(d0)) {
            parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_INFINITY_NOT_ALLOWED);
            return null;
        } else {
            return (T) dynamicops.createDouble(d0);
        }
    }

    private static String joinList(List<String> list) {
        String s;

        switch (list.size()) {
            case 0:
                s = "";
                break;
            case 1:
                s = (String) list.getFirst();
                break;
            default:
                s = String.join("", list);
        }

        return s;
    }

    public static <T> Grammar<T> createParser(DynamicOps<T> dynamicops) {
        T t0 = (T) dynamicops.createBoolean(true);
        T t1 = (T) dynamicops.createBoolean(false);
        T t2 = (T) dynamicops.emptyMap();
        T t3 = (T) dynamicops.emptyList();
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        Atom<SnbtGrammar.e> atom = Atom.<SnbtGrammar.e>of("sign");

        dictionary.put(atom, Term.alternative(Term.sequence(StringReaderTerms.character('+'), Term.marker(atom, SnbtGrammar.e.PLUS)), Term.sequence(StringReaderTerms.character('-'), Term.marker(atom, SnbtGrammar.e.MINUS))), (scope) -> {
            return (SnbtGrammar.e) scope.getOrThrow(atom);
        });
        Atom<SnbtGrammar.d> atom1 = Atom.<SnbtGrammar.d>of("integer_suffix");

        dictionary.put(atom1, Term.alternative(Term.sequence(StringReaderTerms.characters('u', 'U'), Term.alternative(Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.UNSIGNED, SnbtGrammar.i.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.UNSIGNED, SnbtGrammar.i.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.UNSIGNED, SnbtGrammar.i.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.UNSIGNED, SnbtGrammar.i.LONG))))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.alternative(Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.SIGNED, SnbtGrammar.i.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.SIGNED, SnbtGrammar.i.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.SIGNED, SnbtGrammar.i.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker(atom1, new SnbtGrammar.d(SnbtGrammar.g.SIGNED, SnbtGrammar.i.LONG))))), Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker(atom1, new SnbtGrammar.d((SnbtGrammar.g) null, SnbtGrammar.i.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker(atom1, new SnbtGrammar.d((SnbtGrammar.g) null, SnbtGrammar.i.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker(atom1, new SnbtGrammar.d((SnbtGrammar.g) null, SnbtGrammar.i.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker(atom1, new SnbtGrammar.d((SnbtGrammar.g) null, SnbtGrammar.i.LONG)))), (scope) -> {
            return (SnbtGrammar.d) scope.getOrThrow(atom1);
        });
        Atom<String> atom2 = Atom.<String>of("binary_numeral");

        dictionary.put(atom2, SnbtGrammar.BINARY_NUMERAL);
        Atom<String> atom3 = Atom.<String>of("decimal_numeral");

        dictionary.put(atom3, SnbtGrammar.DECIMAL_NUMERAL);
        Atom<String> atom4 = Atom.<String>of("hex_numeral");

        dictionary.put(atom4, SnbtGrammar.HEX_NUMERAL);
        Atom<SnbtGrammar.c> atom5 = Atom.<SnbtGrammar.c>of("integer_literal");
        NamedRule<StringReader, SnbtGrammar.c> namedrule = dictionary.put(atom5, Term.sequence(Term.optional(dictionary.named(atom)), Term.alternative(Term.sequence(StringReaderTerms.character('0'), Term.cut(), Term.alternative(Term.sequence(StringReaderTerms.characters('x', 'X'), Term.cut(), dictionary.named(atom4)), Term.sequence(StringReaderTerms.characters('b', 'B'), dictionary.named(atom2)), Term.sequence(dictionary.named(atom3), Term.cut(), Term.fail(SnbtGrammar.ERROR_LEADING_ZERO_NOT_ALLOWED)), Term.marker(atom3, "0"))), dictionary.named(atom3)), Term.optional(dictionary.named(atom1))), (scope) -> {
            SnbtGrammar.d snbtgrammar_d = (SnbtGrammar.d) scope.getOrDefault(atom1, SnbtGrammar.d.EMPTY);
            SnbtGrammar.e snbtgrammar_e = (SnbtGrammar.e) scope.getOrDefault(atom, SnbtGrammar.e.PLUS);
            String s = (String) scope.get(atom3);

            if (s != null) {
                return new SnbtGrammar.c(snbtgrammar_e, SnbtGrammar.b.DECIMAL, s, snbtgrammar_d);
            } else {
                String s1 = (String) scope.get(atom4);

                if (s1 != null) {
                    return new SnbtGrammar.c(snbtgrammar_e, SnbtGrammar.b.HEX, s1, snbtgrammar_d);
                } else {
                    String s2 = (String) scope.getOrThrow(atom2);

                    return new SnbtGrammar.c(snbtgrammar_e, SnbtGrammar.b.BINARY, s2, snbtgrammar_d);
                }
            }
        });
        Atom<SnbtGrammar.i> atom6 = Atom.<SnbtGrammar.i>of("float_type_suffix");

        dictionary.put(atom6, Term.alternative(Term.sequence(StringReaderTerms.characters('f', 'F'), Term.marker(atom6, SnbtGrammar.i.FLOAT)), Term.sequence(StringReaderTerms.characters('d', 'D'), Term.marker(atom6, SnbtGrammar.i.DOUBLE))), (scope) -> {
            return (SnbtGrammar.i) scope.getOrThrow(atom6);
        });
        Atom<SnbtGrammar.f<String>> atom7 = Atom.<SnbtGrammar.f<String>>of("float_exponent_part");

        dictionary.put(atom7, Term.sequence(StringReaderTerms.characters('e', 'E'), Term.optional(dictionary.named(atom)), dictionary.named(atom3)), (scope) -> {
            return new SnbtGrammar.f((SnbtGrammar.e) scope.getOrDefault(atom, SnbtGrammar.e.PLUS), (String) scope.getOrThrow(atom3));
        });
        Atom<String> atom8 = Atom.<String>of("float_whole_part");
        Atom<String> atom9 = Atom.<String>of("float_fraction_part");
        Atom<T> atom10 = Atom.<T>of("float_literal");

        dictionary.putComplex(atom10, Term.sequence(Term.optional(dictionary.named(atom)), Term.alternative(Term.sequence(dictionary.namedWithAlias(atom3, atom8), StringReaderTerms.character('.'), Term.cut(), Term.optional(dictionary.namedWithAlias(atom3, atom9)), Term.optional(dictionary.named(atom7)), Term.optional(dictionary.named(atom6))), Term.sequence(StringReaderTerms.character('.'), Term.cut(), dictionary.namedWithAlias(atom3, atom9), Term.optional(dictionary.named(atom7)), Term.optional(dictionary.named(atom6))), Term.sequence(dictionary.namedWithAlias(atom3, atom8), dictionary.named(atom7), Term.cut(), Term.optional(dictionary.named(atom6))), Term.sequence(dictionary.namedWithAlias(atom3, atom8), Term.optional(dictionary.named(atom7)), dictionary.named(atom6)))), (parsestate) -> {
            Scope scope = parsestate.scope();
            SnbtGrammar.e snbtgrammar_e = (SnbtGrammar.e) scope.getOrDefault(atom, SnbtGrammar.e.PLUS);
            String s = (String) scope.get(atom8);
            String s1 = (String) scope.get(atom9);
            SnbtGrammar.f<String> snbtgrammar_f = (SnbtGrammar.f) scope.get(atom7);
            SnbtGrammar.i snbtgrammar_i = (SnbtGrammar.i) scope.get(atom6);

            return createFloat(dynamicops, snbtgrammar_e, s, s1, snbtgrammar_f, snbtgrammar_i, parsestate);
        });
        Atom<String> atom11 = Atom.<String>of("string_hex_2");

        dictionary.put(atom11, new SnbtGrammar.h(2));
        Atom<String> atom12 = Atom.<String>of("string_hex_4");

        dictionary.put(atom12, new SnbtGrammar.h(4));
        Atom<String> atom13 = Atom.<String>of("string_hex_8");

        dictionary.put(atom13, new SnbtGrammar.h(8));
        Atom<String> atom14 = Atom.<String>of("string_unicode_name");

        dictionary.put(atom14, new GreedyPatternParseRule(SnbtGrammar.UNICODE_NAME, SnbtGrammar.ERROR_INVALID_CHARACTER_NAME));
        Atom<String> atom15 = Atom.<String>of("string_escape_sequence");

        dictionary.putComplex(atom15, Term.alternative(Term.sequence(StringReaderTerms.character('b'), Term.marker(atom15, "\b")), Term.sequence(StringReaderTerms.character('s'), Term.marker(atom15, " ")), Term.sequence(StringReaderTerms.character('t'), Term.marker(atom15, "\t")), Term.sequence(StringReaderTerms.character('n'), Term.marker(atom15, "\n")), Term.sequence(StringReaderTerms.character('f'), Term.marker(atom15, "\f")), Term.sequence(StringReaderTerms.character('r'), Term.marker(atom15, "\r")), Term.sequence(StringReaderTerms.character('\\'), Term.marker(atom15, "\\")), Term.sequence(StringReaderTerms.character('\''), Term.marker(atom15, "'")), Term.sequence(StringReaderTerms.character('"'), Term.marker(atom15, "\"")), Term.sequence(StringReaderTerms.character('x'), dictionary.named(atom11)), Term.sequence(StringReaderTerms.character('u'), dictionary.named(atom12)), Term.sequence(StringReaderTerms.character('U'), dictionary.named(atom13)), Term.sequence(StringReaderTerms.character('N'), StringReaderTerms.character('{'), dictionary.named(atom14), StringReaderTerms.character('}'))), (parsestate) -> {
            Scope scope = parsestate.scope();
            String s = (String) scope.getAny(atom15);

            if (s != null) {
                return s;
            } else {
                String s1 = (String) scope.getAny(atom11, atom12, atom13);

                if (s1 != null) {
                    int i = HexFormat.fromHexDigits(s1);

                    if (!Character.isValidCodePoint(i)) {
                        parsestate.errorCollector().store(parsestate.mark(), DelayedException.create(SnbtGrammar.ERROR_INVALID_CODEPOINT, String.format(Locale.ROOT, "U+%08X", i)));
                        return null;
                    } else {
                        return Character.toString(i);
                    }
                } else {
                    String s2 = (String) scope.getOrThrow(atom14);

                    int j;

                    try {
                        j = Character.codePointOf(s2);
                    } catch (IllegalArgumentException illegalargumentexception) {
                        parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_INVALID_CHARACTER_NAME);
                        return null;
                    }

                    return Character.toString(j);
                }
            }
        });
        Atom<String> atom16 = Atom.<String>of("string_plain_contents");

        dictionary.put(atom16, SnbtGrammar.PLAIN_STRING_CHUNK);
        Atom<List<String>> atom17 = Atom.<List<String>>of("string_chunks");
        Atom<String> atom18 = Atom.<String>of("string_contents");
        Atom<String> atom19 = Atom.<String>of("single_quoted_string_chunk");
        NamedRule<StringReader, String> namedrule1 = dictionary.put(atom19, Term.alternative(dictionary.namedWithAlias(atom16, atom18), Term.sequence(StringReaderTerms.character('\\'), dictionary.namedWithAlias(atom15, atom18)), Term.sequence(StringReaderTerms.character('"'), Term.marker(atom18, "\""))), (scope) -> {
            return (String) scope.getOrThrow(atom18);
        });
        Atom<String> atom20 = Atom.<String>of("single_quoted_string_contents");

        dictionary.put(atom20, Term.repeated(namedrule1, atom17), (scope) -> {
            return joinList((List) scope.getOrThrow(atom17));
        });
        Atom<String> atom21 = Atom.<String>of("double_quoted_string_chunk");
        NamedRule<StringReader, String> namedrule2 = dictionary.put(atom21, Term.alternative(dictionary.namedWithAlias(atom16, atom18), Term.sequence(StringReaderTerms.character('\\'), dictionary.namedWithAlias(atom15, atom18)), Term.sequence(StringReaderTerms.character('\''), Term.marker(atom18, "'"))), (scope) -> {
            return (String) scope.getOrThrow(atom18);
        });
        Atom<String> atom22 = Atom.<String>of("double_quoted_string_contents");

        dictionary.put(atom22, Term.repeated(namedrule2, atom17), (scope) -> {
            return joinList((List) scope.getOrThrow(atom17));
        });
        Atom<String> atom23 = Atom.<String>of("quoted_string_literal");

        dictionary.put(atom23, Term.alternative(Term.sequence(StringReaderTerms.character('"'), Term.cut(), Term.optional(dictionary.namedWithAlias(atom22, atom18)), StringReaderTerms.character('"')), Term.sequence(StringReaderTerms.character('\''), Term.optional(dictionary.namedWithAlias(atom20, atom18)), StringReaderTerms.character('\''))), (scope) -> {
            return (String) scope.getOrThrow(atom18);
        });
        Atom<String> atom24 = Atom.<String>of("unquoted_string");

        dictionary.put(atom24, new UnquotedStringParseRule(1, SnbtGrammar.ERROR_EXPECTED_UNQUOTED_STRING));
        Atom<T> atom25 = Atom.<T>of("literal");
        Atom<List<T>> atom26 = Atom.<List<T>>of("arguments");

        dictionary.put(atom26, Term.repeatedWithTrailingSeparator(dictionary.forward(atom25), atom26, StringReaderTerms.character(',')), (scope) -> {
            return (List) scope.getOrThrow(atom26);
        });
        Atom<T> atom27 = Atom.<T>of("unquoted_string_or_builtin");

        dictionary.putComplex(atom27, Term.sequence(dictionary.named(atom24), Term.optional(Term.sequence(StringReaderTerms.character('('), dictionary.named(atom26), StringReaderTerms.character(')')))), (parsestate) -> {
            Scope scope = parsestate.scope();
            String s = (String) scope.getOrThrow(atom24);

            if (!s.isEmpty() && isAllowedToStartUnquotedString(s.charAt(0))) {
                List<T> list = (List) scope.get(atom26);

                if (list != null) {
                    SnbtOperations.a snbtoperations_a = new SnbtOperations.a(s, list.size());
                    SnbtOperations.b snbtoperations_b = (SnbtOperations.b) SnbtOperations.BUILTIN_OPERATIONS.get(snbtoperations_a);

                    if (snbtoperations_b != null) {
                        return snbtoperations_b.run(dynamicops, list, parsestate);
                    } else {
                        parsestate.errorCollector().store(parsestate.mark(), DelayedException.create(SnbtGrammar.ERROR_NO_SUCH_OPERATION, snbtoperations_a.toString()));
                        return null;
                    }
                } else {
                    return s.equalsIgnoreCase("true") ? t0 : (s.equalsIgnoreCase("false") ? t1 : dynamicops.createString(s));
                }
            } else {
                parsestate.errorCollector().store(parsestate.mark(), SnbtOperations.BUILTIN_IDS, SnbtGrammar.ERROR_INVALID_UNQUOTED_START);
                return null;
            }
        });
        Atom<String> atom28 = Atom.<String>of("map_key");

        dictionary.put(atom28, Term.alternative(dictionary.named(atom23), dictionary.named(atom24)), (scope) -> {
            return (String) scope.getAnyOrThrow(atom23, atom24);
        });
        Atom<Map.Entry<String, T>> atom29 = Atom.<Map.Entry<String, T>>of("map_entry");
        NamedRule<StringReader, Map.Entry<String, T>> namedrule3 = dictionary.putComplex(atom29, Term.sequence(dictionary.named(atom28), StringReaderTerms.character(':'), dictionary.named(atom25)), (parsestate) -> {
            Scope scope = parsestate.scope();
            String s = (String) scope.getOrThrow(atom28);

            if (s.isEmpty()) {
                parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_EMPTY_KEY);
                return null;
            } else {
                T t4 = (T) scope.getOrThrow(atom25);

                return Map.entry(s, t4);
            }
        });
        Atom<List<Map.Entry<String, T>>> atom30 = Atom.<List<Map.Entry<String, T>>>of("map_entries");

        dictionary.put(atom30, Term.repeatedWithTrailingSeparator(namedrule3, atom30, StringReaderTerms.character(',')), (scope) -> {
            return (List) scope.getOrThrow(atom30);
        });
        Atom<T> atom31 = Atom.<T>of("map_literal");

        dictionary.put(atom31, Term.sequence(StringReaderTerms.character('{'), dictionary.named(atom30), StringReaderTerms.character('}')), (scope) -> {
            List<Map.Entry<String, T>> list = (List) scope.getOrThrow(atom30);

            if (list.isEmpty()) {
                return t2;
            } else {
                ImmutableMap.Builder<T, T> immutablemap_builder = ImmutableMap.builderWithExpectedSize(list.size());

                for (Map.Entry<String, T> map_entry : list) {
                    immutablemap_builder.put(dynamicops.createString((String) map_entry.getKey()), map_entry.getValue());
                }

                return dynamicops.createMap(immutablemap_builder.buildKeepingLast());
            }
        });
        Atom<List<T>> atom32 = Atom.<List<T>>of("list_entries");

        dictionary.put(atom32, Term.repeatedWithTrailingSeparator(dictionary.forward(atom25), atom32, StringReaderTerms.character(',')), (scope) -> {
            return (List) scope.getOrThrow(atom32);
        });
        Atom<SnbtGrammar.a> atom33 = Atom.<SnbtGrammar.a>of("array_prefix");

        dictionary.put(atom33, Term.alternative(Term.sequence(StringReaderTerms.character('B'), Term.marker(atom33, SnbtGrammar.a.BYTE)), Term.sequence(StringReaderTerms.character('L'), Term.marker(atom33, SnbtGrammar.a.LONG)), Term.sequence(StringReaderTerms.character('I'), Term.marker(atom33, SnbtGrammar.a.INT))), (scope) -> {
            return (SnbtGrammar.a) scope.getOrThrow(atom33);
        });
        Atom<List<SnbtGrammar.c>> atom34 = Atom.<List<SnbtGrammar.c>>of("int_array_entries");

        dictionary.put(atom34, Term.repeatedWithTrailingSeparator(namedrule, atom34, StringReaderTerms.character(',')), (scope) -> {
            return (List) scope.getOrThrow(atom34);
        });
        Atom<T> atom35 = Atom.<T>of("list_literal");

        dictionary.putComplex(atom35, Term.sequence(StringReaderTerms.character('['), Term.alternative(Term.sequence(dictionary.named(atom33), StringReaderTerms.character(';'), dictionary.named(atom34)), dictionary.named(atom32)), StringReaderTerms.character(']')), (parsestate) -> {
            Scope scope = parsestate.scope();
            SnbtGrammar.a snbtgrammar_a = (SnbtGrammar.a) scope.get(atom33);

            if (snbtgrammar_a != null) {
                List<SnbtGrammar.c> list = (List) scope.getOrThrow(atom34);

                return list.isEmpty() ? snbtgrammar_a.create(dynamicops) : snbtgrammar_a.create(dynamicops, list, parsestate);
            } else {
                List<T> list1 = (List) scope.getOrThrow(atom32);

                return list1.isEmpty() ? t3 : dynamicops.createList(list1.stream());
            }
        });
        NamedRule<StringReader, T> namedrule4 = dictionary.putComplex(atom25, Term.alternative(Term.sequence(Term.positiveLookahead(SnbtGrammar.NUMBER_LOOKEAHEAD), Term.alternative(dictionary.namedWithAlias(atom10, atom25), dictionary.named(atom5))), Term.sequence(Term.positiveLookahead(StringReaderTerms.characters('"', '\'')), Term.cut(), dictionary.named(atom23)), Term.sequence(Term.positiveLookahead(StringReaderTerms.character('{')), Term.cut(), dictionary.namedWithAlias(atom31, atom25)), Term.sequence(Term.positiveLookahead(StringReaderTerms.character('[')), Term.cut(), dictionary.namedWithAlias(atom35, atom25)), dictionary.namedWithAlias(atom27, atom25)), (parsestate) -> {
            Scope scope = parsestate.scope();
            String s = (String) scope.get(atom23);

            if (s != null) {
                return dynamicops.createString(s);
            } else {
                SnbtGrammar.c snbtgrammar_c = (SnbtGrammar.c) scope.get(atom5);

                return snbtgrammar_c != null ? snbtgrammar_c.create(dynamicops, parsestate) : scope.getOrThrow(atom25);
            }
        });

        return new Grammar<T>(dictionary, namedrule4);
    }

    private static enum e {

        PLUS, MINUS;

        private e() {}

        public void append(StringBuilder stringbuilder) {
            if (this == SnbtGrammar.e.MINUS) {
                stringbuilder.append("-");
            }

        }
    }

    private static enum b {

        BINARY, DECIMAL, HEX;

        private b() {}
    }

    private static enum i {

        FLOAT, DOUBLE, BYTE, SHORT, INT, LONG;

        private i() {}
    }

    private static enum g {

        SIGNED, UNSIGNED;

        private g() {}
    }

    private static record d(@Nullable SnbtGrammar.g signed, @Nullable SnbtGrammar.i type) {

        public static final SnbtGrammar.d EMPTY = new SnbtGrammar.d((SnbtGrammar.g) null, (SnbtGrammar.i) null);
    }

    private static enum a {

        BYTE(SnbtGrammar.i.BYTE, new SnbtGrammar.i[0]) {
            private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

            @Override
            public <T> T create(DynamicOps<T> dynamicops) {
                return (T) dynamicops.createByteList(null.EMPTY_BUFFER);
            }

            @Nullable
            @Override
            public <T> T create(DynamicOps<T> dynamicops, List<SnbtGrammar.c> list, ParseState<?> parsestate) {
                ByteList bytelist = new ByteArrayList();

                for (SnbtGrammar.c snbtgrammar_c : list) {
                    Number number = this.buildNumber(snbtgrammar_c, parsestate);

                    if (number == null) {
                        return null;
                    }

                    bytelist.add(number.byteValue());
                }

                return (T) dynamicops.createByteList(ByteBuffer.wrap(bytelist.toByteArray()));
            }
        },
        INT(SnbtGrammar.i.INT, new SnbtGrammar.i[]{SnbtGrammar.i.BYTE, SnbtGrammar.i.SHORT}) {
            @Override
            public <T> T create(DynamicOps<T> dynamicops) {
                return (T) dynamicops.createIntList(IntStream.empty());
            }

            @Nullable
            @Override
            public <T> T create(DynamicOps<T> dynamicops, List<SnbtGrammar.c> list, ParseState<?> parsestate) {
                Builder builder = IntStream.builder();

                for (SnbtGrammar.c snbtgrammar_c : list) {
                    Number number = this.buildNumber(snbtgrammar_c, parsestate);

                    if (number == null) {
                        return null;
                    }

                    builder.add(number.intValue());
                }

                return (T) dynamicops.createIntList(builder.build());
            }
        },
        LONG(SnbtGrammar.i.LONG, new SnbtGrammar.i[]{SnbtGrammar.i.BYTE, SnbtGrammar.i.SHORT, SnbtGrammar.i.INT}) {
            @Override
            public <T> T create(DynamicOps<T> dynamicops) {
                return (T) dynamicops.createLongList(LongStream.empty());
            }

            @Nullable
            @Override
            public <T> T create(DynamicOps<T> dynamicops, List<SnbtGrammar.c> list, ParseState<?> parsestate) {
                java.util.stream.LongStream.Builder java_util_stream_longstream_builder = LongStream.builder();

                for (SnbtGrammar.c snbtgrammar_c : list) {
                    Number number = this.buildNumber(snbtgrammar_c, parsestate);

                    if (number == null) {
                        return null;
                    }

                    java_util_stream_longstream_builder.add(number.longValue());
                }

                return (T) dynamicops.createLongList(java_util_stream_longstream_builder.build());
            }
        };

        private final SnbtGrammar.i defaultType;
        private final Set<SnbtGrammar.i> additionalTypes;

        a(final SnbtGrammar.i snbtgrammar_i, final SnbtGrammar.i... asnbtgrammar_i) {
            this.additionalTypes = Set.of(asnbtgrammar_i);
            this.defaultType = snbtgrammar_i;
        }

        public boolean isAllowed(SnbtGrammar.i snbtgrammar_i) {
            return snbtgrammar_i == this.defaultType || this.additionalTypes.contains(snbtgrammar_i);
        }

        public abstract <T> T create(DynamicOps<T> dynamicops);

        @Nullable
        public abstract <T> T create(DynamicOps<T> dynamicops, List<SnbtGrammar.c> list, ParseState<?> parsestate);

        @Nullable
        protected Number buildNumber(SnbtGrammar.c snbtgrammar_c, ParseState<?> parsestate) {
            SnbtGrammar.i snbtgrammar_i = this.computeType(snbtgrammar_c.suffix);

            if (snbtgrammar_i == null) {
                parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_INVALID_ARRAY_ELEMENT_TYPE);
                return null;
            } else {
                return (Number) snbtgrammar_c.create(JavaOps.INSTANCE, snbtgrammar_i, parsestate);
            }
        }

        @Nullable
        private SnbtGrammar.i computeType(SnbtGrammar.d snbtgrammar_d) {
            SnbtGrammar.i snbtgrammar_i = snbtgrammar_d.type();

            return snbtgrammar_i == null ? this.defaultType : (!this.isAllowed(snbtgrammar_i) ? null : snbtgrammar_i);
        }
    }

    private static class h extends GreedyPredicateParseRule {

        public h(int i) {
            super(i, i, DelayedException.create(SnbtGrammar.ERROR_EXPECTED_HEX_ESCAPE, String.valueOf(i)));
        }

        @Override
        protected boolean isAccepted(char c0) {
            boolean flag;

            switch (c0) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    flag = true;
                    break;
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '_':
                case '`':
                default:
                    flag = false;
            }

            return flag;
        }
    }

    private static record c(SnbtGrammar.e sign, SnbtGrammar.b base, String digits, SnbtGrammar.d suffix) {

        private SnbtGrammar.g signedOrDefault() {
            if (this.suffix.signed != null) {
                return this.suffix.signed;
            } else {
                SnbtGrammar.g snbtgrammar_g;

                switch (this.base.ordinal()) {
                    case 0:
                    case 2:
                        snbtgrammar_g = SnbtGrammar.g.UNSIGNED;
                        break;
                    case 1:
                        snbtgrammar_g = SnbtGrammar.g.SIGNED;
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }

                return snbtgrammar_g;
            }
        }

        private String cleanupDigits(SnbtGrammar.e snbtgrammar_e) {
            boolean flag = SnbtGrammar.needsUnderscoreRemoval(this.digits);

            if (snbtgrammar_e != SnbtGrammar.e.MINUS && !flag) {
                return this.digits;
            } else {
                StringBuilder stringbuilder = new StringBuilder();

                snbtgrammar_e.append(stringbuilder);
                SnbtGrammar.cleanAndAppend(stringbuilder, this.digits, flag);
                return stringbuilder.toString();
            }
        }

        @Nullable
        public <T> T create(DynamicOps<T> dynamicops, ParseState<?> parsestate) {
            return (T) this.create(dynamicops, (SnbtGrammar.i) Objects.requireNonNullElse(this.suffix.type, SnbtGrammar.i.INT), parsestate);
        }

        @Nullable
        public <T> T create(DynamicOps<T> dynamicops, SnbtGrammar.i snbtgrammar_i, ParseState<?> parsestate) {
            boolean flag = this.signedOrDefault() == SnbtGrammar.g.SIGNED;

            if (!flag && this.sign == SnbtGrammar.e.MINUS) {
                parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_EXPECTED_NON_NEGATIVE_NUMBER);
                return null;
            } else {
                String s = this.cleanupDigits(this.sign);
                byte b0;

                switch (this.base.ordinal()) {
                    case 0:
                        b0 = 2;
                        break;
                    case 1:
                        b0 = 10;
                        break;
                    case 2:
                        b0 = 16;
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }

                int i = b0;

                try {
                    if (flag) {
                        Object object;

                        switch (snbtgrammar_i.ordinal()) {
                            case 2:
                                object = dynamicops.createByte(Byte.parseByte(s, i));
                                break;
                            case 3:
                                object = dynamicops.createShort(Short.parseShort(s, i));
                                break;
                            case 4:
                                object = dynamicops.createInt(Integer.parseInt(s, i));
                                break;
                            case 5:
                                object = dynamicops.createLong(Long.parseLong(s, i));
                                break;
                            default:
                                parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_EXPECTED_INTEGER_TYPE);
                                object = null;
                        }

                        return (T) object;
                    } else {
                        Object object1;

                        switch (snbtgrammar_i.ordinal()) {
                            case 2:
                                object1 = dynamicops.createByte(UnsignedBytes.parseUnsignedByte(s, i));
                                break;
                            case 3:
                                object1 = dynamicops.createShort(SnbtGrammar.parseUnsignedShort(s, i));
                                break;
                            case 4:
                                object1 = dynamicops.createInt(Integer.parseUnsignedInt(s, i));
                                break;
                            case 5:
                                object1 = dynamicops.createLong(Long.parseUnsignedLong(s, i));
                                break;
                            default:
                                parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.ERROR_EXPECTED_INTEGER_TYPE);
                                object1 = null;
                        }

                        return (T) object1;
                    }
                } catch (NumberFormatException numberformatexception) {
                    parsestate.errorCollector().store(parsestate.mark(), SnbtGrammar.createNumberParseError(numberformatexception));
                    return null;
                }
            }
        }
    }

    private static record f<T>(SnbtGrammar.e sign, T value) {

    }
}
