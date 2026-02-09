package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;

public class StyleArgument extends ParserBasedArgument<ChatModifier> {

    private static final Collection<String> EXAMPLES = List.of("{bold: true}", "{color: 'red'}", "{}");
    public static final DynamicCommandExceptionType ERROR_INVALID_STYLE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.style.invalid", object);
    });
    private static final DynamicOps<NBTBase> OPS = DynamicOpsNBT.INSTANCE;
    private static final CommandArgumentParser<NBTBase> TAG_PARSER = SnbtGrammar.<NBTBase>createParser(StyleArgument.OPS);

    private StyleArgument(HolderLookup.a holderlookup_a) {
        super(StyleArgument.TAG_PARSER.withCodec(holderlookup_a.createSerializationContext(StyleArgument.OPS), StyleArgument.TAG_PARSER, ChatModifier.ChatModifierSerializer.CODEC, StyleArgument.ERROR_INVALID_STYLE));
    }

    public static ChatModifier getStyle(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (ChatModifier) commandcontext.getArgument(s, ChatModifier.class);
    }

    public static StyleArgument style(CommandBuildContext commandbuildcontext) {
        return new StyleArgument(commandbuildcontext);
    }

    public Collection<String> getExamples() {
        return StyleArgument.EXAMPLES;
    }
}
