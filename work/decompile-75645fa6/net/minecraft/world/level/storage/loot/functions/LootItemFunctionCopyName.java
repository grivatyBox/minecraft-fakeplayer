package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionCopyName extends LootItemFunctionConditional {

    private static final ExtraCodecs.b<String, LootItemFunctionCopyName.a> SOURCES = new ExtraCodecs.b<String, LootItemFunctionCopyName.a>();
    public static final MapCodec<LootItemFunctionCopyName> CODEC;
    private final LootItemFunctionCopyName.a source;

    private LootItemFunctionCopyName(List<LootItemCondition> list, LootItemFunctionCopyName.a lootitemfunctioncopyname_a) {
        super(list);
        this.source = lootitemfunctioncopyname_a;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionCopyName> getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        Object object = loottableinfo.getOptionalParameter(this.source.param);

        if (object instanceof INamableTileEntity inamabletileentity) {
            itemstack.set(DataComponents.CUSTOM_NAME, inamabletileentity.getCustomName());
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> copyName(LootItemFunctionCopyName.a lootitemfunctioncopyname_a) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionCopyName(list, lootitemfunctioncopyname_a);
        });
    }

    static {
        for (LootTableInfo.EntityTarget loottableinfo_entitytarget : LootTableInfo.EntityTarget.values()) {
            LootItemFunctionCopyName.SOURCES.put(loottableinfo_entitytarget.getSerializedName(), new LootItemFunctionCopyName.a(loottableinfo_entitytarget.getParam()));
        }

        for (LootTableInfo.a loottableinfo_a : LootTableInfo.a.values()) {
            LootItemFunctionCopyName.SOURCES.put(loottableinfo_a.getSerializedName(), new LootItemFunctionCopyName.a(loottableinfo_a.getParam()));
        }

        CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return commonFields(instance).and(LootItemFunctionCopyName.SOURCES.codec(Codec.STRING).fieldOf("source").forGetter((lootitemfunctioncopyname) -> {
                return lootitemfunctioncopyname.source;
            })).apply(instance, LootItemFunctionCopyName::new);
        });
    }

    public static record a(ContextKey<?> param) {

    }
}
