package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3813 extends DataConverterSchemaNamed {

    public V3813(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(false, DataConverterTypes.SAVED_DATA_MAP_DATA, () -> {
            return DSL.optionalFields("data", DSL.optionalFields("banners", DSL.list(DSL.optionalFields("name", DataConverterTypes.TEXT_COMPONENT.in(schema)))));
        });
    }
}
