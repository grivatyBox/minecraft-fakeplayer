package net.minecraft.commands.synchronization;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.EntityTypes;

public class CompletionProviders {

    private static final Map<MinecraftKey, SuggestionProvider<ICompletionProvider>> PROVIDERS_BY_NAME = new HashMap();
    private static final MinecraftKey ID_ASK_SERVER = MinecraftKey.withDefaultNamespace("ask_server");
    public static final SuggestionProvider<ICompletionProvider> ASK_SERVER = register(CompletionProviders.ID_ASK_SERVER, (commandcontext, suggestionsbuilder) -> {
        return ((ICompletionProvider) commandcontext.getSource()).customSuggestion(commandcontext);
    });
    public static final SuggestionProvider<ICompletionProvider> AVAILABLE_SOUNDS = register(MinecraftKey.withDefaultNamespace("available_sounds"), (commandcontext, suggestionsbuilder) -> {
        return ICompletionProvider.suggestResource(((ICompletionProvider) commandcontext.getSource()).getAvailableSounds(), suggestionsbuilder);
    });
    public static final SuggestionProvider<ICompletionProvider> SUMMONABLE_ENTITIES = register(MinecraftKey.withDefaultNamespace("summonable_entities"), (commandcontext, suggestionsbuilder) -> {
        return ICompletionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter((entitytypes) -> {
            return entitytypes.isEnabled(((ICompletionProvider) commandcontext.getSource()).enabledFeatures()) && entitytypes.canSummon();
        }), suggestionsbuilder, EntityTypes::getKey, EntityTypes::getDescription);
    });

    public CompletionProviders() {}

    public static <S extends ICompletionProvider> SuggestionProvider<S> register(MinecraftKey minecraftkey, SuggestionProvider<ICompletionProvider> suggestionprovider) {
        SuggestionProvider<ICompletionProvider> suggestionprovider1 = (SuggestionProvider) CompletionProviders.PROVIDERS_BY_NAME.putIfAbsent(minecraftkey, suggestionprovider);

        if (suggestionprovider1 != null) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name '" + String.valueOf(minecraftkey) + "'");
        } else {
            return new CompletionProviders.a(minecraftkey, suggestionprovider);
        }
    }

    public static <S extends ICompletionProvider> SuggestionProvider<S> cast(SuggestionProvider<ICompletionProvider> suggestionprovider) {
        return suggestionprovider;
    }

    public static <S extends ICompletionProvider> SuggestionProvider<S> getProvider(MinecraftKey minecraftkey) {
        return cast((SuggestionProvider) CompletionProviders.PROVIDERS_BY_NAME.getOrDefault(minecraftkey, CompletionProviders.ASK_SERVER));
    }

    public static MinecraftKey getName(SuggestionProvider<?> suggestionprovider) {
        MinecraftKey minecraftkey;

        if (suggestionprovider instanceof CompletionProviders.a completionproviders_a) {
            minecraftkey = completionproviders_a.name;
        } else {
            minecraftkey = CompletionProviders.ID_ASK_SERVER;
        }

        return minecraftkey;
    }

    private static record a(MinecraftKey name, SuggestionProvider<ICompletionProvider> delegate) implements SuggestionProvider<ICompletionProvider> {

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ICompletionProvider> commandcontext, SuggestionsBuilder suggestionsbuilder) throws CommandSyntaxException {
            return this.delegate.getSuggestions(commandcontext, suggestionsbuilder);
        }
    }
}
