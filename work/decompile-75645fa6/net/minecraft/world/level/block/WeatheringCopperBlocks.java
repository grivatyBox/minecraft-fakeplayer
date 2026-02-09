package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockBase;
import org.apache.commons.lang3.function.TriFunction;

public record WeatheringCopperBlocks(Block unaffected, Block exposed, Block weathered, Block oxidized, Block waxed, Block waxedExposed, Block waxedWeathered, Block waxedOxidized) {

    public static <WaxedBlock extends Block, WeatheringBlock extends Block & WeatheringCopper> WeatheringCopperBlocks create(String s, TriFunction<String, Function<BlockBase.Info, Block>, BlockBase.Info, Block> trifunction, Function<BlockBase.Info, WaxedBlock> function, BiFunction<WeatheringCopper.a, BlockBase.Info, WeatheringBlock> bifunction, Function<WeatheringCopper.a, BlockBase.Info> function1) {
        Block block = (Block) trifunction.apply(s, (Function) (blockbase_info) -> {
            return (Block) bifunction.apply(WeatheringCopper.a.UNAFFECTED, blockbase_info);
        }, (BlockBase.Info) function1.apply(WeatheringCopper.a.UNAFFECTED));
        Block block1 = (Block) trifunction.apply("exposed_" + s, (Function) (blockbase_info) -> {
            return (Block) bifunction.apply(WeatheringCopper.a.EXPOSED, blockbase_info);
        }, (BlockBase.Info) function1.apply(WeatheringCopper.a.EXPOSED));
        Block block2 = (Block) trifunction.apply("weathered_" + s, (Function) (blockbase_info) -> {
            return (Block) bifunction.apply(WeatheringCopper.a.WEATHERED, blockbase_info);
        }, (BlockBase.Info) function1.apply(WeatheringCopper.a.WEATHERED));
        Block block3 = (Block) trifunction.apply("oxidized_" + s, (Function) (blockbase_info) -> {
            return (Block) bifunction.apply(WeatheringCopper.a.OXIDIZED, blockbase_info);
        }, (BlockBase.Info) function1.apply(WeatheringCopper.a.OXIDIZED));
        String s1 = "waxed_" + s;

        Objects.requireNonNull(function);
        Block block4 = (Block) trifunction.apply(s1, function::apply, (BlockBase.Info) function1.apply(WeatheringCopper.a.UNAFFECTED));
        String s2 = "waxed_exposed_" + s;

        Objects.requireNonNull(function);
        Block block5 = (Block) trifunction.apply(s2, function::apply, (BlockBase.Info) function1.apply(WeatheringCopper.a.EXPOSED));
        String s3 = "waxed_weathered_" + s;

        Objects.requireNonNull(function);
        Block block6 = (Block) trifunction.apply(s3, function::apply, (BlockBase.Info) function1.apply(WeatheringCopper.a.WEATHERED));
        String s4 = "waxed_oxidized_" + s;

        Objects.requireNonNull(function);
        return new WeatheringCopperBlocks(block, block1, block2, block3, block4, block5, block6, (Block) trifunction.apply(s4, function::apply, (BlockBase.Info) function1.apply(WeatheringCopper.a.OXIDIZED)));
    }

    public ImmutableBiMap<Block, Block> weatheringMapping() {
        return ImmutableBiMap.of(this.unaffected, this.exposed, this.exposed, this.weathered, this.weathered, this.oxidized);
    }

    public ImmutableBiMap<Block, Block> waxedMapping() {
        return ImmutableBiMap.of(this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized);
    }

    public ImmutableList<Block> asList() {
        return ImmutableList.of(this.unaffected, this.waxed, this.exposed, this.waxedExposed, this.weathered, this.waxedWeathered, this.oxidized, this.waxedOxidized);
    }

    public void forEach(Consumer<Block> consumer) {
        consumer.accept(this.unaffected);
        consumer.accept(this.exposed);
        consumer.accept(this.weathered);
        consumer.accept(this.oxidized);
        consumer.accept(this.waxed);
        consumer.accept(this.waxedExposed);
        consumer.accept(this.waxedWeathered);
        consumer.accept(this.waxedOxidized);
    }
}
