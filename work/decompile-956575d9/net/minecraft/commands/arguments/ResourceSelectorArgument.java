package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.io.FilenameUtils;

public class ResourceSelectorArgument<T> implements ArgumentType<Collection<Holder.c<T>>> {

    private static final Collection<String> EXAMPLES = List.of("minecraft:*", "*:asset", "*");
    public static final Dynamic2CommandExceptionType ERROR_NO_MATCHES = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("argument.resource_selector.not_found", object, object1);
    });
    final ResourceKey<? extends IRegistry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    ResourceSelectorArgument(CommandBuildContext commandbuildcontext, ResourceKey<? extends IRegistry<T>> resourcekey) {
        this.registryKey = resourcekey;
        this.registryLookup = commandbuildcontext.lookupOrThrow(resourcekey);
    }

    public Collection<Holder.c<T>> parse(StringReader stringreader) throws CommandSyntaxException {
        String s = ensureNamespaced(readPattern(stringreader));
        List<Holder.c<T>> list = this.registryLookup.listElements().filter((holder_c) -> {
            return matches(s, holder_c.key().location());
        }).toList();

        if (list.isEmpty()) {
            throw ResourceSelectorArgument.ERROR_NO_MATCHES.createWithContext(stringreader, s, this.registryKey.location());
        } else {
            return list;
        }
    }

    public static <T> Collection<Holder.c<T>> parse(StringReader stringreader, HolderLookup<T> holderlookup) {
        String s = ensureNamespaced(readPattern(stringreader));

        return holderlookup.listElements().filter((holder_c) -> {
            return matches(s, holder_c.key().location());
        }).toList();
    }

    private static String readPattern(StringReader stringreader) {
        int i = stringreader.getCursor();

        while (stringreader.canRead() && isAllowedPatternCharacter(stringreader.peek())) {
            stringreader.skip();
        }

        return stringreader.getString().substring(i, stringreader.getCursor());
    }

    private static boolean isAllowedPatternCharacter(char c0) {
        return MinecraftKey.isAllowedInResourceLocation(c0) || c0 == '*' || c0 == '?';
    }

    private static String ensureNamespaced(String s) {
        return !s.contains(":") ? "minecraft:" + s : s;
    }

    private static boolean matches(String s, MinecraftKey minecraftkey) {
        return FilenameUtils.wildcardMatch(minecraftkey.toString(), s);
    }

    public static <T> ResourceSelectorArgument<T> resourceSelector(CommandBuildContext commandbuildcontext, ResourceKey<? extends IRegistry<T>> resourcekey) {
        return new ResourceSelectorArgument<T>(commandbuildcontext, resourcekey);
    }

    public static <T> Collection<Holder.c<T>> getSelectedResources(CommandContext<CommandListenerWrapper> commandcontext, String s, ResourceKey<? extends IRegistry<T>> resourcekey) {
        return (Collection) commandcontext.getArgument(s, Collection.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        Object object = commandcontext.getSource();

        if (object instanceof ICompletionProvider icompletionprovider) {
            return icompletionprovider.suggestRegistryElements(this.registryKey, ICompletionProvider.a.ELEMENTS, suggestionsbuilder, commandcontext);
        } else {
            return ICompletionProvider.suggest(this.registryLookup.listElementIds().map(ResourceKey::location).map(MinecraftKey::toString), suggestionsbuilder);
        }
    }

    public Collection<String> getExamples() {
        return ResourceSelectorArgument.EXAMPLES;
    }

    public static class a<T> implements ArgumentTypeInfo<ResourceSelectorArgument<T>, ResourceSelectorArgument.a<T>.a> {

        public a() {}

        public void serializeToNetwork(ResourceSelectorArgument.a<T>.a resourceselectorargument_a_a, PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeResourceKey(resourceselectorargument_a_a.registryKey);
        }

        @Override
        public ResourceSelectorArgument.a<T>.a deserializeFromNetwork(PacketDataSerializer packetdataserializer) {
            return new ResourceSelectorArgument.a.a(packetdataserializer.readRegistryKey());
        }

        public void serializeToJson(ResourceSelectorArgument.a<T>.a resourceselectorargument_a_a, JsonObject jsonobject) {
            jsonobject.addProperty("registry", resourceselectorargument_a_a.registryKey.location().toString());
        }

        public ResourceSelectorArgument.a<T>.a unpack(ResourceSelectorArgument<T> resourceselectorargument) {
            return new ResourceSelectorArgument.a.a(resourceselectorargument.registryKey);
        }

        public final class a implements ArgumentTypeInfo.a<ResourceSelectorArgument<T>> {

            final ResourceKey<? extends IRegistry<T>> registryKey;

            a(final ResourceKey resourcekey) {
                this.registryKey = resourcekey;
            }

            @Override
            public ResourceSelectorArgument<T> instantiate(CommandBuildContext commandbuildcontext) {
                return new ResourceSelectorArgument<T>(commandbuildcontext, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceSelectorArgument<T>, ?> type() {
                return a.this;
            }
        }
    }
}
