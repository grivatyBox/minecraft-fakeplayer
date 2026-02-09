package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V4301 extends DataConverterSchemaNamed {

    public V4301(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.ENTITY_EQUIPMENT, () -> {
            return DSL.optional(DSL.field("equipment", DSL.optionalFields(new Pair[]{Pair.of("mainhand", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("offhand", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("feet", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("legs", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("chest", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("head", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("body", DataConverterTypes.ITEM_STACK.in(schema)), Pair.of("saddle", DataConverterTypes.ITEM_STACK.in(schema))})));
        });
    }
}
