package net.minecraft.world.level.block;

import com.google.common.collect.BiMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyChestType;

public class CopperChestBlock extends BlockChest {

    public static final MapCodec<CopperChestBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState), BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(BlockChest::getOpenChestSound), BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(BlockChest::getCloseChestSound), propertiesCodec()).apply(instance, CopperChestBlock::new);
    });
    private static final Map<Block, Supplier<Block>> COPPER_TO_COPPER_CHEST_MAPPING = Map.of(Blocks.COPPER_BLOCK, (Supplier) () -> {
        return Blocks.COPPER_CHEST;
    }, Blocks.EXPOSED_COPPER, (Supplier) () -> {
        return Blocks.EXPOSED_COPPER_CHEST;
    }, Blocks.WEATHERED_COPPER, (Supplier) () -> {
        return Blocks.WEATHERED_COPPER_CHEST;
    }, Blocks.OXIDIZED_COPPER, (Supplier) () -> {
        return Blocks.OXIDIZED_COPPER_CHEST;
    }, Blocks.WAXED_COPPER_BLOCK, (Supplier) () -> {
        return Blocks.COPPER_CHEST;
    }, Blocks.WAXED_EXPOSED_COPPER, (Supplier) () -> {
        return Blocks.EXPOSED_COPPER_CHEST;
    }, Blocks.WAXED_WEATHERED_COPPER, (Supplier) () -> {
        return Blocks.WEATHERED_COPPER_CHEST;
    }, Blocks.WAXED_OXIDIZED_COPPER, (Supplier) () -> {
        return Blocks.OXIDIZED_COPPER_CHEST;
    });
    private final WeatheringCopper.a weatherState;

    @Override
    public MapCodec<? extends CopperChestBlock> codec() {
        return CopperChestBlock.CODEC;
    }

    public CopperChestBlock(WeatheringCopper.a weatheringcopper_a, SoundEffect soundeffect, SoundEffect soundeffect1, BlockBase.Info blockbase_info) {
        super(() -> {
            return TileEntityTypes.CHEST;
        }, soundeffect, soundeffect1, blockbase_info);
        this.weatherState = weatheringcopper_a;
    }

    @Override
    public boolean chestCanConnectTo(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.COPPER_CHESTS) && iblockdata.hasProperty(BlockChest.TYPE);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = super.getStateForPlacement(blockactioncontext);

        return getLeastOxidizedChestOfConnectedBlocks(iblockdata, blockactioncontext.getLevel(), blockactioncontext.getClickedPos());
    }

    private static IBlockData getLeastOxidizedChestOfConnectedBlocks(IBlockData iblockdata, World world, BlockPosition blockposition) {
        IBlockData iblockdata1 = world.getBlockState(blockposition.relative(getConnectedDirection(iblockdata)));

        if (!((BlockPropertyChestType) iblockdata.getValue(BlockChest.TYPE)).equals(BlockPropertyChestType.SINGLE)) {
            Block block = iblockdata.getBlock();

            if (block instanceof CopperChestBlock) {
                CopperChestBlock copperchestblock = (CopperChestBlock) block;

                block = iblockdata1.getBlock();
                if (block instanceof CopperChestBlock) {
                    CopperChestBlock copperchestblock1 = (CopperChestBlock) block;
                    IBlockData iblockdata2 = iblockdata;
                    IBlockData iblockdata3 = iblockdata1;

                    if (copperchestblock.isWaxed() != copperchestblock1.isWaxed()) {
                        iblockdata2 = (IBlockData) unwaxBlock(copperchestblock, iblockdata).orElse(iblockdata);
                        iblockdata3 = (IBlockData) unwaxBlock(copperchestblock1, iblockdata1).orElse(iblockdata1);
                    }

                    Block block1 = copperchestblock.weatherState.ordinal() <= copperchestblock1.weatherState.ordinal() ? iblockdata2.getBlock() : iblockdata3.getBlock();

                    return block1.withPropertiesOf(iblockdata2);
                }
            }
        }

        return iblockdata;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        IBlockData iblockdata2 = super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);

        if (this.chestCanConnectTo(iblockdata1)) {
            BlockPropertyChestType blockpropertychesttype = (BlockPropertyChestType) iblockdata2.getValue(BlockChest.TYPE);

            if (!blockpropertychesttype.equals(BlockPropertyChestType.SINGLE) && getConnectedDirection(iblockdata2) == enumdirection) {
                return iblockdata1.getBlock().withPropertiesOf(iblockdata2);
            }
        }

        return iblockdata2;
    }

    private static Optional<IBlockData> unwaxBlock(CopperChestBlock copperchestblock, IBlockData iblockdata) {
        return !copperchestblock.isWaxed() ? Optional.of(iblockdata) : Optional.ofNullable((Block) ((BiMap) HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(iblockdata.getBlock())).map((block) -> {
            return block.withPropertiesOf(iblockdata);
        });
    }

    public WeatheringCopper.a getState() {
        return this.weatherState;
    }

    public static IBlockData getFromCopperBlock(Block block, EnumDirection enumdirection, World world, BlockPosition blockposition) {
        Map map = CopperChestBlock.COPPER_TO_COPPER_CHEST_MAPPING;
        Block block1 = Blocks.COPPER_CHEST;

        Objects.requireNonNull(block1);
        CopperChestBlock copperchestblock = (CopperChestBlock) ((Supplier) map.getOrDefault(block, block1::asBlock)).get();
        BlockPropertyChestType blockpropertychesttype = copperchestblock.getChestType(world, blockposition, enumdirection);
        IBlockData iblockdata = (IBlockData) ((IBlockData) copperchestblock.defaultBlockState().setValue(CopperChestBlock.FACING, enumdirection)).setValue(CopperChestBlock.TYPE, blockpropertychesttype);

        return getLeastOxidizedChestOfConnectedBlocks(iblockdata, world, blockposition);
    }

    public boolean isWaxed() {
        return true;
    }

    @Override
    public boolean shouldChangedStateKeepBlockEntity(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.COPPER_CHESTS);
    }
}
