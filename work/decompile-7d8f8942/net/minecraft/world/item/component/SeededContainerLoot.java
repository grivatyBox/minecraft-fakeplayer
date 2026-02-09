package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.storage.loot.LootTable;

public record SeededContainerLoot(ResourceKey<LootTable> lootTable, long seed) implements TooltipProvider {

    private static final IChatBaseComponent UNKNOWN_CONTENTS = IChatBaseComponent.translatable("item.container.loot_table.unknown");
    public static final Codec<SeededContainerLoot> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(SeededContainerLoot::lootTable), Codec.LONG.optionalFieldOf("seed", 0L).forGetter(SeededContainerLoot::seed)).apply(instance, SeededContainerLoot::new);
    });

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        consumer.accept(SeededContainerLoot.UNKNOWN_CONTENTS);
    }
}
