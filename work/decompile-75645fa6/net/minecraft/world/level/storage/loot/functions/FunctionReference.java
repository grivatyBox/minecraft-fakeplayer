package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter((functionreference) -> {
            return functionreference.name;
        })).apply(instance, FunctionReference::new);
    });
    private final ResourceKey<LootItemFunction> name;

    private FunctionReference(List<LootItemCondition> list, ResourceKey<LootItemFunction> resourcekey) {
        super(list);
        this.name = resourcekey;
    }

    @Override
    public LootItemFunctionType<FunctionReference> getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(LootCollector lootcollector) {
        if (!lootcollector.allowsReferences()) {
            lootcollector.reportProblem(new LootCollector.d(this.name));
        } else if (lootcollector.hasVisitedElement(this.name)) {
            lootcollector.reportProblem(new LootCollector.c(this.name));
        } else {
            super.validate(lootcollector);
            lootcollector.resolver().get(this.name).ifPresentOrElse((holder_c) -> {
                ((LootItemFunction) holder_c.value()).validate(lootcollector.enterElement(new ProblemReporter.b(this.name), this.name));
            }, () -> {
                lootcollector.reportProblem(new LootCollector.a(this.name));
            });
        }
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        LootItemFunction lootitemfunction = (LootItemFunction) loottableinfo.getResolver().get(this.name).map(Holder::value).orElse((Object) null);

        if (lootitemfunction == null) {
            FunctionReference.LOGGER.warn("Unknown function: {}", this.name.location());
            return itemstack;
        } else {
            LootTableInfo.e<?> loottableinfo_e = LootTableInfo.createVisitedEntry(lootitemfunction);

            if (loottableinfo.pushVisitedElement(loottableinfo_e)) {
                ItemStack itemstack1;

                try {
                    itemstack1 = (ItemStack) lootitemfunction.apply(itemstack, loottableinfo);
                } finally {
                    loottableinfo.popVisitedElement(loottableinfo_e);
                }

                return itemstack1;
            } else {
                FunctionReference.LOGGER.warn("Detected infinite loop in loot tables");
                return itemstack;
            }
        }
    }

    public static LootItemFunctionConditional.a<?> functionReference(ResourceKey<LootItemFunction> resourcekey) {
        return simpleBuilder((list) -> {
            return new FunctionReference(list, resourcekey);
        });
    }
}
