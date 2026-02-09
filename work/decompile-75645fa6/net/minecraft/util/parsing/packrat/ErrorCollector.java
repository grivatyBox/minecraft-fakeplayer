package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.SystemUtils;

public interface ErrorCollector<S> {

    void store(int i, SuggestionSupplier<S> suggestionsupplier, Object object);

    default void store(int i, Object object) {
        this.store(i, SuggestionSupplier.empty(), object);
    }

    void finish(int i);

    public static class b<S> implements ErrorCollector<S> {

        public b() {}

        @Override
        public void store(int i, SuggestionSupplier<S> suggestionsupplier, Object object) {}

        @Override
        public void finish(int i) {}
    }

    public static class a<S> implements ErrorCollector<S> {

        private ErrorCollector.a.a<S>[] entries = new ErrorCollector.a.a[16];
        private int nextErrorEntry;
        private int lastCursor = -1;

        public a() {}

        private void discardErrorsFromShorterParse(int i) {
            if (i > this.lastCursor) {
                this.lastCursor = i;
                this.nextErrorEntry = 0;
            }

        }

        @Override
        public void finish(int i) {
            this.discardErrorsFromShorterParse(i);
        }

        @Override
        public void store(int i, SuggestionSupplier<S> suggestionsupplier, Object object) {
            this.discardErrorsFromShorterParse(i);
            if (i == this.lastCursor) {
                this.addErrorEntry(suggestionsupplier, object);
            }

        }

        private void addErrorEntry(SuggestionSupplier<S> suggestionsupplier, Object object) {
            int i = this.entries.length;

            if (this.nextErrorEntry >= i) {
                int j = SystemUtils.growByHalf(i, this.nextErrorEntry + 1);
                ErrorCollector.a.a<S>[] aerrorcollector_a_a = new ErrorCollector.a.a[j];

                System.arraycopy(this.entries, 0, aerrorcollector_a_a, 0, i);
                this.entries = aerrorcollector_a_a;
            }

            int k = this.nextErrorEntry++;
            ErrorCollector.a.a<S> errorcollector_a_a = this.entries[k];

            if (errorcollector_a_a == null) {
                errorcollector_a_a = new ErrorCollector.a.a<S>();
                this.entries[k] = errorcollector_a_a;
            }

            errorcollector_a_a.suggestions = suggestionsupplier;
            errorcollector_a_a.reason = object;
        }

        public List<ErrorEntry<S>> entries() {
            int i = this.nextErrorEntry;

            if (i == 0) {
                return List.of();
            } else {
                List<ErrorEntry<S>> list = new ArrayList(i);

                for (int j = 0; j < i; ++j) {
                    ErrorCollector.a.a<S> errorcollector_a_a = this.entries[j];

                    list.add(new ErrorEntry(this.lastCursor, errorcollector_a_a.suggestions, errorcollector_a_a.reason));
                }

                return list;
            }
        }

        public int cursor() {
            return this.lastCursor;
        }

        private static class a<S> {

            SuggestionSupplier<S> suggestions = SuggestionSupplier.<S>empty();
            Object reason = "empty";

            a() {}
        }
    }
}
