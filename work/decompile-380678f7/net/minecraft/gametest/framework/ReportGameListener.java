package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestHarnessListener {

    private int attempts = 0;
    private int successes = 0;

    public ReportGameListener() {}

    @Override
    public void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo) {
        ++this.attempts;
    }

    private void handleRetry(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner, boolean flag) {
        RetryOptions retryoptions = gametestharnessinfo.retryOptions();
        String s = String.format("[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);

        if (!retryoptions.unlimitedTries()) {
            s = s + String.format(", Left: %4d", retryoptions.numberOfTries() - this.attempts);
        }

        s = s + "]";
        String s1 = String.valueOf(gametestharnessinfo.id());
        String s2 = s1 + " " + (flag ? "passed" : "failed") + "! " + gametestharnessinfo.getRunTime() + "ms";
        String s3 = String.format("%-53s%s", s, s2);

        if (flag) {
            reportPassed(gametestharnessinfo, s3);
        } else {
            say(gametestharnessinfo.getLevel(), EnumChatFormat.RED, s3);
        }

        if (retryoptions.hasTriesLeft(this.attempts, this.successes)) {
            gametestharnessrunner.rerunTest(gametestharnessinfo);
        }

    }

    @Override
    public void testPassed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
        ++this.successes;
        if (gametestharnessinfo.retryOptions().hasRetries()) {
            this.handleRetry(gametestharnessinfo, gametestharnessrunner, true);
        } else if (!gametestharnessinfo.isFlaky()) {
            String s = String.valueOf(gametestharnessinfo.id());

            reportPassed(gametestharnessinfo, s + " passed! (" + gametestharnessinfo.getRunTime() + "ms)");
        } else {
            if (this.successes >= gametestharnessinfo.requiredSuccesses()) {
                String s1 = String.valueOf(gametestharnessinfo);

                reportPassed(gametestharnessinfo, s1 + " passed " + this.successes + " times of " + this.attempts + " attempts.");
            } else {
                WorldServer worldserver = gametestharnessinfo.getLevel();
                EnumChatFormat enumchatformat = EnumChatFormat.GREEN;
                String s2 = String.valueOf(gametestharnessinfo);

                say(worldserver, enumchatformat, "Flaky test " + s2 + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
                gametestharnessrunner.rerunTest(gametestharnessinfo);
            }

        }
    }

    @Override
    public void testFailed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
        if (!gametestharnessinfo.isFlaky()) {
            reportFailure(gametestharnessinfo, gametestharnessinfo.getError());
            if (gametestharnessinfo.retryOptions().hasRetries()) {
                this.handleRetry(gametestharnessinfo, gametestharnessrunner, false);
            }

        } else {
            GameTestInstance gametestinstance = gametestharnessinfo.getTest();
            String s = String.valueOf(gametestharnessinfo);
            String s1 = "Flaky test " + s + " failed, attempt: " + this.attempts + "/" + gametestinstance.maxAttempts();

            if (gametestinstance.requiredSuccesses() > 1) {
                s1 = s1 + ", successes: " + this.successes + " (" + gametestinstance.requiredSuccesses() + " required)";
            }

            say(gametestharnessinfo.getLevel(), EnumChatFormat.YELLOW, s1);
            if (gametestharnessinfo.maxAttempts() - this.attempts + this.successes >= gametestharnessinfo.requiredSuccesses()) {
                gametestharnessrunner.rerunTest(gametestharnessinfo);
            } else {
                reportFailure(gametestharnessinfo, new ExhaustedAttemptsException(this.attempts, this.successes, gametestharnessinfo));
            }

        }
    }

    @Override
    public void testAddedForRerun(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessInfo gametestharnessinfo1, GameTestHarnessRunner gametestharnessrunner) {
        gametestharnessinfo1.addListener(this);
    }

    public static void reportPassed(GameTestHarnessInfo gametestharnessinfo, String s) {
        getTestInstanceBlockEntity(gametestharnessinfo).ifPresent((testinstanceblockentity) -> {
            testinstanceblockentity.setSuccess();
        });
        visualizePassedTest(gametestharnessinfo, s);
    }

    private static void visualizePassedTest(GameTestHarnessInfo gametestharnessinfo, String s) {
        say(gametestharnessinfo.getLevel(), EnumChatFormat.GREEN, s);
        GlobalTestReporter.onTestSuccess(gametestharnessinfo);
    }

    protected static void reportFailure(GameTestHarnessInfo gametestharnessinfo, Throwable throwable) {
        IChatBaseComponent ichatbasecomponent;

        if (throwable instanceof GameTestHarnessAssertion gametestharnessassertion) {
            ichatbasecomponent = gametestharnessassertion.getDescription();
        } else {
            ichatbasecomponent = IChatBaseComponent.literal(SystemUtils.describeError(throwable));
        }

        getTestInstanceBlockEntity(gametestharnessinfo).ifPresent((testinstanceblockentity) -> {
            testinstanceblockentity.setErrorMessage(ichatbasecomponent);
        });
        visualizeFailedTest(gametestharnessinfo, throwable);
    }

    protected static void visualizeFailedTest(GameTestHarnessInfo gametestharnessinfo, Throwable throwable) {
        String s = throwable.getMessage();
        String s1 = s + (throwable.getCause() == null ? "" : " cause: " + SystemUtils.describeError(throwable.getCause()));

        s = gametestharnessinfo.isRequired() ? "" : "(optional) ";
        String s2 = s + String.valueOf(gametestharnessinfo.id()) + " failed! " + s1;

        say(gametestharnessinfo.getLevel(), gametestharnessinfo.isRequired() ? EnumChatFormat.RED : EnumChatFormat.YELLOW, s2);
        Throwable throwable1 = (Throwable) MoreObjects.firstNonNull(ExceptionUtils.getRootCause(throwable), throwable);

        if (throwable1 instanceof GameTestHarnessAssertionPosition gametestharnessassertionposition) {
            showRedBox(gametestharnessinfo.getLevel(), gametestharnessassertionposition.getAbsolutePos(), gametestharnessassertionposition.getMessageToShowAtBlock());
        }

        GlobalTestReporter.onTestFailed(gametestharnessinfo);
    }

    private static Optional<TestInstanceBlockEntity> getTestInstanceBlockEntity(GameTestHarnessInfo gametestharnessinfo) {
        WorldServer worldserver = gametestharnessinfo.getLevel();
        Optional<BlockPosition> optional = Optional.ofNullable(gametestharnessinfo.getTestBlockPos());
        Optional<TestInstanceBlockEntity> optional1 = optional.flatMap((blockposition) -> {
            return worldserver.getBlockEntity(blockposition, TileEntityTypes.TEST_INSTANCE_BLOCK);
        });

        return optional1;
    }

    protected static void say(WorldServer worldserver, EnumChatFormat enumchatformat, String s) {
        worldserver.getPlayers((entityplayer) -> {
            return true;
        }).forEach((entityplayer) -> {
            entityplayer.sendSystemMessage(IChatBaseComponent.literal(s).withStyle(enumchatformat));
        });
    }

    private static void showRedBox(WorldServer worldserver, BlockPosition blockposition, String s) {
        PacketDebug.sendGameTestAddMarker(worldserver, blockposition, s, -2130771968, Integer.MAX_VALUE);
    }
}
