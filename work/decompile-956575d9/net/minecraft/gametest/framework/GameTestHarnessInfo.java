package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.phys.AxisAlignedBB;

public class GameTestHarnessInfo {

    private final Holder.c<GameTestInstance> test;
    @Nullable
    private BlockPosition testBlockPos;
    private final WorldServer level;
    private final Collection<GameTestHarnessListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestHarnessSequence> sequences = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap();
    private boolean placedStructure;
    private boolean chunksLoaded;
    private int tickCount;
    private boolean started;
    private final RetryOptions retryOptions;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done;
    private final EnumBlockRotation extraRotation;
    @Nullable
    private GameTestException error;
    @Nullable
    private TestInstanceBlockEntity testInstanceBlockEntity;

    public GameTestHarnessInfo(Holder.c<GameTestInstance> holder_c, EnumBlockRotation enumblockrotation, WorldServer worldserver, RetryOptions retryoptions) {
        this.test = holder_c;
        this.level = worldserver;
        this.retryOptions = retryoptions;
        this.timeoutTicks = ((GameTestInstance) holder_c.value()).maxTicks();
        this.extraRotation = enumblockrotation;
    }

    public void setTestBlockPos(@Nullable BlockPosition blockposition) {
        this.testBlockPos = blockposition;
    }

    public GameTestHarnessInfo startExecution(int i) {
        this.tickCount = -(((GameTestInstance) this.test.value()).setupTicks() + i + 1);
        return this;
    }

    public void placeStructure() {
        if (!this.placedStructure) {
            TestInstanceBlockEntity testinstanceblockentity = this.getTestInstanceBlockEntity();

            if (!testinstanceblockentity.placeStructure()) {
                this.fail((IChatBaseComponent) IChatBaseComponent.translatable("test.error.structure.failure", testinstanceblockentity.getTestName().getString()));
            }

            this.placedStructure = true;
            testinstanceblockentity.encaseStructure();
            StructureBoundingBox structureboundingbox = testinstanceblockentity.getStructureBoundingBox();

            this.level.getBlockTicks().clearArea(structureboundingbox);
            this.level.clearBlockEvents(structureboundingbox);
            this.listeners.forEach((gametestharnesslistener) -> {
                gametestharnesslistener.testStructureLoaded(this);
            });
        }
    }

    public void tick(GameTestHarnessRunner gametestharnessrunner) {
        if (!this.isDone()) {
            if (!this.placedStructure) {
                this.fail((IChatBaseComponent) IChatBaseComponent.translatable("test.error.ticking_without_structure"));
            }

            if (this.testInstanceBlockEntity == null) {
                this.fail((IChatBaseComponent) IChatBaseComponent.translatable("test.error.missing_block_entity"));
            }

            if (this.error != null) {
                this.finish();
            }

            if (!this.chunksLoaded) {
                Stream stream = this.testInstanceBlockEntity.getStructureBoundingBox().intersectingChunks();
                WorldServer worldserver = this.level;

                Objects.requireNonNull(this.level);
                if (!stream.allMatch(worldserver::areEntitiesActuallyLoadedAndTicking)) {
                    return;
                }
            }

            this.chunksLoaded = true;
            this.tickInternal();
            if (this.isDone()) {
                if (this.error != null) {
                    this.listeners.forEach((gametestharnesslistener) -> {
                        gametestharnesslistener.testFailed(this, gametestharnessrunner);
                    });
                } else {
                    this.listeners.forEach((gametestharnesslistener) -> {
                        gametestharnesslistener.testPassed(this, gametestharnessrunner);
                    });
                }
            }

        }
    }

