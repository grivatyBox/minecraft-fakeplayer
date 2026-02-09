package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V1488 extends DataConverterSchemaNamed {

    public V1488(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "minecraft:command_block", () -> {
            return DSL.optionalFields("CustomName", DataConverterTypes.TEXT_COMPONENT.in(schema), "LastOutput", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        return map;
    }
}
