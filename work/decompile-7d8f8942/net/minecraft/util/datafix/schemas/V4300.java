package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V4300 extends DataConverterSchemaNamed {

    public V4300(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);

        schema.register(map, "minecraft:llama", (s) -> {
            return entityWithInventory(schema);
        });
        schema.register(map, "minecraft:trader_llama", (s) -> {
            return entityWithInventory(schema);
        });
        schema.register(map, "minecraft:donkey", (s) -> {
            return entityWithInventory(schema);
        });
        schema.register(map, "minecraft:mule", (s) -> {
            return entityWithInventory(schema);
        });
        schema.registerSimple(map, "minecraft:horse");
        schema.registerSimple(map, "minecraft:skeleton_horse");
        schema.registerSimple(map, "minecraft:zombie_horse");
        return map;
    }

    private static TypeTemplate entityWithInventory(Schema schema) {
        return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
    }
}
