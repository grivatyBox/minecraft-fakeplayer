package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;
import net.minecraft.world.entity.Entity;

public class ArgumentChatComponent extends ParserBasedArgument<IChatBaseComponent> {

    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "'hello world'", "\"\"", "{text:\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.component.invalid", object);
    });
    private static final DynamicOps<NBTBase> OPS = DynamicOpsNBT.INSTANCE;
    private static final CommandArgumentParser<NBTBase> TAG_PARSER = SnbtGrammar.<NBTBase>createParser(ArgumentChatComponent.OPS);

    private ArgumentChatComponent(HolderLookup.a holderlookup_a) {
        super(ArgumentChatComponent.TAG_PARSER.withCodec(holderlookup_a.createSerializationContext(ArgumentChatComponent.OPS), ArgumentChatComponent.TAG_PARSER, ComponentSerialization.CODEC, ArgumentChatComponent.ERROR_INVALID_COMPONENT));
    }

    public static IChatBaseComponent getRawComponent(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (IChatBaseComponent) commandcontext.getArgument(s, IChatBaseComponent.class);
    }

    public static IChatBaseComponent getResolvedComponent(CommandContext<CommandListenerWrapper> commandcontext, String s, @Nullable Entity entity) throws CommandSyntaxException {
        return ChatComponentUtils.updateForEntity((CommandListenerWrapper) commandcontext.getSource(), getRawComponent(commandcontext, s), entity, 0);
    }

    public static IChatBaseComponent getResolvedComponent(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return getResolvedComponent(commandcontext, s, ((CommandListenerWrapper) commandcontext.getSource()).getEntity());
    }

    public static ArgumentChatComponent textComponent(CommandBuildContext commandbuildcontext) {
        return new ArgumentChatComponent(commandbuildcontext);
    }

    public Collection<String> getExamples() {
        return ArgumentChatComponent.EXAMPLES;
    }
}
