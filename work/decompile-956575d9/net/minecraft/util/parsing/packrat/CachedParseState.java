package net.minecraft.util.parsing.packrat;

import javax.annotation.Nullable;
import net.minecraft.SystemUtils;

public abstract class CachedParseState<S> implements ParseState<S> {

    private CachedParseState.b[] positionCache = new CachedParseState.b[256];
    private final ErrorCollector<S> errorCollector;
    private final Scope scope = new Scope();
    private CachedParseState.d[] controlCache = new CachedParseState.d[16];
    private int nextControlToReturn;
    private final CachedParseState<S>.c silent = new CachedParseState.c();

    protected CachedParseState(ErrorCollector<S> errorcollector) {
        this.errorCollector = errorcollector;
    }

    @Override
    public Scope scope() {
        return this.scope;
    }

    @Override
    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    @Nullable
    @Override
    public <T> T parse(NamedRule<S, T> namedrule) {
        int i = this.mark();
        CachedParseState.b cachedparsestate_b = this.getCacheForPosition(i);
        int j = cachedparsestate_b.findKeyIndex(namedrule.name());

        if (j != -1) {
            CachedParseState.a<T> cachedparsestate_a = cachedparsestate_b.<T>getValue(j);

            if (cachedparsestate_a != null) {
                if (cachedparsestate_a == CachedParseState.a.NEGATIVE) {
                    return null;
                }

                this.restore(cachedparsestate_a.markAfterParse);
                return cachedparsestate_a.value;
            }
        } else {
            j = cachedparsestate_b.allocateNewEntry(namedrule.name());
        }

        T t0 = namedrule.value().parse(this);
        CachedParseState.a<T> cachedparsestate_a1;

        if (t0 == null) {
            cachedparsestate_a1 = CachedParseState.a.<T>negativeEntry();
        } else {
            int k = this.mark();

            cachedparsestate_a1 = new CachedParseState.a<T>(t0, k);
        }

        cachedparsestate_b.setValue(j, cachedparsestate_a1);
        return t0;
    }

    private CachedParseState.b getCacheForPosition(int i) {
        int j = this.positionCache.length;

        if (i >= j) {
            int k = SystemUtils.growByHalf(j, i + 1);
            CachedParseState.b[] acachedparsestate_b = new CachedParseState.b[k];

            System.arraycopy(this.positionCache, 0, acachedparsestate_b, 0, j);
            this.positionCache = acachedparsestate_b;
        }

        CachedParseState.b cachedparsestate_b = this.positionCache[i];

        if (cachedparsestate_b == null) {
            cachedparsestate_b = new CachedParseState.b();
            this.positionCache[i] = cachedparsestate_b;
        }

        return cachedparsestate_b;
    }

    @Override
    public Control acquireControl() {
        int i = this.controlCache.length;

        if (this.nextControlToReturn >= i) {
            int j = SystemUtils.growByHalf(i, this.nextControlToReturn + 1);
            CachedParseState.d[] acachedparsestate_d = new CachedParseState.d[j];

            System.arraycopy(this.controlCache, 0, acachedparsestate_d, 0, i);
            this.controlCache = acachedparsestate_d;
        }

        int k = this.nextControlToReturn++;
        CachedParseState.d cachedparsestate_d = this.controlCache[k];

        if (cachedparsestate_d == null) {
            cachedparsestate_d = new CachedParseState.d();
            this.controlCache[k] = cachedparsestate_d;
        } else {
            cachedparsestate_d.reset();
        }

        return cachedparsestate_d;
    }

    @Override
    public void releaseControl() {
        --this.nextControlToReturn;
    }

    @Override
    public ParseState<S> silent() {
        return this.silent;
    }

    private static class b {

        public static final int ENTRY_STRIDE = 2;
        private static final int NOT_FOUND = -1;
        private Object[] atomCache = new Object[16];
        private int nextKey;

        b() {}

        public int findKeyIndex(Atom<?> atom) {
            for (int i = 0; i < this.nextKey; i += 2) {
                if (this.atomCache[i] == atom) {
                    return i;
                }
            }

            return -1;
        }

        public int allocateNewEntry(Atom<?> atom) {
            int i = this.nextKey;

            this.nextKey += 2;
            int j = i + 1;
            int k = this.atomCache.length;

            if (j >= k) {
                int l = SystemUtils.growByHalf(k, j + 1);
                Object[] aobject = new Object[l];

                System.arraycopy(this.atomCache, 0, aobject, 0, k);
                this.atomCache = aobject;
            }

            this.atomCache[i] = atom;
            return i;
        }

        @Nullable
        public <T> CachedParseState.a<T> getValue(int i) {
            return (CachedParseState.a) this.atomCache[i + 1];
        }

        public void setValue(int i, CachedParseState.a<?> cachedparsestate_a) {
            this.atomCache[i + 1] = cachedparsestate_a;
        }
    }

    private static record a<T>(@Nullable T value, int markAfterParse) {

        public static final CachedParseState.a<?> NEGATIVE = new CachedParseState.a((Object) null, -1);

        public static <T> CachedParseState.a<T> negativeEntry() {
            return CachedParseState.a.NEGATIVE;
        }
    }

    private class c implements ParseState<S> {

        private final ErrorCollector<S> silentCollector = new ErrorCollector.b<S>();

        c() {}

        @Override
        public ErrorCollector<S> errorCollector() {
            return this.silentCollector;
        }

        @Override
        public Scope scope() {
            return CachedParseState.this.scope();
        }

        @Nullable
        @Override
        public <T> T parse(NamedRule<S, T> namedrule) {
            return (T) CachedParseState.this.parse(namedrule);
        }

        @Override
        public S input() {
            return (S) CachedParseState.this.input();
        }

        @Override
        public int mark() {
            return CachedParseState.this.mark();
        }

        @Override
        public void restore(int i) {
            CachedParseState.this.restore(i);
        }

        @Override
        public Control acquireControl() {
            return CachedParseState.this.acquireControl();
        }

        @Override
        public void releaseControl() {
            CachedParseState.this.releaseControl();
        }

        @Override
        public ParseState<S> silent() {
            return this;
        }
    }

    private static class d implements Control {

        private boolean hasCut;

        d() {}

        @Override
        public void cut() {
            this.hasCut = true;
        }

        @Override
        public boolean hasCut() {
            return this.hasCut;
        }

        public void reset() {
            this.hasCut = false;
        }
    }
}
