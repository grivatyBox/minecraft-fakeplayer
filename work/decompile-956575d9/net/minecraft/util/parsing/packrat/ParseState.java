package net.minecraft.util.parsing.packrat;

import java.util.Optional;
import javax.annotation.Nullable;

public interface ParseState<S> {

    Scope scope();

    ErrorCollector<S> errorCollector();

    default <T> Optional<T> parseTopRule(NamedRule<S, T> namedrule) {
        T t0 = (T) this.parse(namedrule);

        if (t0 != null) {
            this.errorCollector().finish(this.mark());
        }

        if (!this.scope().hasOnlySingleFrame()) {
            throw new IllegalStateException("Malformed scope: " + String.valueOf(this.scope()));
        } else {
            return Optional.ofNullable(t0);
        }
    }

    @Nullable
    <T> T parse(NamedRule<S, T> namedrule);

    S input();

    int mark();

    void restore(int i);

    Control acquireControl();

    void releaseControl();

    ParseState<S> silent();
}
