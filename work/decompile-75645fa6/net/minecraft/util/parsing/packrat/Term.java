package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;

public interface Term<S> {

    boolean parse(ParseState<S> parsestate, Scope scope, Control control);

    static <S, T> Term<S> marker(Atom<T> atom, T t0) {
        return new Term.c(atom, t0);
    }

    @SafeVarargs
    static <S> Term<S> sequence(Term<S>... aterm) {
        return new Term.g<S>(aterm);
    }

    @SafeVarargs
    static <S> Term<S> alternative(Term<S>... aterm) {
        return new Term.a<S>(aterm);
    }

    static <S> Term<S> optional(Term<S> term) {
        return new Term.d<S>(term);
    }

    static <S, T> Term<S> repeated(NamedRule<S, T> namedrule, Atom<List<T>> atom) {
        return repeated(namedrule, atom, 0);
    }

    static <S, T> Term<S> repeated(NamedRule<S, T> namedrule, Atom<List<T>> atom, int i) {
        return new Term.e(namedrule, atom, i);
    }

    static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> namedrule, Atom<List<T>> atom, Term<S> term) {
        return repeatedWithTrailingSeparator(namedrule, atom, term, 0);
    }

    static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> namedrule, Atom<List<T>> atom, Term<S> term, int i) {
        return new Term.f(namedrule, atom, term, i, true);
    }

    static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> namedrule, Atom<List<T>> atom, Term<S> term) {
        return repeatedWithoutTrailingSeparator(namedrule, atom, term, 0);
    }

    static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> namedrule, Atom<List<T>> atom, Term<S> term, int i) {
        return new Term.f(namedrule, atom, term, i, false);
    }

    static <S> Term<S> positiveLookahead(Term<S> term) {
        return new Term.b<S>(term, true);
    }

    static <S> Term<S> negativeLookahead(Term<S> term) {
        return new Term.b<S>(term, false);
    }

    static <S> Term<S> cut() {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
                control.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    static <S> Term<S> empty() {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    static <S> Term<S> fail(final Object object) {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
                parsestate.errorCollector().store(parsestate.mark(), object);
                return false;
            }

            public String toString() {
                return "fail";
            }
        };
    }

    public static record c<S, T>(Atom<T> name, T value) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            scope.put(this.name, this.value);
            return true;
        }
    }

    public static record g<S>(Term<S>[] elements) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();

            for (Term<S> term : this.elements) {
                if (!term.parse(parsestate, scope, control)) {
                    parsestate.restore(i);
                    return false;
                }
            }

            return true;
        }
    }

    public static record a<S>(Term<S>[] elements) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            Control control1 = parsestate.acquireControl();

            try {
                int i = parsestate.mark();

                scope.splitFrame();

                for (Term<S> term : this.elements) {
                    if (term.parse(parsestate, scope, control1)) {
                        scope.mergeFrame();
                        boolean flag = true;

                        return flag;
                    }

                    scope.clearFrameValues();
                    parsestate.restore(i);
                    if (control1.hasCut()) {
                        break;
                    }
                }

                scope.popFrame();
                boolean flag1 = false;

                return flag1;
            } finally {
                parsestate.releaseControl();
            }
        }
    }

    public static record d<S>(Term<S> term) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();

            if (!this.term.parse(parsestate, scope, control)) {
                parsestate.restore(i);
            }

            return true;
        }
    }

    public static record e<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, int minRepetitions) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();
            List<T> list = new ArrayList(this.minRepetitions);

            while (true) {
                int j = parsestate.mark();
                T t0 = (T) parsestate.parse(this.element);

                if (t0 == null) {
                    parsestate.restore(j);
                    if (list.size() < this.minRepetitions) {
                        parsestate.restore(i);
                        return false;
                    } else {
                        scope.put(this.listName, list);
                        return true;
                    }
                }

                list.add(t0);
            }
        }
    }

    public static record f<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, Term<S> separator, int minRepetitions, boolean allowTrailingSeparator) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();
            List<T> list = new ArrayList(this.minRepetitions);
            boolean flag = true;

            while (true) {
                int j = parsestate.mark();

                if (!flag && !this.separator.parse(parsestate, scope, control)) {
                    parsestate.restore(j);
                    break;
                }

                int k = parsestate.mark();
                T t0 = (T) parsestate.parse(this.element);

                if (t0 == null) {
                    if (flag) {
                        parsestate.restore(k);
                    } else {
                        if (!this.allowTrailingSeparator) {
                            parsestate.restore(i);
                            return false;
                        }

                        parsestate.restore(k);
                    }
                    break;
                }

                list.add(t0);
                flag = false;
            }

            if (list.size() < this.minRepetitions) {
                parsestate.restore(i);
                return false;
            } else {
                scope.put(this.listName, list);
                return true;
            }
        }
    }

    public static record b<S>(Term<S> term, boolean positive) implements Term<S> {

        @Override
        public boolean parse(ParseState<S> parsestate, Scope scope, Control control) {
            int i = parsestate.mark();
            boolean flag = this.term.parse(parsestate.silent(), scope, control);

            parsestate.restore(i);
            return this.positive == flag;
        }
    }
}
