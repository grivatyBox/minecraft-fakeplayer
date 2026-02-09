package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.Unit;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.ResourceLocationParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {

    public ComponentPredicateParser() {}

    public static <T, C, P> Grammar<List<T>> createGrammar(ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
        Atom<List<T>> atom = Atom.<List<T>>of("top");
        Atom<Optional<T>> atom1 = Atom.<Optional<T>>of("type");
        Atom<Unit> atom2 = Atom.<Unit>of("any_type");
        Atom<T> atom3 = Atom.<T>of("element_type");
        Atom<T> atom4 = Atom.<T>of("tag_type");
        Atom<List<T>> atom5 = Atom.<List<T>>of("conditions");
        Atom<List<T>> atom6 = Atom.<List<T>>of("alternatives");
        Atom<T> atom7 = Atom.<T>of("term");
        Atom<T> atom8 = Atom.<T>of("negation");
        Atom<T> atom9 = Atom.<T>of("test");
        Atom<C> atom10 = Atom.<C>of("component_type");
        Atom<P> atom11 = Atom.<P>of("predicate_type");
        Atom<MinecraftKey> atom12 = Atom.<MinecraftKey>of("id");
        Atom<Dynamic<?>> atom13 = Atom.<Dynamic<?>>of("tag");
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        NamedRule<StringReader, MinecraftKey> namedrule = dictionary.put(atom12, ResourceLocationParseRule.INSTANCE);
        NamedRule<StringReader, List<T>> namedrule1 = dictionary.put(atom, Term.alternative(Term.sequence(dictionary.named(atom1), StringReaderTerms.character('['), Term.cut(), Term.optional(dictionary.named(atom5)), StringReaderTerms.character(']')), dictionary.named(atom1)), (scope) -> {
            ImmutableList.Builder<T> immutablelist_builder = ImmutableList.builder();
            Optional optional = (Optional) scope.getOrThrow(atom1);

            Objects.requireNonNull(immutablelist_builder);
            optional.ifPresent(immutablelist_builder::add);
            List<T> list = (List) scope.get(atom5);

            if (list != null) {
                immutablelist_builder.addAll(list);
            }

            return immutablelist_builder.build();
        });

        dictionary.put(atom1, Term.alternative(dictionary.named(atom3), Term.sequence(StringReaderTerms.character('#'), Term.cut(), dictionary.named(atom4)), dictionary.named(atom2)), (scope) -> {
            return Optional.ofNullable(scope.getAny(atom3, atom4));
        });
        dictionary.put(atom2, StringReaderTerms.character('*'), (scope) -> {
            return Unit.INSTANCE;
        });
        dictionary.put(atom3, new ComponentPredicateParser.c(namedrule, componentpredicateparser_b));
        dictionary.put(atom4, new ComponentPredicateParser.e(namedrule, componentpredicateparser_b));
        dictionary.put(atom5, Term.sequence(dictionary.named(atom6), Term.optional(Term.sequence(StringReaderTerms.character(','), dictionary.named(atom5)))), (scope) -> {
            T t0 = componentpredicateparser_b.anyOf((List) scope.getOrThrow(atom6));

            return (List) Optional.ofNullable((List) scope.get(atom5)).map((list) -> {
                return SystemUtils.copyAndAdd(t0, list);
            }).orElse(List.of(t0));
        });
        dictionary.put(atom6, Term.sequence(dictionary.named(atom7), Term.optional(Term.sequence(StringReaderTerms.character('|'), dictionary.named(atom6)))), (scope) -> {
            T t0 = (T) scope.getOrThrow(atom7);

            return (List) Optional.ofNullable((List) scope.get(atom6)).map((list) -> {
                return SystemUtils.copyAndAdd(t0, list);
            }).orElse(List.of(t0));
        });
        dictionary.put(atom7, Term.alternative(dictionary.named(atom9), Term.sequence(StringReaderTerms.character('!'), dictionary.named(atom8))), (scope) -> {
            return scope.getAnyOrThrow(atom9, atom8);
        });
        dictionary.put(atom8, dictionary.named(atom9), (scope) -> {
            return componentpredicateparser_b.negate(scope.getOrThrow(atom9));
        });
        dictionary.putComplex(atom9, Term.alternative(Term.sequence(dictionary.named(atom10), StringReaderTerms.character('='), Term.cut(), dictionary.named(atom13)), Term.sequence(dictionary.named(atom11), StringReaderTerms.character('~'), Term.cut(), dictionary.named(atom13)), dictionary.named(atom10)), (parsestate) -> {
            Scope scope = parsestate.scope();
            P p0 = (P) scope.get(atom11);

            try {
                if (p0 != null) {
                    Dynamic<?> dynamic = (Dynamic) scope.getOrThrow(atom13);

                    return componentpredicateparser_b.createPredicateTest((ImmutableStringReader) parsestate.input(), p0, dynamic);
                } else {
                    C c0 = (C) scope.getOrThrow(atom10);
                    Dynamic<?> dynamic1 = (Dynamic) scope.get(atom13);

                    return dynamic1 != null ? componentpredicateparser_b.createComponentTest((ImmutableStringReader) parsestate.input(), c0, dynamic1) : componentpredicateparser_b.createComponentTest((ImmutableStringReader) parsestate.input(), c0);
                }
            } catch (CommandSyntaxException commandsyntaxexception) {
                parsestate.errorCollector().store(parsestate.mark(), commandsyntaxexception);
                return null;
            }
        });
        dictionary.put(atom10, new ComponentPredicateParser.a(namedrule, componentpredicateparser_b));
        dictionary.put(atom11, new ComponentPredicateParser.d(namedrule, componentpredicateparser_b));
        dictionary.put(atom13, new TagParseRule(DynamicOpsNBT.INSTANCE));
        return new Grammar<List<T>>(dictionary, namedrule1);
    }

    private static class c<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, T> {

        c(NamedRule<StringReader, MinecraftKey> namedrule, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(namedrule, componentpredicateparser_b);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return (T) ((ComponentPredicateParser.b) this.context).forElementType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listElementTypes();
        }
    }

    private static class e<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, T> {

        e(NamedRule<StringReader, MinecraftKey> namedrule, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(namedrule, componentpredicateparser_b);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return (T) ((ComponentPredicateParser.b) this.context).forTagType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listTagTypes();
        }
    }

    private static class a<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, C> {

        a(NamedRule<StringReader, MinecraftKey> namedrule, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(namedrule, componentpredicateparser_b);
        }

        @Override
        protected C validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return (C) ((ComponentPredicateParser.b) this.context).lookupComponentType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listComponentTypes();
        }
    }

    private static class d<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.b<T, C, P>, P> {

        d(NamedRule<StringReader, MinecraftKey> namedrule, ComponentPredicateParser.b<T, C, P> componentpredicateparser_b) {
            super(namedrule, componentpredicateparser_b);
        }

        @Override
        protected P validateElement(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws Exception {
            return (P) ((ComponentPredicateParser.b) this.context).lookupPredicateType(immutablestringreader, minecraftkey);
        }

        @Override
        public Stream<MinecraftKey> possibleResources() {
            return ((ComponentPredicateParser.b) this.context).listPredicateTypes();
        }
    }

    public interface b<T, C, P> {

        T forElementType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listElementTypes();

        T forTagType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listTagTypes();

        C lookupComponentType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listComponentTypes();

        T createComponentTest(ImmutableStringReader immutablestringreader, C c0, Dynamic<?> dynamic) throws CommandSyntaxException;

        T createComponentTest(ImmutableStringReader immutablestringreader, C c0);

        P lookupPredicateType(ImmutableStringReader immutablestringreader, MinecraftKey minecraftkey) throws CommandSyntaxException;

        Stream<MinecraftKey> listPredicateTypes();

        T createPredicateTest(ImmutableStringReader immutablestringreader, P p0, Dynamic<?> dynamic) throws CommandSyntaxException;

        T negate(T t0);

        T anyOf(List<T> list);
    }
}
