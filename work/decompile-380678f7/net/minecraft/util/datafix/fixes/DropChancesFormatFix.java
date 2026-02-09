package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;

public class DropChancesFormatFix extends DataFix {

    private static final List<String> ARMOR_SLOT_NAMES = List.of("feet", "legs", "chest", "head");
    private static final List<String> HAND_SLOT_NAMES = List.of("mainhand", "offhand");
    private static final float DEFAULT_CHANCE = 0.085F;

    public DropChancesFormatFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DropChancesFormatFix", this.getInputSchema().getType(DataConverterTypes.ENTITY), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                List<Float> list = parseDropChances(dynamic.get("ArmorDropChances"));
                List<Float> list1 = parseDropChances(dynamic.get("HandDropChances"));
                float f = (Float) dynamic.get("body_armor_drop_chance").asNumber().result().map(Number::floatValue).orElse(0.085F);

                dynamic = dynamic.remove("ArmorDropChances").remove("HandDropChances").remove("body_armor_drop_chance");
                Dynamic<?> dynamic1 = dynamic.emptyMap();

                dynamic1 = addSlotChances(dynamic1, list, DropChancesFormatFix.ARMOR_SLOT_NAMES);
                dynamic1 = addSlotChances(dynamic1, list1, DropChancesFormatFix.HAND_SLOT_NAMES);
                if (f != 0.085F) {
                    dynamic1 = dynamic1.set("body", dynamic.createFloat(f));
                }

                return !dynamic1.equals(dynamic.emptyMap()) ? dynamic.set("drop_chances", dynamic1) : dynamic;
            });
        });
    }

    private static Dynamic<?> addSlotChances(Dynamic<?> dynamic, List<Float> list, List<String> list1) {
        for (int i = 0; i < list1.size() && i < list.size(); ++i) {
            String s = (String) list1.get(i);
            float f = (Float) list.get(i);

            if (f != 0.085F) {
                dynamic = dynamic.set(s, dynamic.createFloat(f));
            }
        }

        return dynamic;
    }

    private static List<Float> parseDropChances(OptionalDynamic<?> optionaldynamic) {
        return optionaldynamic.asStream().map((dynamic) -> {
            return dynamic.asFloat(0.085F);
        }).toList();
    }
}
