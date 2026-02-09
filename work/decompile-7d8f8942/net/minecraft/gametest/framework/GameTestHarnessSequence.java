package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.network.chat.IChatBaseComponent;

public class GameTestHarnessSequence {

    final GameTestHarnessInfo parent;
    private final List<GameTestHarnessEvent> events = Lists.newArrayList();
    private int lastTick;

    GameTestHarnessSequence(GameTestHarnessInfo gametestharnessinfo) {
        this.parent = gametestharnessinfo;
        this.lastTick = gametestharnessinfo.getTick();
    }

    public GameTestHarnessSequence thenWaitUntil(Runnable runnable) {
        this.events.add(GameTestHarnessEvent.create(runnable));
        return this;
    }

    public GameTestHarnessSequence thenWaitUntil(long i, Runnable runnable) {
        this.events.add(GameTestHarnessEvent.create(i, runnable));
        return this;
    }

    public GameTestHarnessSequence thenIdle(int i) {
        return this.thenExecuteAfter(i, () -> {
        });
    }

    public GameTestHarnessSequence thenExecute(Runnable runnable) {
        this.events.add(GameTestHarnessEvent.create(() -> {
            this.executeWithoutFail(runnable);
        }));
        return this;
    }

    public GameTestHarnessSequence thenExecuteAfter(int i, Runnable runnable) {
        this.events.add(GameTestHarnessEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + i) {
                throw new GameTestHarnessAssertion(IChatBaseComponent.translatable("test.error.sequence.not_completed"), this.parent.getTick());
            } else {
                this.executeWithoutFail(runnable);
            }
        }));
        return this;
    }

    public GameTestHarnessSequence thenExecuteFor(int i, Runnable runnable) {
        this.events.add(GameTestHarnessEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + i) {
                this.executeWithoutFail(runnable);
                throw new GameTestHarnessAssertion(IChatBaseComponent.translatable("test.error.sequence.not_completed"), this.parent.getTick());
            }
        }));
        return this;
    }

    public void thenSucceed() {
        List list = this.events;
        GameTestHarnessInfo gametestharnessinfo = this.parent;

        Objects.requireNonNull(this.parent);
        list.add(GameTestHarnessEvent.create(gametestharnessinfo::succeed));
    }

    public void thenFail(Supplier<GameTestException> supplier) {
        this.events.add(GameTestHarnessEvent.create(() -> {
            this.parent.fail((GameTestException) supplier.get());
        }));
    }

    public GameTestHarnessSequence.a thenTrigger() {
        GameTestHarnessSequence.a gametestharnesssequence_a = new GameTestHarnessSequence.a();

        this.events.add(GameTestHarnessEvent.create(() -> {
            gametestharnesssequence_a.trigger(this.parent.getTick());
        }));
        return gametestharnesssequence_a;
    }

    public void tickAndContinue(int i) {
        try {
            this.tick(i);
        } catch (GameTestHarnessAssertion gametestharnessassertion) {
            ;
        }

    }

    public void tickAndFailIfNotComplete(int i) {
        try {
            this.tick(i);
        } catch (GameTestHarnessAssertion gametestharnessassertion) {
            this.parent.fail((GameTestException) gametestharnessassertion);
        }

    }

    private void executeWithoutFail(Runnable runnable) {
        try {
            runnable.run();
        } catch (GameTestHarnessAssertion gametestharnessassertion) {
            this.parent.fail((GameTestException) gametestharnessassertion);
        }

    }

    private void tick(int i) {
        Iterator<GameTestHarnessEvent> iterator = this.events.iterator();

        while (iterator.hasNext()) {
            GameTestHarnessEvent gametestharnessevent = (GameTestHarnessEvent) iterator.next();

            gametestharnessevent.assertion.run();
            iterator.remove();
            int j = i - this.lastTick;
            int k = this.lastTick;

            this.lastTick = i;
            if (gametestharnessevent.expectedDelay != null && gametestharnessevent.expectedDelay != (long) j) {
                this.parent.fail((GameTestException) (new GameTestHarnessAssertion(IChatBaseComponent.translatable("test.error.sequence.invalid_tick", (long) k + gametestharnessevent.expectedDelay), i)));
                break;
            }
        }

    }

    public class a {

        private static final int NOT_TRIGGERED = -1;
        private int triggerTime = -1;

        public a() {}

        void trigger(int i) {
            if (this.triggerTime != -1) {
                throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
            } else {
                this.triggerTime = i;
            }
        }

        public void assertTriggeredThisTick() {
            int i = GameTestHarnessSequence.this.parent.getTick();

            if (this.triggerTime != i) {
                if (this.triggerTime == -1) {
                    throw new GameTestHarnessAssertion(IChatBaseComponent.translatable("test.error.sequence.condition_not_triggered"), i);
                } else {
                    throw new GameTestHarnessAssertion(IChatBaseComponent.translatable("test.error.sequence.condition_already_triggered", this.triggerTime), i);
                }
            }
        }
    }
}
