package net.minecraft.util.parsing.packrat;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Dictionary<S> {

    private final Map<Atom<?>, Dictionary.a<S, ?>> terms = new IdentityHashMap();

    public Dictionary() {}

    public <T> NamedRule<S, T> put(Atom<T> atom, Rule<S, T> rule) {
        Dictionary.a<S, T> dictionary_a = (Dictionary.a) this.terms.computeIfAbsent(atom, Dictionary.a::new);

        if (dictionary_a.value != null) {
            throw new IllegalArgumentException("Trying to override rule: " + String.valueOf(atom));
        } else {
            dictionary_a.value = rule;
            return dictionary_a;
        }
    }

    public <T> NamedRule<S, T> putComplex(Atom<T> atom, Term<S> term, Rule.a<S, T> rule_a) {
        return this.put(atom, Rule.fromTerm(term, rule_a));
    }

    public <T> NamedRule<S, T> put(Atom<T> atom, Term<S> term, Rule.b<S, T> rule_b) {
        return this.put(atom, Rule.fromTerm(term, rule_b));
    }

    public void checkAllBound() {
        List<? extends Atom<?>> list = this.terms.entrySet().stream().filter((entry) -> {
            return entry.getValue() == null;
        }).map(Entry::getKey).toList();

        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound names: " + String.valueOf(list));
        }
    }

    public <T> NamedRule<S, T> getOrThrow(Atom<T> atom) {
        return (NamedRule) Objects.requireNonNull((Dictionary.a) this.terms.get(atom), () -> {
            return "No rule called " + String.valueOf(atom);
        });
    }

    public <T> NamedRule<S, T> forward(Atom<T> atom) {
        return this.getOrCreateEntry(atom);
    }

    private <T> Dictionary.a<S, T> getOrCreateEntry(Atom<T> atom) {
        return (Dictionary.a) this.terms.computeIfAbsent(atom, Dictionary.a::new);
    }

    public <T> Term<S> named(Atom<T> atom) {
        return new Dictionary.b(this.getOrCreateEntry(atom), atom);
    }

    public <T> Term<S> namedWithAlias(Atom<T> atom, Atom<T> atom1) {
        return new Dictionary.b(this.getOrCreateEntry(atom), atom1);
    }

    private static record b<S, T>(Dictionary.a<S, T> ruleToParse, Atom<T> nameToStore) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            T t0 = (T) parsestate.parse(this.ruleToParse);

            if (t0 == null) {
                return false;
            } else {
                scope.put(this.nameToStore, t0);
                return true;
            }
        }
    }

    private static class a<S, T> implements NamedRule<S, T>, Supplier<String> {

        private final Atom<T> name;
        @Nullable
        Rule<S, T> value;

        private a(Atom<T> atom) {
            this.name = atom;
        }

        @Override
        public Atom<T> name() {
            return this.name;
        }

        @Override
        public Rule<S, T> value() {
            return (Rule) Objects.requireNonNull(this.value, this);
        }

        public String get() {
            return "Unbound rule " + String.valueOf(this.name);
        }
    }
}
