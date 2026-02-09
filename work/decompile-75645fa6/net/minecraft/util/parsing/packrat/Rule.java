package net.minecraft.util.parsing.packrat;

import javax.annotation.Nullable;

public interface Rule<S, T> {

    @Nullable
    T parse(ParseState<S> parsestate);

    static <S, T> Rule<S, T> fromTerm(Term<S> term, Rule.a<S, T> rule_a) {
        return new Rule.c<S, T>(rule_a, term);
    }

    static <S, T> Rule<S, T> fromTerm(Term<S> term, Rule.b<S, T> rule_b) {
        return new Rule.c<S, T>(rule_b, term);
    }

    @FunctionalInterface
    public interface b<S, T> extends Rule.a<S, T> {

        T run(Scope scope);

        @Override
        default T run(ParseState<S> parsestate) {
            return (T) this.run(parsestate.scope());
        }
    }

    public static record c<S, T>(Rule.a<S, T> action, Term<S> child) implements Rule<S, T> {

        @Nullable
        @Override
        public T parse(ParseState<S> parsestate) {
            Scope scope = parsestate.scope();

            scope.pushFrame();

            Object object;

            try {
                if (!this.child.parse(parsestate, scope, Control.UNBOUND)) {
                    object = null;
                    return (T) object;
                }

                object = this.action.run(parsestate);
            } finally {
                scope.popFrame();
            }

            return (T) object;
        }
    }

    @FunctionalInterface
    public interface a<S, T> {

        @Nullable
        T run(ParseState<S> parsestate);
    }
}
