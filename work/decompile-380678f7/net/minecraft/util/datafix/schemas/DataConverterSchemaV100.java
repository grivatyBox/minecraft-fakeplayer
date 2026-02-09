package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class DataConverterSchemaV100 extends Schema {

    public DataConverterSchemaV100(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.ENTITY_EQUIPMENT, () -> {
            return DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)))), new TypeTemplate[]{DSL.optional(DSL.field("HandItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)))), DSL.optional(DSL.field("body_armor_item", DataConverterTypes.ITEM_STACK.in(schema))), DSL.optional(DSL.field("saddle", DataConverterTypes.ITEM_STACK.in(schema)))});
        });
    }
}
