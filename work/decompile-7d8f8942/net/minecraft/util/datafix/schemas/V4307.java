package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V4307 extends DataConverterSchemaNamed {

    public V4307(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedmap = V4059.components(schema);

        sequencedmap.put("minecraft:can_place_on", (Supplier) () -> {
            return adventureModePredicate(schema);
        });
        sequencedmap.put("minecraft:can_break", (Supplier) () -> {
            return adventureModePredicate(schema);
        });
        return sequencedmap;
    }

    private static TypeTemplate adventureModePredicate(Schema schema) {
        TypeTemplate typetemplate = DSL.optionalFields("blocks", DSL.or(DataConverterTypes.BLOCK_NAME.in(schema), DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))));

        return DSL.or(typetemplate, DSL.list(typetemplate));
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.DATA_COMPONENTS, () -> {
            return DSL.optionalFieldsLazy(components(schema));
        });
    }
}
