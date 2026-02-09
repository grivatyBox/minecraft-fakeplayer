package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V4290 extends DataConverterSchemaNamed {

    public V4290(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.TEXT_COMPONENT, () -> {
            return DSL.or(DSL.or(DSL.constType(DSL.string()), DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema))), DSL.optionalFields("extra", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema)), "separator", DataConverterTypes.TEXT_COMPONENT.in(schema), "hoverEvent", DSL.taggedChoice("action", DSL.string(), Map.of("show_text", DSL.optionalFields("contents", DataConverterTypes.TEXT_COMPONENT.in(schema)), "show_item", DSL.optionalFields("contents", DSL.or(DataConverterTypes.ITEM_STACK.in(schema), DataConverterTypes.ITEM_NAME.in(schema))), "show_entity", DSL.optionalFields("type", DataConverterTypes.ENTITY_NAME.in(schema), "name", DataConverterTypes.TEXT_COMPONENT.in(schema))))));
        });
    }
}
