package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;

public class StyleArgument implements ArgumentType<ChatModifier> {

    private static final Collection<String> EXAMPLES = List.of("{\"bold\": true}\n");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.style.invalid", object);
    });

    private StyleArgument() {}

    public static ChatModifier getStyle(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (ChatModifier) commandcontext.getArgument(s, ChatModifier.class);
    }

    public static StyleArgument style() {
        return new StyleArgument();
    }

    public ChatModifier parse(StringReader stringreader) throws CommandSyntaxException {
        try {
            return (ChatModifier) ParserUtils.parseJson(stringreader, ChatModifier.ChatModifierSerializer.CODEC);
        } catch (Exception exception) {
            String s = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();

            throw StyleArgument.ERROR_INVALID_JSON.createWithContext(stringreader, s);
        }
    }

    public Collection<String> getExamples() {
        return StyleArgument.EXAMPLES;
    }
}