    private void tickInternal() {
        ++this.tickCount;
        if (this.tickCount >= 0) {
            if (!this.started) {
                this.startTest();
            }

            ObjectIterator<Object2LongMap.Entry<Runnable>> objectiterator = this.runAtTickTimeMap.object2LongEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Object2LongMap.Entry<Runnable> object2longmap_entry = (Entry) objectiterator.next();

                if (object2longmap_entry.getLongValue() <= (long) this.tickCount) {
                    try {
                        ((Runnable) object2longmap_entry.getKey()).run();
                    } catch (GameTestException gametestexception) {
                        this.fail(gametestexception);
                    } catch (Exception exception) {
                        this.fail((GameTestException) (new UnknownGameTestException(exception)));
                    }

                    objectiterator.remove();
                }
            }

            if (this.tickCount > this.timeoutTicks) {
                if (this.sequences.isEmpty()) {
                    this.fail((GameTestException) (new GameTestHarnessTimeout(IChatBaseComponent.translatable("test.error.timeout.no_result", ((GameTestInstance) this.test.value()).maxTicks()))));
                } else {
                    this.sequences.forEach((gametestharnesssequence) -> {
                        gametestharnesssequence.tickAndFailIfNotComplete(this.tickCount);
                    });
                    if (this.error == null) {
                        this.fail((GameTestException) (new GameTestHarnessTimeout(IChatBaseComponent.translatable("test.error.timeout.no_sequences_finished", ((GameTestInstance) this.test.value()).maxTicks()))));
                    }
                }
            } else {
                this.sequences.forEach((gametestharnesssequence) -> {
                    gametestharnesssequence.tickAndContinue(this.tickCount);
                });
            }

        }
    }

    private void startTest() {
        if (!this.started) {
            this.started = true;
            this.getTestInstanceBlockEntity().setRunning();

            try {
                ((GameTestInstance) this.test.value()).run(new GameTestHarnessHelper(this));
            } catch (GameTestException gametestexception) {
                this.fail(gametestexception);
            } catch (Exception exception) {
                this.fail((GameTestException) (new UnknownGameTestException(exception)));
            }

        }
    }

    public void setRunAtTickTime(long i, Runnable runnable) {
        this.runAtTickTimeMap.put(runnable, i);
    }

    public MinecraftKey id() {
        return this.test.key().location();
    }

    @Nullable
    public BlockPosition getTestBlockPos() {
        return this.testBlockPos;
    }

    public BlockPosition getTestOrigin() {
        return this.testInstanceBlockEntity.getStartCorner();
    }

    public AxisAlignedBB getStructureBounds() {
        TestInstanceBlockEntity testinstanceblockentity = this.getTestInstanceBlockEntity();

        return testinstanceblockentity.getStructureBounds();
    }

    public TestInstanceBlockEntity getTestInstanceBlockEntity() {
        if (this.testInstanceBlockEntity == null) {
            if (this.testBlockPos == null) {
                throw new IllegalStateException("This GameTestInfo has no position");
            }

            TileEntity tileentity = this.level.getBlockEntity(this.testBlockPos);

            if (tileentity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity testinstanceblockentity = (TestInstanceBlockEntity) tileentity;

                this.testInstanceBlockEntity = testinstanceblockentity;
            }

            if (this.testInstanceBlockEntity == null) {
                throw new IllegalStateException("Could not find a test instance block entity at the given coordinate " + String.valueOf(this.testBlockPos));
            }
        }

        return this.testInstanceBlockEntity;
    }

    public WorldServer getLevel() {
        return this.level;
    }

    public boolean hasSucceeded() {
        return this.done && this.error == null;
    }

    public boolean hasFailed() {
        return this.error != null;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public boolean isDone() {
        return this.done;
    }

    public long getRunTime() {
        return this.timer.elapsed(TimeUnit.MILLISECONDS);
    }

    private void finish() {
        if (!this.done) {
            this.done = true;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }

    }

    public void succeed() {
        if (this.error == null) {
            this.finish();
            AxisAlignedBB axisalignedbb = this.getStructureBounds();
            List<Entity> list = this.getLevel().<Entity>getEntitiesOfClass(Entity.class, axisalignedbb.inflate(1.0D), (entity) -> {
                return !(entity instanceof EntityHuman);
            });

            list.forEach((entity) -> {
                entity.remove(Entity.RemovalReason.DISCARDED);
            });
        }

    }

    public void fail(IChatBaseComponent ichatbasecomponent) {
        this.fail((GameTestException) (new GameTestHarnessAssertion(ichatbasecomponent, this.tickCount)));
    }

    public void fail(GameTestException gametestexception) {
        this.error = gametestexception;
    }

    @Nullable
    public GameTestException getError() {
        return this.error;
    }

    public String toString() {
        return this.id().toString();
    }

    public void addListener(GameTestHarnessListener gametestharnesslistener) {
        this.listeners.add(gametestharnesslistener);
    }

    public GameTestHarnessInfo prepareTestStructure() {
        this.testInstanceBlockEntity = this.createTestInstanceBlock((BlockPosition) Objects.requireNonNull(this.testBlockPos), this.extraRotation, this.level);
        this.placeStructure();
        return this;
    }

    private TestInstanceBlockEntity createTestInstanceBlock(BlockPosition blockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        worldserver.setBlockAndUpdate(blockposition, Blocks.TEST_INSTANCE_BLOCK.defaultBlockState());
        TestInstanceBlockEntity testinstanceblockentity = (TestInstanceBlockEntity) Objects.requireNonNull((TestInstanceBlockEntity) worldserver.getBlockEntity(blockposition));
        ResourceKey<GameTestInstance> resourcekey = this.getTestHolder().key();
        BaseBlockPosition baseblockposition = (BaseBlockPosition) TestInstanceBlockEntity.getStructureSize(worldserver, resourcekey).orElse(new BaseBlockPosition(1, 1, 1));

        testinstanceblockentity.set(new TestInstanceBlockEntity.a(Optional.of(resourcekey), baseblockposition, enumblockrotation, false, TestInstanceBlockEntity.b.CLEARED, Optional.empty()));
        return testinstanceblockentity;
    }

    int getTick() {
        return this.tickCount;
    }

    GameTestHarnessSequence createSequence() {
        GameTestHarnessSequence gametestharnesssequence = new GameTestHarnessSequence(this);

        this.sequences.add(gametestharnesssequence);
        return gametestharnesssequence;
    }

    public boolean isRequired() {
        return ((GameTestInstance) this.test.value()).required();
    }

    public boolean isOptional() {
        return !((GameTestInstance) this.test.value()).required();
    }

    public MinecraftKey getStructure() {
        return ((GameTestInstance) this.test.value()).structure();
    }

    public EnumBlockRotation getRotation() {
        return ((GameTestInstance) this.test.value()).info().rotation().getRotated(this.extraRotation);
    }

    public GameTestInstance getTest() {
        return (GameTestInstance) this.test.value();
    }

    public Holder.c<GameTestInstance> getTestHolder() {
        return this.test;
    }

    public int getTimeoutTicks() {
        return this.timeoutTicks;
    }

    public boolean isFlaky() {
        return ((GameTestInstance) this.test.value()).maxAttempts() > 1;
    }

    public int maxAttempts() {
        return ((GameTestInstance) this.test.value()).maxAttempts();
    }

    public int requiredSuccesses() {
        return ((GameTestInstance) this.test.value()).requiredSuccesses();
    }

    public RetryOptions retryOptions() {
        return this.retryOptions;
    }

    public Stream<GameTestHarnessListener> getListeners() {
        return this.listeners.stream();
    }

    public GameTestHarnessInfo copyReset() {
        GameTestHarnessInfo gametestharnessinfo = new GameTestHarnessInfo(this.test, this.extraRotation, this.level, this.retryOptions());

        if (this.testBlockPos != null) {
            gametestharnessinfo.setTestBlockPos(this.testBlockPos);
        }

        return gametestharnessinfo;
    }
}
