package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3818_3 extends DataConverterSchemaNamed {

    public V3818_3(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedmap = new LinkedHashMap();

        sequencedmap.put("minecraft:bees", (Supplier) () -> {
            return DSL.list(DSL.optionalFields("entity_data", DataConverterTypes.ENTITY_TREE.in(schema)));
        });
        sequencedmap.put("minecraft:block_entity_data", (Supplier) () -> {
            return DataConverterTypes.BLOCK_ENTITY.in(schema);
        });
        sequencedmap.put("minecraft:bundle_contents", (Supplier) () -> {
            return DSL.list(DataConverterTypes.ITEM_STACK.in(schema));
        });
        sequencedmap.put("minecraft:can_break", (Supplier) () -> {
            return DSL.optionalFields("predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))))));
        });
        sequencedmap.put("minecraft:can_place_on", (Supplier) () -> {
            return DSL.optionalFields("predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))))));
        });
        sequencedmap.put("minecraft:charged_projectiles", (Supplier) () -> {
            return DSL.list(DataConverterTypes.ITEM_STACK.in(schema));
        });
        sequencedmap.put("minecraft:container", (Supplier) () -> {
            return DSL.list(DSL.optionalFields("item", DataConverterTypes.ITEM_STACK.in(schema)));
        });
        sequencedmap.put("minecraft:entity_data", (Supplier) () -> {
            return DataConverterTypes.ENTITY_TREE.in(schema);
        });
        sequencedmap.put("minecraft:pot_decorations", (Supplier) () -> {
            return DSL.list(DataConverterTypes.ITEM_NAME.in(schema));
        });
        sequencedmap.put("minecraft:food", (Supplier) () -> {
            return DSL.optionalFields("using_converts_to", DataConverterTypes.ITEM_STACK.in(schema));
        });
        sequencedmap.put("minecraft:custom_name", (Supplier) () -> {
            return DataConverterTypes.TEXT_COMPONENT.in(schema);
        });
        sequencedmap.put("minecraft:item_name", (Supplier) () -> {
            return DataConverterTypes.TEXT_COMPONENT.in(schema);
        });
        sequencedmap.put("minecraft:lore", (Supplier) () -> {
            return DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        sequencedmap.put("minecraft:written_book_content", (Supplier) () -> {
            return DSL.optionalFields("pages", DSL.list(DSL.or(DSL.optionalFields("raw", DataConverterTypes.TEXT_COMPONENT.in(schema), "filtered", DataConverterTypes.TEXT_COMPONENT.in(schema)), DataConverterTypes.TEXT_COMPONENT.in(schema))));
        });
        return sequencedmap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.DATA_COMPONENTS, () -> {
            return DSL.optionalFieldsLazy(components(schema));
        });
    }
}
