package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class SaddleEquipmentSlotFix extends DataFix {

    private static final Set<String> ENTITIES_WITH_SADDLE_ITEM = Set.of("minecraft:horse", "minecraft:skeleton_horse", "minecraft:zombie_horse", "minecraft:donkey", "minecraft:mule", "minecraft:camel", "minecraft:llama", "minecraft:trader_llama");
    private static final Set<String> ENTITIES_WITH_SADDLE_FLAG = Set.of("minecraft:pig", "minecraft:strider");
    private static final String SADDLE_FLAG = "Saddle";
    private static final String NEW_SADDLE = "saddle";

    public SaddleEquipmentSlotFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(DataConverterTypes.ENTITY);
        OpticFinder<Pair<String, ?>> opticfinder = DSL.typeFinder(taggedchoice_taggedchoicetype);
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.ENTITY);
        Type<?> type2 = ExtraDataFixUtils.patchSubType(type, type, type1);

        return this.fixTypeEverywhereTyped("SaddleEquipmentSlotFix", type, type1, (typed) -> {
            String s = (String) typed.getOptional(opticfinder).map(Pair::getFirst).map(DataConverterSchemaNamed::ensureNamespaced).orElse("");
            Typed<?> typed1 = ExtraDataFixUtils.cast(type2, typed);

            return SaddleEquipmentSlotFix.ENTITIES_WITH_SADDLE_ITEM.contains(s) ? SystemUtils.writeAndReadTypedOrThrow(typed1, type1, SaddleEquipmentSlotFix::fixEntityWithSaddleItem) : (SaddleEquipmentSlotFix.ENTITIES_WITH_SADDLE_FLAG.contains(s) ? SystemUtils.writeAndReadTypedOrThrow(typed1, type1, SaddleEquipmentSlotFix::fixEntityWithSaddleFlag) : ExtraDataFixUtils.cast(type1, typed));
        });
    }

    private static Dynamic<?> fixEntityWithSaddleItem(Dynamic<?> dynamic) {
        return dynamic.get("SaddleItem").result().isEmpty() ? dynamic : fixDropChances(dynamic.renameField("SaddleItem", "saddle"));
    }

    private static Dynamic<?> fixEntityWithSaddleFlag(Dynamic<?> dynamic) {
        boolean flag = dynamic.get("Saddle").asBoolean(false);

        dynamic = dynamic.remove("Saddle");
        if (!flag) {
            return dynamic;
        } else {
            Dynamic<?> dynamic1 = dynamic.emptyMap().set("id", dynamic.createString("minecraft:saddle")).set("count", dynamic.createInt(1));

            return fixDropChances(dynamic.set("saddle", dynamic1));
        }
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = dynamic.get("drop_chances").orElseEmptyMap().set("saddle", dynamic.createFloat(2.0F));

        return dynamic.set("drop_chances", dynamic1);
    }
}
