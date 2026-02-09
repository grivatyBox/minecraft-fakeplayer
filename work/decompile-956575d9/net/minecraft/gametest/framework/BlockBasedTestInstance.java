package net.minecraft.gametest.framework;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.TestBlockMode;

public class BlockBasedTestInstance extends GameTestInstance {

    public static final MapCodec<BlockBasedTestInstance> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(TestData.CODEC.forGetter(GameTestInstance::info)).apply(instance, BlockBasedTestInstance::new);
    });

    public BlockBasedTestInstance(TestData<Holder<TestEnvironmentDefinition>> testdata) {
        super(testdata);
    }

    @Override
    public void run(GameTestHarnessHelper gametestharnesshelper) {
        BlockPosition blockposition = this.findStartBlock(gametestharnesshelper);
        TestBlockEntity testblockentity = (TestBlockEntity) gametestharnesshelper.getBlockEntity(blockposition, TestBlockEntity.class);

        testblockentity.trigger();
        gametestharnesshelper.onEachTick(() -> {
            List<BlockPosition> list = this.findTestBlocks(gametestharnesshelper, TestBlockMode.ACCEPT);

            if (list.isEmpty()) {
                gametestharnesshelper.fail(IChatBaseComponent.translatable("test_block.error.missing", TestBlockMode.ACCEPT.getDisplayName()));
            }

            boolean flag = list.stream().map((blockposition1) -> {
                return (TestBlockEntity) gametestharnesshelper.getBlockEntity(blockposition1, TestBlockEntity.class);
            }).anyMatch(TestBlockEntity::hasTriggered);

            if (flag) {
                gametestharnesshelper.succeed();
            } else {
                this.forAllTriggeredTestBlocks(gametestharnesshelper, TestBlockMode.FAIL, (testblockentity1) -> {
                    gametestharnesshelper.fail(IChatBaseComponent.literal(testblockentity1.getMessage()));
                });
                this.forAllTriggeredTestBlocks(gametestharnesshelper, TestBlockMode.LOG, TestBlockEntity::trigger);
            }

        });
    }

    private void forAllTriggeredTestBlocks(GameTestHarnessHelper gametestharnesshelper, TestBlockMode testblockmode, Consumer<TestBlockEntity> consumer) {
        for (BlockPosition blockposition : this.findTestBlocks(gametestharnesshelper, testblockmode)) {
            TestBlockEntity testblockentity = (TestBlockEntity) gametestharnesshelper.getBlockEntity(blockposition, TestBlockEntity.class);

            if (testblockentity.hasTriggered()) {
                consumer.accept(testblockentity);
                testblockentity.reset();
            }
        }

    }

    private BlockPosition findStartBlock(GameTestHarnessHelper gametestharnesshelper) {
        List<BlockPosition> list = this.findTestBlocks(gametestharnesshelper, TestBlockMode.START);

        if (list.isEmpty()) {
            gametestharnesshelper.fail(IChatBaseComponent.translatable("test_block.error.missing", TestBlockMode.START.getDisplayName()));
        }

        if (list.size() != 1) {
            gametestharnesshelper.fail(IChatBaseComponent.translatable("test_block.error.too_many", TestBlockMode.START.getDisplayName()));
        }

        return (BlockPosition) list.getFirst();
    }

    private List<BlockPosition> findTestBlocks(GameTestHarnessHelper gametestharnesshelper, TestBlockMode testblockmode) {
        List<BlockPosition> list = new ArrayList();

        gametestharnesshelper.forEveryBlockInStructure((blockposition) -> {
            IBlockData iblockdata = gametestharnesshelper.getBlockState(blockposition);

            if (iblockdata.is(Blocks.TEST_BLOCK) && iblockdata.getValue(TestBlock.MODE) == testblockmode) {
                list.add(blockposition.immutable());
            }

        });
        return list;
    }

    @Override
    public MapCodec<BlockBasedTestInstance> codec() {
        return BlockBasedTestInstance.CODEC;
    }

    @Override
    protected IChatMutableComponent typeDescription() {
        return IChatBaseComponent.translatable("test_instance.type.block_based");
    }
}
