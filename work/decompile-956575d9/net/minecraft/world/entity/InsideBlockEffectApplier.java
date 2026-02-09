package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.SystemUtils;

public interface InsideBlockEffectApplier {

    InsideBlockEffectApplier NOOP = new InsideBlockEffectApplier() {
        @Override
        public void apply(InsideBlockEffectType insideblockeffecttype) {}

        @Override
        public void runBefore(InsideBlockEffectType insideblockeffecttype, Consumer<Entity> consumer) {}

        @Override
        public void runAfter(InsideBlockEffectType insideblockeffecttype, Consumer<Entity> consumer) {}
    };

    void apply(InsideBlockEffectType insideblockeffecttype);

    void runBefore(InsideBlockEffectType insideblockeffecttype, Consumer<Entity> consumer);

    void runAfter(InsideBlockEffectType insideblockeffecttype, Consumer<Entity> consumer);

    public static class a implements InsideBlockEffectApplier {

        private static final InsideBlockEffectType[] APPLY_ORDER = InsideBlockEffectType.values();
        private static final int NO_STEP = -1;
        private final Set<InsideBlockEffectType> effectsInStep = EnumSet.noneOf(InsideBlockEffectType.class);
        private final Map<InsideBlockEffectType, List<Consumer<Entity>>> beforeEffectsInStep = SystemUtils.<InsideBlockEffectType, List<Consumer<Entity>>>makeEnumMap(InsideBlockEffectType.class, (insideblockeffecttype) -> {
            return new ArrayList();
        });
        private final Map<InsideBlockEffectType, List<Consumer<Entity>>> afterEffectsInStep = SystemUtils.<InsideBlockEffectType, List<Consumer<Entity>>>makeEnumMap(InsideBlockEffectType.class, (insideblockeffecttype) -> {
            return new ArrayList();
        });
        private final List<Consumer<Entity>> finalEffects = new ArrayList();
        private int lastStep = -1;

        public a() {}

        public void advanceStep(int i) {
            if (this.lastStep != i) {
                this.lastStep = i;
                this.flushStep();
            }

        }

        public void applyAndClear(Entity entity) {
            this.flushStep();

            for (Consumer<Entity> consumer : this.finalEffects) {
                if (!entity.isAlive()) {
                    break;
                }

                consumer.accept(entity);
            }

            this.finalEffects.clear();
            this.lastStep = -1;
        }

        private void flushStep() {
            for (InsideBlockEffectType insideblockeffecttype : InsideBlockEffectApplier.a.APPLY_ORDER) {
                List<Consumer<Entity>> list = (List) this.beforeEffectsInStep.get(insideblockeffecttype);

                this.finalEffects.addAll(list);
                list.clear();
                if (this.effectsInStep.remove(insideblockeffecttype)) {
                    this.finalEffects.add(insideblockeffecttype.effect());
                }

                List<Consumer<Entity>> list1 = (List) this.afterEffectsInStep.get(insideblockeffecttype);

                this.finalEffects.addAll(list1);
                list1.clear();
            }

        }

        @Override
        public void apply(InsideBlockEffectType insideblockeffecttype) {
            this.effectsInStep.add(insideblockeffecttype);
        }

        @Override
        public void runBefore(InsideBlockEffectType insideblockeffecttype, Consumer<Entity> consumer) {
            ((List) this.beforeEffectsInStep.get(insideblockeffecttype)).add(consumer);
        }

        @Override
        public void runAfter(InsideBlockEffectType insideblockeffecttype, Consumer<Entity> consumer) {
            ((List) this.afterEffectsInStep.get(insideblockeffecttype)).add(consumer);
        }
    }
}
