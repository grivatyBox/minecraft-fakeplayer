package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.ResourceLocationParseRule;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceOrIdArgument<T> implements ArgumentType<Holder<T>> {

    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
    public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.resource_or_id.failed_to_parse", object);
    });
    public static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ELEMENT = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("argument.resource_or_id.no_such_element", object, object1);
    });
    public static final DynamicOps<NBTBase> OPS = DynamicOpsNBT.INSTANCE;
    private final HolderLookup.a registryLookup;
    private final Optional<? extends HolderLookup.b<T>> elementLookup;
    private final Codec<T> codec;
    private final Grammar<ResourceOrIdArgument.g<T, NBTBase>> grammar;
    private final ResourceKey<? extends IRegistry<T>> registryKey;

    protected ResourceOrIdArgument(CommandBuildContext commandbuildcontext, ResourceKey<? extends IRegistry<T>> resourcekey, Codec<T> codec) {
        this.registryLookup = commandbuildcontext;
        this.elementLookup = commandbuildcontext.lookup(resourcekey);
        this.registryKey = resourcekey;
        this.codec = codec;
        this.grammar = createGrammar(resourcekey, ResourceOrIdArgument.OPS);
    }

    public static <T, O> Grammar<ResourceOrIdArgument.g<T, O>> createGrammar(ResourceKey<? extends IRegistry<T>> resourcekey, DynamicOps<O> dynamicops) {
        Grammar<O> grammar = SnbtGrammar.<O>createParser(dynamicops);
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        Atom<ResourceOrIdArgument.g<T, O>> atom = Atom.<ResourceOrIdArgument.g<T, O>>of("result");
        Atom<MinecraftKey> atom1 = Atom.<MinecraftKey>of("id");
        Atom<O> atom2 = Atom.<O>of("value");

        dictionary.put(atom1, ResourceLocationParseRule.INSTANCE);
        dictionary.put(atom2, grammar.top().value());
        NamedRule<StringReader, ResourceOrIdArgument.g<T, O>> namedrule = dictionary.put(atom, Term.alternative(dictionary.named(atom1), dictionary.named(atom2)), (scope) -> {
            MinecraftKey minecraftkey = (MinecraftKey) scope.get(atom1);

            if (minecraftkey != null) {
                return new ResourceOrIdArgument.f(ResourceKey.create(resourcekey, minecraftkey));
            } else {
                O o0 = (O) scope.getOrThrow(atom2);

                return new ResourceOrIdArgument.b(o0);
            }
        });

        return new Grammar<ResourceOrIdArgument.g<T, O>>(dictionary, namedrule);
    }

    public static ResourceOrIdArgument.e lootTable(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.e(commandbuildcontext);
    }

    public static Holder<LootTable> getLootTable(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return getResource(commandcontext, s);
    }

    public static ResourceOrIdArgument.c lootModifier(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.c(commandbuildcontext);
    }

    public static Holder<LootItemFunction> getLootModifier(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return getResource(commandcontext, s);
    }

    public static ResourceOrIdArgument.d lootPredicate(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.d(commandbuildcontext);
    }

    public static Holder<LootItemCondition> getLootPredicate(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return getResource(commandcontext, s);
    }

    public static ResourceOrIdArgument.a dialog(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.a(commandbuildcontext);
    }

    public static Holder<Dialog> getDialog(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return getResource(commandcontext, s);
    }

    private static <T> Holder<T> getResource(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (Holder) commandcontext.getArgument(s, Holder.class);
    }

    @Nullable
    public Holder<T> parse(StringReader stringreader) throws CommandSyntaxException {
        return this.parse(stringreader, this.grammar, ResourceOrIdArgument.OPS);
    }

    @Nullable
    private <O> Holder<T> parse(StringReader stringreader, Grammar<ResourceOrIdArgument.g<T, O>> grammar, DynamicOps<O> dynamicops) throws CommandSyntaxException {
        ResourceOrIdArgument.g<T, O> resourceoridargument_g = (ResourceOrIdArgument.g) grammar.parseForCommands(stringreader);

        return this.elementLookup.isEmpty() ? null : resourceoridargument_g.parse(stringreader, this.registryLookup, dynamicops, this.codec, (HolderLookup.b) this.elementLookup.get());
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        return ICompletionProvider.listSuggestions(commandcontext, suggestionsbuilder, this.registryKey, ICompletionProvider.a.ELEMENTS);
    }

    public Collection<String> getExamples() {
        return ResourceOrIdArgument.EXAMPLES;
    }

    public static class e extends ResourceOrIdArgument<LootTable> {

        protected e(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.LOOT_TABLE, LootTable.DIRECT_CODEC);
        }
    }

    public static class c extends ResourceOrIdArgument<LootItemFunction> {

        protected c(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC);
        }
    }

    public static class d extends ResourceOrIdArgument<LootItemCondition> {

        protected d(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);
        }
    }

    public static class a extends ResourceOrIdArgument<Dialog> {

        protected a(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.DIALOG, Dialog.DIRECT_CODEC);
        }
    }

    public static record b<T, O>(O value) implements ResourceOrIdArgument.g<T, O> {

        @Override
        public Holder<T> parse(ImmutableStringReader immutablestringreader, HolderLookup.a holderlookup_a, DynamicOps<O> dynamicops, Codec<T> codec, HolderLookup.b<T> holderlookup_b) throws CommandSyntaxException {
            return Holder.<T>direct(codec.parse(holderlookup_a.createSerializationContext(dynamicops), this.value).getOrThrow((s) -> {
                return ResourceOrIdArgument.ERROR_FAILED_TO_PARSE.createWithContext(immutablestringreader, s);
            }));
        }
    }

    public static record f<T, O>(ResourceKey<T> key) implements ResourceOrIdArgument.g<T, O> {

        @Override
        public Holder<T> parse(ImmutableStringReader immutablestringreader, HolderLookup.a holderlookup_a, DynamicOps<O> dynamicops, Codec<T> codec, HolderLookup.b<T> holderlookup_b) throws CommandSyntaxException {
            return (Holder) holderlookup_b.get(this.key).orElseThrow(() -> {
                return ResourceOrIdArgument.ERROR_NO_SUCH_ELEMENT.createWithContext(immutablestringreader, this.key.location(), this.key.registry());
            });
        }
    }

    public sealed interface g<T, O> permits ResourceOrIdArgument.b, ResourceOrIdArgument.f {

        Holder<T> parse(ImmutableStringReader immutablestringreader, HolderLookup.a holderlookup_a, DynamicOps<O> dynamicops, Codec<T> codec, HolderLookup.b<T> holderlookup_b) throws CommandSyntaxException;
    }
}
