package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;

public class PlayerEquipmentFix extends DataFix {

    private static final Map<Integer, String> SLOT_TRANSLATIONS = Map.of(100, "feet", 101, "legs", 102, "chest", 103, "head", -106, "offhand");

    public PlayerEquipmentFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getTypeRaw(DataConverterTypes.PLAYER);
        Type<?> type1 = this.getOutputSchema().getTypeRaw(DataConverterTypes.PLAYER);

        return this.writeFixAndRead("Player Equipment Fix", type, type1, (dynamic) -> {
            Map<Dynamic<?>, Dynamic<?>> map = new HashMap();

            dynamic = dynamic.update("Inventory", (dynamic1) -> {
                return dynamic1.createList(dynamic1.asStream().filter((dynamic2) -> {
                    int i = dynamic2.get("Slot").asInt(-1);
                    String s = (String) PlayerEquipmentFix.SLOT_TRANSLATIONS.get(i);

                    if (s != null) {
                        map.put(dynamic1.createString(s), dynamic2.remove("Slot"));
                    }

                    return s == null;
                }));
            });
            dynamic = dynamic.set("equipment", dynamic.createMap(map));
            return dynamic;
        });
    }
}
