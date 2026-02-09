package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.EnumChatFormat;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatClickable;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestHarnessTestCommand {

    public static final int TEST_NEARBY_SEARCH_RADIUS = 15;
    public static final int TEST_FULL_SEARCH_RADIUS = 200;
    public static final int VERIFY_TEST_GRID_AXIS_SIZE = 10;
    public static final int VERIFY_TEST_BATCH_SIZE = 100;
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final SimpleCommandExceptionType CLEAR_NO_TESTS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.test.clear.error.no_tests"));
    private static final SimpleCommandExceptionType RESET_NO_TESTS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.test.reset.error.no_tests"));
    private static final SimpleCommandExceptionType TEST_INSTANCE_COULD_NOT_BE_FOUND = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.test.error.test_instance_not_found"));
    private static final SimpleCommandExceptionType NO_STRUCTURES_TO_EXPORT = new SimpleCommandExceptionType(IChatBaseComponent.literal("Could not find any structures to export"));
    private static final SimpleCommandExceptionType NO_TEST_INSTANCES = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.test.error.no_test_instances"));
    private static final Dynamic3CommandExceptionType NO_TEST_CONTAINING = new Dynamic3CommandExceptionType((object, object1, object2) -> {
        return IChatBaseComponent.translatableEscape("commands.test.error.no_test_containing_pos", object, object1, object2);
    });
    private static final DynamicCommandExceptionType TOO_LARGE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.test.error.too_large", object);
    });

    public GameTestHarnessTestCommand() {}

    private static int reset(TestFinder testfinder) throws CommandSyntaxException {
        stopTests();
        int i = toGameTestInfos(testfinder.source(), RetryOptions.noRetries(), testfinder).map((gametestharnessinfo) -> {
            return resetGameTestInfo(testfinder.source(), gametestharnessinfo);
        }).toList().size();

        if (i == 0) {
            throw GameTestHarnessTestCommand.CLEAR_NO_TESTS.create();
        } else {
            testfinder.source().sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.reset.success", i);
            }, true);
            return i;
        }
    }

    private static int clear(TestFinder testfinder) throws CommandSyntaxException {
        stopTests();
        CommandListenerWrapper commandlistenerwrapper = testfinder.source();
        WorldServer worldserver = commandlistenerwrapper.getLevel();

        GameTestHarnessRunner.clearMarkers(worldserver);
        List<StructureBoundingBox> list = testfinder.findTestPos().flatMap((blockposition) -> {
            return worldserver.getBlockEntity(blockposition, TileEntityTypes.TEST_INSTANCE_BLOCK).stream();
        }).map(TestInstanceBlockEntity::getStructureBoundingBox).toList();

        list.forEach((structureboundingbox) -> {
            GameTestHarnessStructures.clearSpaceForStructure(structureboundingbox, worldserver);
        });
        if (list.isEmpty()) {
            throw GameTestHarnessTestCommand.CLEAR_NO_TESTS.create();
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.clear.success", list.size());
            }, true);
            return list.size();
        }
    }

    private static int export(TestFinder testfinder) throws CommandSyntaxException {
        CommandListenerWrapper commandlistenerwrapper = testfinder.source();
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        int i = 0;
        boolean flag = true;

        for (Iterator<BlockPosition> iterator = testfinder.findTestPos().iterator(); iterator.hasNext(); ++i) {
            BlockPosition blockposition = (BlockPosition) iterator.next();
            TileEntity tileentity = worldserver.getBlockEntity(blockposition);

            if (!(tileentity instanceof TestInstanceBlockEntity)) {
                throw GameTestHarnessTestCommand.TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
            }

            TestInstanceBlockEntity testinstanceblockentity = (TestInstanceBlockEntity) tileentity;

            Objects.requireNonNull(commandlistenerwrapper);
            if (!testinstanceblockentity.exportTest(commandlistenerwrapper::sendSystemMessage)) {
                flag = false;
            }
        }

        if (i == 0) {
            throw GameTestHarnessTestCommand.NO_STRUCTURES_TO_EXPORT.create();
        } else {
            String s = "Exported " + i + " structures";

            testfinder.source().sendSuccess(() -> {
                return IChatBaseComponent.literal(s);
            }, true);
            return flag ? 0 : 1;
        }
    }

    private static int verify(TestFinder testfinder) {
        stopTests();
        CommandListenerWrapper commandlistenerwrapper = testfinder.source();
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        BlockPosition blockposition = createTestPositionAround(commandlistenerwrapper);
        Collection<GameTestHarnessInfo> collection = Stream.concat(toGameTestInfos(commandlistenerwrapper, RetryOptions.noRetries(), testfinder), toGameTestInfo(commandlistenerwrapper, RetryOptions.noRetries(), testfinder, 0)).toList();

        GameTestHarnessRunner.clearMarkers(worldserver);
        FailedTestTracker.forgetFailedTests();
        Collection<GameTestHarnessBatch> collection1 = new ArrayList();

        for (GameTestHarnessInfo gametestharnessinfo : collection) {
            for (EnumBlockRotation enumblockrotation : EnumBlockRotation.values()) {
                Collection<GameTestHarnessInfo> collection2 = new ArrayList();

                for (int i = 0; i < 100; ++i) {
                    GameTestHarnessInfo gametestharnessinfo1 = new GameTestHarnessInfo(gametestharnessinfo.getTestHolder(), enumblockrotation, worldserver, new RetryOptions(1, true));

                    gametestharnessinfo1.setTestBlockPos(gametestharnessinfo.getTestBlockPos());
                    collection2.add(gametestharnessinfo1);
                }

                GameTestHarnessBatch gametestharnessbatch = GameTestBatchFactory.toGameTestBatch(collection2, gametestharnessinfo.getTest().batch(), enumblockrotation.ordinal());

                collection1.add(gametestharnessbatch);
            }
        }

        StructureGridSpawner structuregridspawner = new StructureGridSpawner(blockposition, 10, true);
        GameTestHarnessRunner gametestharnessrunner = GameTestHarnessRunner.a.fromBatches(collection1, worldserver).batcher(GameTestBatchFactory.fromGameTestInfo(100)).newStructureSpawner(structuregridspawner).existingStructureSpawner(structuregridspawner).haltOnError(true).build();

        return trackAndStartRunner(commandlistenerwrapper, gametestharnessrunner);
    }

    private static int run(TestFinder testfinder, RetryOptions retryoptions, int i, int j) {
        stopTests();
        CommandListenerWrapper commandlistenerwrapper = testfinder.source();
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        BlockPosition blockposition = createTestPositionAround(commandlistenerwrapper);
        Collection<GameTestHarnessInfo> collection = Stream.concat(toGameTestInfos(commandlistenerwrapper, retryoptions, testfinder), toGameTestInfo(commandlistenerwrapper, retryoptions, testfinder, i)).toList();

        if (collection.isEmpty()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.no_tests");
            }, false);
            return 0;
        } else {
            GameTestHarnessRunner.clearMarkers(worldserver);
            FailedTestTracker.forgetFailedTests();
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.run.running", collection.size());
            }, false);
            GameTestHarnessRunner gametestharnessrunner = GameTestHarnessRunner.a.fromInfo(collection, worldserver).newStructureSpawner(new StructureGridSpawner(blockposition, j, false)).build();

            return trackAndStartRunner(commandlistenerwrapper, gametestharnessrunner);
        }
    }

    private static int locate(TestFinder testfinder) throws CommandSyntaxException {
        testfinder.source().sendSystemMessage(IChatBaseComponent.translatable("commands.test.locate.started"));
        MutableInt mutableint = new MutableInt(0);
        BlockPosition blockposition = BlockPosition.containing(testfinder.source().getPosition());

        testfinder.findTestPos().forEach((blockposition1) -> {
            TileEntity tileentity = testfinder.source().getLevel().getBlockEntity(blockposition1);

            if (tileentity instanceof TestInstanceBlockEntity testinstanceblockentity) {
                EnumDirection enumdirection = testinstanceblockentity.getRotation().rotate(EnumDirection.NORTH);
                BlockPosition blockposition2 = testinstanceblockentity.getBlockPos().relative(enumdirection, 2);
                int i = (int) enumdirection.getOpposite().toYRot();
                String s = String.format(Locale.ROOT, "/tp @s %d %d %d %d 0", blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), i);
                int j = blockposition.getX() - blockposition1.getX();
                int k = blockposition.getZ() - blockposition1.getZ();
                int l = MathHelper.floor(MathHelper.sqrt((float) (j * j + k * k)));
                IChatMutableComponent ichatmutablecomponent = ChatComponentUtils.wrapInSquareBrackets(IChatBaseComponent.translatable("chat.coordinates", blockposition1.getX(), blockposition1.getY(), blockposition1.getZ())).withStyle((chatmodifier) -> {
                    return chatmodifier.withColor(EnumChatFormat.GREEN).withClickEvent(new ChatClickable.SuggestCommand(s)).withHoverEvent(new ChatHoverable.e(IChatBaseComponent.translatable("chat.coordinates.tooltip")));
                });

                testfinder.source().sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.test.locate.found", ichatmutablecomponent, l);
                }, false);
                mutableint.increment();
            }
        });
        int i = mutableint.intValue();

        if (i == 0) {
            throw GameTestHarnessTestCommand.NO_TEST_INSTANCES.create();
        } else {
            testfinder.source().sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.locate.done", i);
            }, true);
            return i;
        }
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> runWithRetryOptions(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, InCommandFunction<CommandContext<CommandListenerWrapper>, TestFinder> incommandfunction, Function<ArgumentBuilder<CommandListenerWrapper, ?>, ArgumentBuilder<CommandListenerWrapper, ?>> function) {
        return argumentbuilder.executes((commandcontext) -> {
            return run(incommandfunction.apply(commandcontext), RetryOptions.noRetries(), 0, 8);
        }).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("numberOfTimes", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return run(incommandfunction.apply(commandcontext), new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), false), 0, 8);
        })).then((ArgumentBuilder) function.apply(net.minecraft.commands.CommandDispatcher.argument("untilFailed", BoolArgumentType.bool()).executes((commandcontext) -> {
            return run(incommandfunction.apply(commandcontext), new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), BoolArgumentType.getBool(commandcontext, "untilFailed")), 0, 8);
        }))));
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> runWithRetryOptions(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, InCommandFunction<CommandContext<CommandListenerWrapper>, TestFinder> incommandfunction) {
        return runWithRetryOptions(argumentbuilder, incommandfunction, (argumentbuilder1) -> {
            return argumentbuilder1;
        });
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> runWithRetryOptionsAndBuildInfo(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, InCommandFunction<CommandContext<CommandListenerWrapper>, TestFinder> incommandfunction) {
        return runWithRetryOptions(argumentbuilder, incommandfunction, (argumentbuilder1) -> {
            return argumentbuilder1.then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("rotationSteps", IntegerArgumentType.integer()).executes((commandcontext) -> {
                return run(incommandfunction.apply(commandcontext), new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), BoolArgumentType.getBool(commandcontext, "untilFailed")), IntegerArgumentType.getInteger(commandcontext, "rotationSteps"), 8);
            })).then(net.minecraft.commands.CommandDispatcher.argument("testsPerRow", IntegerArgumentType.integer()).executes((commandcontext) -> {
                return run(incommandfunction.apply(commandcontext), new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), BoolArgumentType.getBool(commandcontext, "untilFailed")), IntegerArgumentType.getInteger(commandcontext, "rotationSteps"), IntegerArgumentType.getInteger(commandcontext, "testsPerRow"));
            })));
        });
    }

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder = runWithRetryOptionsAndBuildInfo(net.minecraft.commands.CommandDispatcher.argument("onlyRequiredTests", BoolArgumentType.bool()), (commandcontext) -> {
            return TestFinder.builder().failedTests(commandcontext, BoolArgumentType.getBool(commandcontext, "onlyRequiredTests"));
        });
        LiteralArgumentBuilder literalargumentbuilder = (LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("test").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("run").then(runWithRetryOptionsAndBuildInfo(net.minecraft.commands.CommandDispatcher.argument("tests", ResourceSelectorArgument.resourceSelector(commandbuildcontext, Registries.TEST_INSTANCE)), (commandcontext) -> {
            return TestFinder.builder().byResourceSelection(commandcontext, ResourceSelectorArgument.getSelectedResources(commandcontext, "tests", Registries.TEST_INSTANCE));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("runmultiple").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("tests", ResourceSelectorArgument.resourceSelector(commandbuildcontext, Registries.TEST_INSTANCE)).executes((commandcontext) -> {
            return run(TestFinder.builder().byResourceSelection(commandcontext, ResourceSelectorArgument.getSelectedResources(commandcontext, "tests", Registries.TEST_INSTANCE)), RetryOptions.noRetries(), 0, 8);
        })).then(net.minecraft.commands.CommandDispatcher.argument("amount", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return run(TestFinder.builder().createMultipleCopies(IntegerArgumentType.getInteger(commandcontext, "amount")).byResourceSelection(commandcontext, ResourceSelectorArgument.getSelectedResources(commandcontext, "tests", Registries.TEST_INSTANCE)), RetryOptions.noRetries(), 0, 8);
        }))));
        LiteralArgumentBuilder literalargumentbuilder1 = net.minecraft.commands.CommandDispatcher.literal("runthese");
        TestFinder.a testfinder_a = TestFinder.builder();

        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptions(literalargumentbuilder1, testfinder_a::allNearby));
        literalargumentbuilder1 = net.minecraft.commands.CommandDispatcher.literal("runclosest");
        testfinder_a = TestFinder.builder();
        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptions(literalargumentbuilder1, testfinder_a::nearest));
        literalargumentbuilder1 = net.minecraft.commands.CommandDispatcher.literal("runthat");
        testfinder_a = TestFinder.builder();
        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptions(literalargumentbuilder1, testfinder_a::lookedAt));
        ArgumentBuilder argumentbuilder1 = net.minecraft.commands.CommandDispatcher.literal("runfailed").then(argumentbuilder);

        testfinder_a = TestFinder.builder();
        Objects.requireNonNull(testfinder_a);
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder2 = (LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptionsAndBuildInfo(argumentbuilder1, testfinder_a::failedTests))).then(net.minecraft.commands.CommandDispatcher.literal("verify").then(net.minecraft.commands.CommandDispatcher.argument("tests", ResourceSelectorArgument.resourceSelector(commandbuildcontext, Registries.TEST_INSTANCE)).executes((commandcontext) -> {
            return verify(TestFinder.builder().byResourceSelection(commandcontext, ResourceSelectorArgument.getSelectedResources(commandcontext, "tests", Registries.TEST_INSTANCE)));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("locate").then(net.minecraft.commands.CommandDispatcher.argument("tests", ResourceSelectorArgument.resourceSelector(commandbuildcontext, Registries.TEST_INSTANCE)).executes((commandcontext) -> {
            return locate(TestFinder.builder().byResourceSelection(commandcontext, ResourceSelectorArgument.getSelectedResources(commandcontext, "tests", Registries.TEST_INSTANCE)));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("resetclosest").executes((commandcontext) -> {
            return reset(TestFinder.builder().nearest(commandcontext));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("resetthese").executes((commandcontext) -> {
            return reset(TestFinder.builder().allNearby(commandcontext));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("resetthat").executes((commandcontext) -> {
            return reset(TestFinder.builder().lookedAt(commandcontext));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("clearthat").executes((commandcontext) -> {
            return clear(TestFinder.builder().lookedAt(commandcontext));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("clearthese").executes((commandcontext) -> {
            return clear(TestFinder.builder().allNearby(commandcontext));
        }))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("clearall").executes((commandcontext) -> {
            return clear(TestFinder.builder().radius(commandcontext, 200));
        })).then(net.minecraft.commands.CommandDispatcher.argument("radius", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return clear(TestFinder.builder().radius(commandcontext, MathHelper.clamp(IntegerArgumentType.getInteger(commandcontext, "radius"), 0, 1024)));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("stop").executes((commandcontext) -> {
            return stopTests();
        }))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("pos").executes((commandcontext) -> {
            return showPos((CommandListenerWrapper) commandcontext.getSource(), "pos");
        })).then(net.minecraft.commands.CommandDispatcher.argument("var", StringArgumentType.word()).executes((commandcontext) -> {
            return showPos((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "var"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("create").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("id", ArgumentMinecraftKeyRegistered.id()).suggests(GameTestHarnessTestCommand::suggestTestFunction).executes((commandcontext) -> {
            return createNewStructure((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "id"), 5, 5, 5);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("width", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return createNewStructure((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "id"), IntegerArgumentType.getInteger(commandcontext, "width"), IntegerArgumentType.getInteger(commandcontext, "width"), IntegerArgumentType.getInteger(commandcontext, "width"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("height", IntegerArgumentType.integer()).then(net.minecraft.commands.CommandDispatcher.argument("depth", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return createNewStructure((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "id"), IntegerArgumentType.getInteger(commandcontext, "width"), IntegerArgumentType.getInteger(commandcontext, "height"), IntegerArgumentType.getInteger(commandcontext, "depth"));
        }))))));

        if (SharedConstants.IS_RUNNING_IN_IDE) {
            literalargumentbuilder2 = (LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) literalargumentbuilder2.then(net.minecraft.commands.CommandDispatcher.literal("export").then(net.minecraft.commands.CommandDispatcher.argument("test", ResourceArgument.resource(commandbuildcontext, Registries.TEST_INSTANCE)).executes((commandcontext) -> {
                return exportTestStructure((CommandListenerWrapper) commandcontext.getSource(), ResourceArgument.getResource(commandcontext, "test", Registries.TEST_INSTANCE));
            })))).then(net.minecraft.commands.CommandDispatcher.literal("exportclosest").executes((commandcontext) -> {
                return export(TestFinder.builder().nearest(commandcontext));
            }))).then(net.minecraft.commands.CommandDispatcher.literal("exportthese").executes((commandcontext) -> {
                return export(TestFinder.builder().allNearby(commandcontext));
            }))).then(net.minecraft.commands.CommandDispatcher.literal("exportthat").executes((commandcontext) -> {
                return export(TestFinder.builder().lookedAt(commandcontext));
            }));
        }

        commanddispatcher.register(literalargumentbuilder2);
    }

    public static CompletableFuture<Suggestions> suggestTestFunction(CommandContext<CommandListenerWrapper> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        Stream<String> stream = ((CommandListenerWrapper) commandcontext.getSource()).registryAccess().lookupOrThrow(Registries.TEST_FUNCTION).listElements().map(Holder::getRegisteredName);

        return ICompletionProvider.suggest(stream, suggestionsbuilder);
    }

    private static int resetGameTestInfo(CommandListenerWrapper commandlistenerwrapper, GameTestHarnessInfo gametestharnessinfo) {
        TestInstanceBlockEntity testinstanceblockentity = gametestharnessinfo.getTestInstanceBlockEntity();

        Objects.requireNonNull(commandlistenerwrapper);
        testinstanceblockentity.resetTest(commandlistenerwrapper::sendSystemMessage);
        return 1;
    }

    private static Stream<GameTestHarnessInfo> toGameTestInfos(CommandListenerWrapper commandlistenerwrapper, RetryOptions retryoptions, TestPosFinder testposfinder) {
        return testposfinder.findTestPos().map((blockposition) -> {
            return createGameTestInfo(blockposition, commandlistenerwrapper, retryoptions);
        }).flatMap(Optional::stream);
    }

    private static Stream<GameTestHarnessInfo> toGameTestInfo(CommandListenerWrapper commandlistenerwrapper, RetryOptions retryoptions, TestInstanceFinder testinstancefinder, int i) {
        return testinstancefinder.findTests().filter((holder_c) -> {
            return verifyStructureExists(commandlistenerwrapper, ((GameTestInstance) holder_c.value()).structure());
        }).map((holder_c) -> {
            return new GameTestHarnessInfo(holder_c, GameTestHarnessStructures.getRotationForRotationSteps(i), commandlistenerwrapper.getLevel(), retryoptions);
        });
    }

    private static Optional<GameTestHarnessInfo> createGameTestInfo(BlockPosition blockposition, CommandListenerWrapper commandlistenerwrapper, RetryOptions retryoptions) {
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof TestInstanceBlockEntity testinstanceblockentity) {
            Optional optional = testinstanceblockentity.test();
            IRegistry iregistry = commandlistenerwrapper.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);

            Objects.requireNonNull(iregistry);
            Optional<Holder.c<GameTestInstance>> optional1 = optional.flatMap(iregistry::get);

            if (optional1.isEmpty()) {
                commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.test.error.non_existant_test", testinstanceblockentity.getTestName()));
                return Optional.empty();
            } else {
                Holder.c<GameTestInstance> holder_c = (Holder.c) optional1.get();
                GameTestHarnessInfo gametestharnessinfo = new GameTestHarnessInfo(holder_c, testinstanceblockentity.getRotation(), worldserver, retryoptions);

                gametestharnessinfo.setTestBlockPos(blockposition);
                return !verifyStructureExists(commandlistenerwrapper, gametestharnessinfo.getStructure()) ? Optional.empty() : Optional.of(gametestharnessinfo);
            }
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.test.error.test_instance_not_found.position", blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            return Optional.empty();
        }
    }

    private static int createNewStructure(CommandListenerWrapper commandlistenerwrapper, MinecraftKey minecraftkey, int i, int j, int k) throws CommandSyntaxException {
        if (i <= 48 && j <= 48 && k <= 48) {
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            BlockPosition blockposition = createTestPositionAround(commandlistenerwrapper);
            TestInstanceBlockEntity testinstanceblockentity = GameTestHarnessStructures.createNewEmptyTest(minecraftkey, blockposition, new BaseBlockPosition(i, j, k), EnumBlockRotation.NONE, worldserver);
            BlockPosition blockposition1 = testinstanceblockentity.getStructurePos();
            BlockPosition blockposition2 = blockposition1.offset(i - 1, 0, k - 1);

            BlockPosition.betweenClosedStream(blockposition1, blockposition2).forEach((blockposition3) -> {
                worldserver.setBlockAndUpdate(blockposition3, Blocks.BEDROCK.defaultBlockState());
            });
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.create.success", testinstanceblockentity.getTestName());
            }, true);
            return 1;
        } else {
            throw GameTestHarnessTestCommand.TOO_LARGE.create(48);
        }
    }

    private static int showPos(CommandListenerWrapper commandlistenerwrapper, String s) throws CommandSyntaxException {
        MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) commandlistenerwrapper.getPlayerOrException().pick(10.0D, 1.0F, false);
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        Optional<BlockPosition> optional = GameTestHarnessStructures.findTestContainingPos(blockposition, 15, worldserver);

        if (optional.isEmpty()) {
            optional = GameTestHarnessStructures.findTestContainingPos(blockposition, 200, worldserver);
        }

        if (optional.isEmpty()) {
            throw GameTestHarnessTestCommand.NO_TEST_CONTAINING.create(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        } else {
            TileEntity tileentity = worldserver.getBlockEntity((BlockPosition) optional.get());

            if (tileentity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity testinstanceblockentity = (TestInstanceBlockEntity) tileentity;
                BlockPosition blockposition1 = testinstanceblockentity.getStructurePos();
                BlockPosition blockposition2 = blockposition.subtract(blockposition1);
                int i = blockposition2.getX();
                String s1 = i + ", " + blockposition2.getY() + ", " + blockposition2.getZ();
                String s2 = testinstanceblockentity.getTestName().getString();
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable("commands.test.coordinates", blockposition2.getX(), blockposition2.getY(), blockposition2.getZ()).setStyle(ChatModifier.EMPTY.withBold(true).withColor(EnumChatFormat.GREEN).withHoverEvent(new ChatHoverable.e(IChatBaseComponent.translatable("commands.test.coordinates.copy"))).withClickEvent(new ChatClickable.CopyToClipboard("final BlockPos " + s + " = new BlockPos(" + s1 + ");")));

                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.test.relative_position", s2, ichatmutablecomponent);
                }, false);
                PacketDebug.sendGameTestAddMarker(worldserver, new BlockPosition(blockposition), s1, -2147418368, 10000);
                return 1;
            } else {
                throw GameTestHarnessTestCommand.TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
            }
        }
    }

    private static int stopTests() {
        GameTestHarnessTicker.SINGLETON.clear();
        return 1;
    }

    public static int trackAndStartRunner(CommandListenerWrapper commandlistenerwrapper, GameTestHarnessRunner gametestharnessrunner) {
        gametestharnessrunner.addListener(new GameTestHarnessTestCommand.a(commandlistenerwrapper));
        GameTestHarnessCollector gametestharnesscollector = new GameTestHarnessCollector(gametestharnessrunner.getTestInfos());

        gametestharnesscollector.addListener(new GameTestHarnessTestCommand.b(commandlistenerwrapper, gametestharnesscollector));
        gametestharnesscollector.addFailureListener((gametestharnessinfo) -> {
            FailedTestTracker.rememberFailedTest(gametestharnessinfo.getTestHolder());
        });
        gametestharnessrunner.start();
        return 1;
    }

    private static int exportTestStructure(CommandListenerWrapper commandlistenerwrapper, Holder<GameTestInstance> holder) {
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        MinecraftKey minecraftkey = ((GameTestInstance) holder.value()).structure();

        Objects.requireNonNull(commandlistenerwrapper);
        return !TestInstanceBlockEntity.export(worldserver, minecraftkey, commandlistenerwrapper::sendSystemMessage) ? 0 : 1;
    }

    private static boolean verifyStructureExists(CommandListenerWrapper commandlistenerwrapper, MinecraftKey minecraftkey) {
        if (commandlistenerwrapper.getLevel().getStructureManager().get(minecraftkey).isEmpty()) {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.test.error.structure_not_found", IChatBaseComponent.translationArg(minecraftkey)));
            return false;
        } else {
            return true;
        }
    }

    private static BlockPosition createTestPositionAround(CommandListenerWrapper commandlistenerwrapper) {
        BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());
        int i = commandlistenerwrapper.getLevel().getHeightmapPos(HeightMap.Type.WORLD_SURFACE, blockposition).getY();

        return new BlockPosition(blockposition.getX(), i, blockposition.getZ() + 3);
    }

    public static record b(CommandListenerWrapper source, GameTestHarnessCollector tracker) implements GameTestHarnessListener {

        @Override
        public void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo) {}

        @Override
        public void testPassed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
            this.showTestSummaryIfAllDone();
        }

        @Override
        public void testFailed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
            this.showTestSummaryIfAllDone();
        }

        @Override
        public void testAddedForRerun(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessInfo gametestharnessinfo1, GameTestHarnessRunner gametestharnessrunner) {
            this.tracker.addTestToTrack(gametestharnessinfo1);
        }

        private void showTestSummaryIfAllDone() {
            if (this.tracker.isDone()) {
                this.source.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.test.summary", this.tracker.getTotalCount()).withStyle(EnumChatFormat.WHITE);
                }, true);
                if (this.tracker.hasFailedRequired()) {
                    this.source.sendFailure(IChatBaseComponent.translatable("commands.test.summary.failed", this.tracker.getFailedRequiredCount()));
                } else {
                    this.source.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.test.summary.all_required_passed").withStyle(EnumChatFormat.GREEN);
                    }, true);
                }

                if (this.tracker.hasFailedOptional()) {
                    this.source.sendSystemMessage(IChatBaseComponent.translatable("commands.test.summary.optional_failed", this.tracker.getFailedOptionalCount()));
                }
            }

        }
    }

    private static record a(CommandListenerWrapper source) implements GameTestBatchListener {

        @Override
        public void testBatchStarting(GameTestHarnessBatch gametestharnessbatch) {
            this.source.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.test.batch.starting", gametestharnessbatch.environment().getRegisteredName(), gametestharnessbatch.index());
            }, true);
        }

        @Override
        public void testBatchFinished(GameTestHarnessBatch gametestharnessbatch) {}
    }
}
