package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ToggleTooltips extends LootItemFunctionConditional {

    public static final MapCodec<ToggleTooltips> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(Codec.unboundedMap(DataComponentType.CODEC, Codec.BOOL).fieldOf("toggles").forGetter((toggletooltips) -> {
            return toggletooltips.values;
        })).apply(instance, ToggleTooltips::new);
    });
    private final Map<DataComponentType<?>, Boolean> values;

    private ToggleTooltips(List<LootItemCondition> list, Map<DataComponentType<?>, Boolean> map) {
        super(list);
        this.values = map;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        itemstack.update(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT, (tooltipdisplay) -> {
            for (Map.Entry<DataComponentType<?>, Boolean> map_entry : this.values.entrySet()) {
                boolean flag = (Boolean) map_entry.getValue();

                tooltipdisplay = tooltipdisplay.withHidden((DataComponentType) map_entry.getKey(), !flag);
            }

            return tooltipdisplay;
        });
        return itemstack;
    }

    @Override
    public LootItemFunctionType<ToggleTooltips> getType() {
        return LootItemFunctions.TOGGLE_TOOLTIPS;
    }
}
