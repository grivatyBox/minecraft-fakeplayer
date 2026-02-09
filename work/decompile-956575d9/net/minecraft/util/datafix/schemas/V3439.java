package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V3439 extends DataConverterSchemaNamed {

    public V3439(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        this.register(map, "minecraft:sign", () -> {
            return sign(schema);
        });
        this.register(map, "minecraft:hanging_sign", () -> {
            return sign(schema);
        });
        return map;
    }

    private static TypeTemplate sign(Schema schema) {
        return DSL.optionalFields("front_text", DSL.optionalFields("messages", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema)), "filtered_messages", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema))), "back_text", DSL.optionalFields("messages", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema)), "filtered_messages", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema))));
    }
}
