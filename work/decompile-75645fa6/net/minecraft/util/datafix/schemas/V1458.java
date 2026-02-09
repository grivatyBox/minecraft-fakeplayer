package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class V1458 extends DataConverterSchemaNamed {

    public V1458(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        super.registerTypes(schema, map, map1);
        schema.registerType(true, DataConverterTypes.ENTITY, () -> {
            return DSL.and(DataConverterTypes.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", DataConverterTypes.TEXT_COMPONENT.in(schema), DSL.taggedChoiceLazy("id", namespacedString(), map)));
        });
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "minecraft:beacon", () -> {
            return nameable(schema);
        });
        schema.register(map, "minecraft:banner", () -> {
            return nameable(schema);
        });
        schema.register(map, "minecraft:brewing_stand", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:chest", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:trapped_chest", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:dispenser", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:dropper", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:enchanting_table", () -> {
            return nameable(schema);
        });
        schema.register(map, "minecraft:furnace", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:hopper", () -> {
            return nameableInventory(schema);
        });
        schema.register(map, "minecraft:shulker_box", () -> {
            return nameableInventory(schema);
        });
        return map;
    }

    public static TypeTemplate nameableInventory(Schema schema) {
        return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "CustomName", DataConverterTypes.TEXT_COMPONENT.in(schema));
    }

    public static TypeTemplate nameable(Schema schema) {
        return DSL.optionalFields("CustomName", DataConverterTypes.TEXT_COMPONENT.in(schema));
    }
}
