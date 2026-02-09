package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import org.slf4j.Logger;

public class GameTestHarnessTicker {

    public static final GameTestHarnessTicker SINGLETON = new GameTestHarnessTicker();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Collection<GameTestHarnessInfo> testInfos = Lists.newCopyOnWriteArrayList();
    @Nullable
    private GameTestHarnessRunner runner;
    private GameTestHarnessTicker.a state;

    private GameTestHarnessTicker() {
        this.state = GameTestHarnessTicker.a.IDLE;
    }

    public void add(GameTestHarnessInfo gametestharnessinfo) {
        this.testInfos.add(gametestharnessinfo);
    }

    public void clear() {
        if (this.state != GameTestHarnessTicker.a.IDLE) {
            this.state = GameTestHarnessTicker.a.HALTING;
        } else {
            this.testInfos.clear();
            if (this.runner != null) {
                this.runner.stop();
                this.runner = null;
            }

        }
    }

    public void setRunner(GameTestHarnessRunner gametestharnessrunner) {
        if (this.runner != null) {
            SystemUtils.logAndPauseIfInIde("The runner was already set in GameTestTicker");
        }

        this.runner = gametestharnessrunner;
    }

    public void tick() {
        if (this.runner != null) {
            this.state = GameTestHarnessTicker.a.RUNNING;
            this.testInfos.forEach((gametestharnessinfo) -> {
                gametestharnessinfo.tick(this.runner);
            });
            this.testInfos.removeIf(GameTestHarnessInfo::isDone);
            GameTestHarnessTicker.a gametestharnessticker_a = this.state;

            this.state = GameTestHarnessTicker.a.IDLE;
            if (gametestharnessticker_a == GameTestHarnessTicker.a.HALTING) {
                this.clear();
            }

        }
    }

    private static enum a {

        IDLE, RUNNING, HALTING;

        private a() {}
    }
}
