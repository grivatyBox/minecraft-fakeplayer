package net.minecraft.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;

public class SnbtOperations {

    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_STRING_UUID = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_string_uuid")));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NUMBER_OR_BOOLEAN = DelayedException.create(new SimpleCommandExceptionType(IChatBaseComponent.translatable("snbt.parser.expected_number_or_boolean")));
    public static final String BUILTIN_TRUE = "true";
    public static final String BUILTIN_FALSE = "false";
    public static final Map<SnbtOperations.a, SnbtOperations.b> BUILTIN_OPERATIONS = Map.of(new SnbtOperations.a("bool", 1), new SnbtOperations.b() {
        @Override
        public <T> T run(DynamicOps<T> dynamicops, List<T> list, ParseState<StringReader> parsestate) {
            Boolean obool = convert(dynamicops, list.getFirst());

            if (obool == null) {
                parsestate.errorCollector().store(parsestate.mark(), SnbtOperations.ERROR_EXPECTED_NUMBER_OR_BOOLEAN);
                return null;
            } else {
                return (T) dynamicops.createBoolean(obool);
            }
        }

        @Nullable
        private static <T> Boolean convert(DynamicOps<T> dynamicops, T t0) {
            Optional<Boolean> optional = dynamicops.getBooleanValue(t0).result();

            if (optional.isPresent()) {
                return (Boolean) optional.get();
            } else {
                Optional<Number> optional1 = dynamicops.getNumberValue(t0).result();

                return optional1.isPresent() ? ((Number) optional1.get()).doubleValue() != 0.0D : null;
            }
        }
    }, new SnbtOperations.a("uuid", 1), new SnbtOperations.b() {
        @Override
        public <T> T run(DynamicOps<T> dynamicops, List<T> list, ParseState<StringReader> parsestate) {
            Optional<String> optional = dynamicops.getStringValue(list.getFirst()).result();

            if (optional.isEmpty()) {
                parsestate.errorCollector().store(parsestate.mark(), SnbtOperations.ERROR_EXPECTED_STRING_UUID);
                return null;
            } else {
                UUID uuid;

                try {
                    uuid = UUID.fromString((String) optional.get());
                } catch (IllegalArgumentException illegalargumentexception) {
                    parsestate.errorCollector().store(parsestate.mark(), SnbtOperations.ERROR_EXPECTED_STRING_UUID);
                    return null;
                }

                return (T) dynamicops.createIntList(IntStream.of(UUIDUtil.uuidToIntArray(uuid)));
            }
        }
    });
    public static final SuggestionSupplier<StringReader> BUILTIN_IDS = new SuggestionSupplier<StringReader>() {
        private final Set<String> keys;

        {
            this.keys = (Set) Stream.concat(Stream.of("false", "true"), SnbtOperations.BUILTIN_OPERATIONS.keySet().stream().map(SnbtOperations.a::id)).collect(Collectors.toSet());
        }

        @Override
        public Stream<String> possibleValues(ParseState<StringReader> parsestate) {
            return this.keys.stream();
        }
    };

    public SnbtOperations() {}

    public static record a(String id, int argCount) {

        public String toString() {
            return this.id + "/" + this.argCount;
        }
    }

    public interface b {

        @Nullable
        <T> T run(DynamicOps<T> dynamicops, List<T> list, ParseState<StringReader> parsestate);
    }
}
