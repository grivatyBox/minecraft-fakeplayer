package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;

public class TestFinder implements TestInstanceFinder, TestPosFinder {

    static final TestInstanceFinder NO_FUNCTIONS = Stream::empty;
    static final TestPosFinder NO_STRUCTURES = Stream::empty;
    private final TestInstanceFinder testInstanceFinder;
    private final TestPosFinder testPosFinder;
    private final CommandListenerWrapper source;

    @Override
    public Stream<BlockPosition> findTestPos() {
        return this.testPosFinder.findTestPos();
    }

    public static TestFinder.a builder() {
        return new TestFinder.a();
    }

    TestFinder(CommandListenerWrapper commandlistenerwrapper, TestInstanceFinder testinstancefinder, TestPosFinder testposfinder) {
        this.source = commandlistenerwrapper;
        this.testInstanceFinder = testinstancefinder;
        this.testPosFinder = testposfinder;
    }

    public CommandListenerWrapper source() {
        return this.source;
    }

    @Override
    public Stream<Holder.c<GameTestInstance>> findTests() {
        return this.testInstanceFinder.findTests();
    }

    public static class a {

        private final UnaryOperator<Supplier<Stream<Holder.c<GameTestInstance>>>> testFinderWrapper;
        private final UnaryOperator<Supplier<Stream<BlockPosition>>> structureBlockPosFinderWrapper;

        public a() {
            this.testFinderWrapper = (supplier) -> {
                return supplier;
            };
            this.structureBlockPosFinderWrapper = (supplier) -> {
                return supplier;
            };
        }

        private a(UnaryOperator<Supplier<Stream<Holder.c<GameTestInstance>>>> unaryoperator, UnaryOperator<Supplier<Stream<BlockPosition>>> unaryoperator1) {
            this.testFinderWrapper = unaryoperator;
            this.structureBlockPosFinderWrapper = unaryoperator1;
        }

        public TestFinder.a createMultipleCopies(int i) {
            return new TestFinder.a(createCopies(i), createCopies(i));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int i) {
            return (supplier) -> {
                List<Q> list = new LinkedList();
                List<Q> list1 = ((Stream) supplier.get()).toList();

                for (int j = 0; j < i; ++j) {
                    list.addAll(list1);
                }

                Objects.requireNonNull(list);
                return list::stream;
            };
        }

        private TestFinder build(CommandListenerWrapper commandlistenerwrapper, TestInstanceFinder testinstancefinder, TestPosFinder testposfinder) {
            UnaryOperator unaryoperator = this.testFinderWrapper;

            Objects.requireNonNull(testinstancefinder);
            Supplier supplier = (Supplier) unaryoperator.apply(testinstancefinder::findTests);

            Objects.requireNonNull(supplier);
            TestInstanceFinder testinstancefinder1 = supplier::get;
            UnaryOperator unaryoperator1 = this.structureBlockPosFinderWrapper;

            Objects.requireNonNull(testposfinder);
            Supplier supplier1 = (Supplier) unaryoperator1.apply(testposfinder::findTestPos);

            Objects.requireNonNull(supplier1);
            return new TestFinder(commandlistenerwrapper, testinstancefinder1, supplier1::get);
        }

        public TestFinder radius(CommandContext<CommandListenerWrapper> commandcontext, int i) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findTestBlocks(blockposition, i, commandlistenerwrapper.getLevel());
            });
        }

        public TestFinder nearest(CommandContext<CommandListenerWrapper> commandcontext) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findNearestTest(blockposition, 15, commandlistenerwrapper.getLevel()).stream();
            });
        }

        public TestFinder allNearby(CommandContext<CommandListenerWrapper> commandcontext) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findTestBlocks(blockposition, 200, commandlistenerwrapper.getLevel());
            });
        }

        public TestFinder lookedAt(CommandContext<CommandListenerWrapper> commandcontext) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.lookedAtTestPos(BlockPosition.containing(commandlistenerwrapper.getPosition()), commandlistenerwrapper.getPlayer().getCamera(), commandlistenerwrapper.getLevel());
            });
        }

        public TestFinder failedTests(CommandContext<CommandListenerWrapper> commandcontext, boolean flag) {
            return this.build((CommandListenerWrapper) commandcontext.getSource(), () -> {
                return FailedTestTracker.getLastFailedTests().filter((holder_c) -> {
                    return !flag || ((GameTestInstance) holder_c.value()).required();
                });
            }, TestFinder.NO_STRUCTURES);
        }

        public TestFinder byResourceSelection(CommandContext<CommandListenerWrapper> commandcontext, Collection<Holder.c<GameTestInstance>> collection) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();

            Objects.requireNonNull(collection);
            return this.build(commandlistenerwrapper, collection::stream, TestFinder.NO_STRUCTURES);
        }

        public TestFinder failedTests(CommandContext<CommandListenerWrapper> commandcontext) {
            return this.failedTests(commandcontext, false);
        }
    }
}
