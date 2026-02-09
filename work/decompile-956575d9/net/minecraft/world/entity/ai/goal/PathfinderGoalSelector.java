package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;

public class PathfinderGoalSelector {

    private static final PathfinderGoalWrapped NO_GOAL = new PathfinderGoalWrapped(Integer.MAX_VALUE, new PathfinderGoal() {
        @Override
        public boolean canUse() {
            return false;
        }
    }) {
        @Override
        public boolean isRunning() {
            return false;
        }
    };
    private final Map<PathfinderGoal.Type, PathfinderGoalWrapped> lockedFlags = new EnumMap(PathfinderGoal.Type.class);
    private final Set<PathfinderGoalWrapped> availableGoals = new ObjectLinkedOpenHashSet();
    private final EnumSet<PathfinderGoal.Type> disabledFlags = EnumSet.noneOf(PathfinderGoal.Type.class);

    public PathfinderGoalSelector() {}

    public void addGoal(int i, PathfinderGoal pathfindergoal) {
        this.availableGoals.add(new PathfinderGoalWrapped(i, pathfindergoal));
    }

    @VisibleForTesting
    public void removeAllGoals(Predicate<PathfinderGoal> predicate) {
        this.availableGoals.removeIf((pathfindergoalwrapped) -> {
            return predicate.test(pathfindergoalwrapped.getGoal());
        });
    }

    public void removeGoal(PathfinderGoal pathfindergoal) {
        for (PathfinderGoalWrapped pathfindergoalwrapped : this.availableGoals) {
            if (pathfindergoalwrapped.getGoal() == pathfindergoal && pathfindergoalwrapped.isRunning()) {
                pathfindergoalwrapped.stop();
            }
        }

        this.availableGoals.removeIf((pathfindergoalwrapped1) -> {
            return pathfindergoalwrapped1.getGoal() == pathfindergoal;
        });
    }

    private static boolean goalContainsAnyFlags(PathfinderGoalWrapped pathfindergoalwrapped, EnumSet<PathfinderGoal.Type> enumset) {
        for (PathfinderGoal.Type pathfindergoal_type : pathfindergoalwrapped.getFlags()) {
            if (enumset.contains(pathfindergoal_type)) {
                return true;
            }
        }

        return false;
    }

    private static boolean goalCanBeReplacedForAllFlags(PathfinderGoalWrapped pathfindergoalwrapped, Map<PathfinderGoal.Type, PathfinderGoalWrapped> map) {
        for (PathfinderGoal.Type pathfindergoal_type : pathfindergoalwrapped.getFlags()) {
            if (!((PathfinderGoalWrapped) map.getOrDefault(pathfindergoal_type, PathfinderGoalSelector.NO_GOAL)).canBeReplacedBy(pathfindergoalwrapped)) {
                return false;
            }
        }

        return true;
    }

    public void tick() {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("goalCleanup");

        for (PathfinderGoalWrapped pathfindergoalwrapped : this.availableGoals) {
            if (pathfindergoalwrapped.isRunning() && (goalContainsAnyFlags(pathfindergoalwrapped, this.disabledFlags) || !pathfindergoalwrapped.canContinueToUse())) {
                pathfindergoalwrapped.stop();
            }
        }

        this.lockedFlags.entrySet().removeIf((entry) -> {
            return !((PathfinderGoalWrapped) entry.getValue()).isRunning();
        });
        gameprofilerfiller.pop();
        gameprofilerfiller.push("goalUpdate");

        for (PathfinderGoalWrapped pathfindergoalwrapped1 : this.availableGoals) {
            if (!pathfindergoalwrapped1.isRunning() && !goalContainsAnyFlags(pathfindergoalwrapped1, this.disabledFlags) && goalCanBeReplacedForAllFlags(pathfindergoalwrapped1, this.lockedFlags) && pathfindergoalwrapped1.canUse()) {
                for (PathfinderGoal.Type pathfindergoal_type : pathfindergoalwrapped1.getFlags()) {
                    PathfinderGoalWrapped pathfindergoalwrapped2 = (PathfinderGoalWrapped) this.lockedFlags.getOrDefault(pathfindergoal_type, PathfinderGoalSelector.NO_GOAL);

                    pathfindergoalwrapped2.stop();
                    this.lockedFlags.put(pathfindergoal_type, pathfindergoalwrapped1);
                }

                pathfindergoalwrapped1.start();
            }
        }

        gameprofilerfiller.pop();
        this.tickRunningGoals(true);
    }

    public void tickRunningGoals(boolean flag) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("goalTick");

        for (PathfinderGoalWrapped pathfindergoalwrapped : this.availableGoals) {
            if (pathfindergoalwrapped.isRunning() && (flag || pathfindergoalwrapped.requiresUpdateEveryTick())) {
                pathfindergoalwrapped.tick();
            }
        }

        gameprofilerfiller.pop();
    }

    public Set<PathfinderGoalWrapped> getAvailableGoals() {
        return this.availableGoals;
    }

    public void disableControlFlag(PathfinderGoal.Type pathfindergoal_type) {
        this.disabledFlags.add(pathfindergoal_type);
    }

    public void enableControlFlag(PathfinderGoal.Type pathfindergoal_type) {
        this.disabledFlags.remove(pathfindergoal_type);
    }

    public void setControlFlag(PathfinderGoal.Type pathfindergoal_type, boolean flag) {
        if (flag) {
            this.enableControlFlag(pathfindergoal_type);
        } else {
            this.disableControlFlag(pathfindergoal_type);
        }

    }
}
