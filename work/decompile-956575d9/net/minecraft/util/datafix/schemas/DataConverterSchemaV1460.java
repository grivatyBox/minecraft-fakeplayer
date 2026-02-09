package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;

public class DataConverterSchemaV1460 extends DataConverterSchemaNamed {

    public DataConverterSchemaV1460(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.registerSimple(map, s);
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.register(map, s, () -> {
            return V1458.nameableInventory(schema);
        });
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();

        schema.register(map, "minecraft:area_effect_cloud", (s) -> {
            return DSL.optionalFields("Particle", DataConverterTypes.PARTICLE.in(schema));
        });
        registerMob(schema, map, "minecraft:armor_stand");
        schema.register(map, "minecraft:arrow", (s) -> {
            return DSL.optionalFields("inBlockState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerMob(schema, map, "minecraft:bat");
        registerMob(schema, map, "minecraft:blaze");
        schema.registerSimple(map, "minecraft:boat");
        registerMob(schema, map, "minecraft:cave_spider");
        schema.register(map, "minecraft:chest_minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        registerMob(schema, map, "minecraft:chicken");
        schema.register(map, "minecraft:commandblock_minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema), "LastOutput", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        registerMob(schema, map, "minecraft:cow");
        registerMob(schema, map, "minecraft:creeper");
        schema.register(map, "minecraft:donkey", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "minecraft:dragon_fireball");
        schema.registerSimple(map, "minecraft:egg");
        registerMob(schema, map, "minecraft:elder_guardian");
        schema.registerSimple(map, "minecraft:ender_crystal");
        registerMob(schema, map, "minecraft:ender_dragon");
        schema.register(map, "minecraft:enderman", (s) -> {
            return DSL.optionalFields("carriedBlockState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerMob(schema, map, "minecraft:endermite");
        schema.registerSimple(map, "minecraft:ender_pearl");
        schema.registerSimple(map, "minecraft:evocation_fangs");
        registerMob(schema, map, "minecraft:evocation_illager");
        schema.registerSimple(map, "minecraft:eye_of_ender_signal");
        schema.register(map, "minecraft:falling_block", (s) -> {
            return DSL.optionalFields("BlockState", DataConverterTypes.BLOCK_STATE.in(schema), "TileEntityData", DataConverterTypes.BLOCK_ENTITY.in(schema));
        });
        schema.registerSimple(map, "minecraft:fireball");
        schema.register(map, "minecraft:fireworks_rocket", (s) -> {
            return DSL.optionalFields("FireworksItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.register(map, "minecraft:furnace_minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerMob(schema, map, "minecraft:ghast");
        registerMob(schema, map, "minecraft:giant");
        registerMob(schema, map, "minecraft:guardian");
        schema.register(map, "minecraft:hopper_minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        schema.register(map, "minecraft:horse", (s) -> {
            return DSL.optionalFields("ArmorItem", DataConverterTypes.ITEM_STACK.in(schema), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:husk");
        registerMob(schema, map, "minecraft:illusion_illager");
        schema.register(map, "minecraft:item", (s) -> {
            return DSL.optionalFields("Item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.register(map, "minecraft:item_frame", (s) -> {
            return DSL.optionalFields("Item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "minecraft:leash_knot");
        schema.register(map, "minecraft:llama", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema), "DecorItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "minecraft:llama_spit");
        registerMob(schema, map, "minecraft:magma_cube");
        schema.register(map, "minecraft:minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerMob(schema, map, "minecraft:mooshroom");
        schema.register(map, "minecraft:mule", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:ocelot");
        schema.registerSimple(map, "minecraft:painting");
        registerMob(schema, map, "minecraft:parrot");
        registerMob(schema, map, "minecraft:pig");
        registerMob(schema, map, "minecraft:polar_bear");
        schema.register(map, "minecraft:potion", (s) -> {
            return DSL.optionalFields("Potion", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:rabbit");
        registerMob(schema, map, "minecraft:sheep");
        registerMob(schema, map, "minecraft:shulker");
        schema.registerSimple(map, "minecraft:shulker_bullet");
        registerMob(schema, map, "minecraft:silverfish");
        registerMob(schema, map, "minecraft:skeleton");
        schema.register(map, "minecraft:skeleton_horse", (s) -> {
            return DSL.optionalFields("SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:slime");
        schema.registerSimple(map, "minecraft:small_fireball");
        schema.registerSimple(map, "minecraft:snowball");
        registerMob(schema, map, "minecraft:snowman");
        schema.register(map, "minecraft:spawner_minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema), DataConverterTypes.UNTAGGED_SPAWNER.in(schema));
        });
        schema.register(map, "minecraft:spectral_arrow", (s) -> {
            return DSL.optionalFields("inBlockState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerMob(schema, map, "minecraft:spider");
        registerMob(schema, map, "minecraft:squid");
        registerMob(schema, map, "minecraft:stray");
        schema.registerSimple(map, "minecraft:tnt");
        schema.register(map, "minecraft:tnt_minecart", (s) -> {
            return DSL.optionalFields("DisplayState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerMob(schema, map, "minecraft:vex");
        schema.register(map, "minecraft:villager", (s) -> {
            return DSL.optionalFields("Inventory", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DataConverterTypes.VILLAGER_TRADE.in(schema))));
        });
        registerMob(schema, map, "minecraft:villager_golem");
        registerMob(schema, map, "minecraft:vindication_illager");
        registerMob(schema, map, "minecraft:witch");
        registerMob(schema, map, "minecraft:wither");
        registerMob(schema, map, "minecraft:wither_skeleton");
        schema.registerSimple(map, "minecraft:wither_skull");
        registerMob(schema, map, "minecraft:wolf");
        schema.registerSimple(map, "minecraft:xp_bottle");
        schema.registerSimple(map, "minecraft:xp_orb");
        registerMob(schema, map, "minecraft:zombie");
        schema.register(map, "minecraft:zombie_horse", (s) -> {
            return DSL.optionalFields("SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerMob(schema, map, "minecraft:zombie_pigman");
        schema.register(map, "minecraft:zombie_villager", (s) -> {
            return DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(DataConverterTypes.VILLAGER_TRADE.in(schema))));
        });
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();

        registerInventory(schema, map, "minecraft:furnace");
        registerInventory(schema, map, "minecraft:chest");
        registerInventory(schema, map, "minecraft:trapped_chest");
        schema.registerSimple(map, "minecraft:ender_chest");
        schema.register(map, "minecraft:jukebox", (s) -> {
            return DSL.optionalFields("RecordItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerInventory(schema, map, "minecraft:dispenser");
        registerInventory(schema, map, "minecraft:dropper");
        schema.register(map, "minecraft:sign", () -> {
            return DataConverterSchemaV99.sign(schema);
        });
        schema.register(map, "minecraft:mob_spawner", (s) -> {
            return DataConverterTypes.UNTAGGED_SPAWNER.in(schema);
        });
        schema.register(map, "minecraft:piston", (s) -> {
            return DSL.optionalFields("blockState", DataConverterTypes.BLOCK_STATE.in(schema));
        });
        registerInventory(schema, map, "minecraft:brewing_stand");
        schema.register(map, "minecraft:enchanting_table", () -> {
            return V1458.nameable(schema);
        });
        schema.registerSimple(map, "minecraft:end_portal");
        schema.register(map, "minecraft:beacon", () -> {
            return V1458.nameable(schema);
        });
        schema.register(map, "minecraft:skull", () -> {
            return DSL.optionalFields("custom_name", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerSimple(map, "minecraft:daylight_detector");
        registerInventory(schema, map, "minecraft:hopper");
        schema.registerSimple(map, "minecraft:comparator");
        schema.register(map, "minecraft:banner", () -> {
            return V1458.nameable(schema);
        });
        schema.registerSimple(map, "minecraft:structure_block");
        schema.registerSimple(map, "minecraft:end_gateway");
        schema.register(map, "minecraft:command_block", () -> {
            return DSL.optionalFields("LastOutput", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        registerInventory(schema, map, "minecraft:shulker_box");
        schema.registerSimple(map, "minecraft:bed");
        return map;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        schema.registerType(false, DataConverterTypes.LEVEL, () -> {
            return DSL.optionalFields("CustomBossEvents", DSL.compoundList(DSL.optionalFields("Name", DataConverterTypes.TEXT_COMPONENT.in(schema))));
        });
        schema.registerType(false, DataConverterTypes.RECIPE, () -> {
            return DSL.constType(namespacedString());
        });
        schema.registerType(false, DataConverterTypes.PLAYER, () -> {
            return DSL.optionalFields(new Pair[]{Pair.of("RootVehicle", DSL.optionalFields("Entity", DataConverterTypes.ENTITY_TREE.in(schema))), Pair.of("ender_pearls", DSL.list(DataConverterTypes.ENTITY_TREE.in(schema))), Pair.of("Inventory", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("EnderItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("ShoulderEntityLeft", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("ShoulderEntityRight", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("recipeBook", DSL.optionalFields("recipes", DSL.list(DataConverterTypes.RECIPE.in(schema)), "toBeDisplayed", DSL.list(DataConverterTypes.RECIPE.in(schema))))});
        });
        schema.registerType(false, DataConverterTypes.CHUNK, () -> {
            return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(DataConverterTypes.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(DataConverterTypes.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", DataConverterTypes.BLOCK_NAME.in(schema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(DataConverterTypes.BLOCK_STATE.in(schema))))));
        });
        schema.registerType(true, DataConverterTypes.BLOCK_ENTITY, () -> {
            return DSL.optionalFields("components", DataConverterTypes.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", namespacedString(), map1));
        });
        schema.registerType(true, DataConverterTypes.ENTITY_TREE, () -> {
            return DSL.optionalFields("Passengers", DSL.list(DataConverterTypes.ENTITY_TREE.in(schema)), DataConverterTypes.ENTITY.in(schema));
        });
        schema.registerType(true, DataConverterTypes.ENTITY, () -> {
            return DSL.and(DataConverterTypes.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", DataConverterTypes.TEXT_COMPONENT.in(schema), DSL.taggedChoiceLazy("id", namespacedString(), map)));
        });
        schema.registerType(true, DataConverterTypes.ITEM_STACK, () -> {
            return DSL.hook(DSL.optionalFields("id", DataConverterTypes.ITEM_NAME.in(schema), "tag", DataConverterSchemaV99.itemStackTag(schema)), DataConverterSchemaV705.ADD_NAMES, HookFunction.IDENTITY);
        });
        schema.registerType(false, DataConverterTypes.HOTBAR, () -> {
            return DSL.compoundList(DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        schema.registerType(false, DataConverterTypes.OPTIONS, DSL::remainder);
        schema.registerType(false, DataConverterTypes.STRUCTURE, () -> {
            return DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", DataConverterTypes.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", DataConverterTypes.BLOCK_ENTITY.in(schema))), "palette", DSL.list(DataConverterTypes.BLOCK_STATE.in(schema)));
        });
        schema.registerType(false, DataConverterTypes.BLOCK_NAME, () -> {
            return DSL.constType(namespacedString());
        });
        schema.registerType(false, DataConverterTypes.ITEM_NAME, () -> {
            return DSL.constType(namespacedString());
        });
        schema.registerType(false, DataConverterTypes.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, DataConverterTypes.FLAT_BLOCK_STATE, DSL::remainder);
        Supplier<TypeTemplate> supplier = () -> {
            return DSL.compoundList(DataConverterTypes.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
        };

        schema.registerType(false, DataConverterTypes.STATS, () -> {
            return DSL.optionalFields("stats", DSL.optionalFields(new Pair[]{Pair.of("minecraft:mined", DSL.compoundList(DataConverterTypes.BLOCK_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:crafted", (TypeTemplate) supplier.get()), Pair.of("minecraft:used", (TypeTemplate) supplier.get()), Pair.of("minecraft:broken", (TypeTemplate) supplier.get()), Pair.of("minecraft:picked_up", (TypeTemplate) supplier.get()), Pair.of("minecraft:dropped", (TypeTemplate) supplier.get()), Pair.of("minecraft:killed", DSL.compoundList(DataConverterTypes.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:killed_by", DSL.compoundList(DataConverterTypes.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType())))}));
        });
        schema.registerType(false, DataConverterTypes.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
        schema.registerType(false, DataConverterTypes.SAVED_DATA_TICKETS, DSL::remainder);
        schema.registerType(false, DataConverterTypes.SAVED_DATA_MAP_DATA, () -> {
            return DSL.optionalFields("banners", DSL.list(DSL.optionalFields("Name", DataConverterTypes.TEXT_COMPONENT.in(schema))));
        });
        schema.registerType(false, DataConverterTypes.SAVED_DATA_MAP_INDEX, DSL::remainder);
        schema.registerType(false, DataConverterTypes.SAVED_DATA_RAIDS, DSL::remainder);
        schema.registerType(false, DataConverterTypes.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
        schema.registerType(false, DataConverterTypes.SAVED_DATA_SCOREBOARD, () -> {
            return DSL.optionalFields("data", DSL.optionalFields("Objectives", DSL.list(DataConverterTypes.OBJECTIVE.in(schema)), "Teams", DSL.list(DataConverterTypes.TEAM.in(schema)), "PlayerScores", DSL.list(DSL.optionalFields("display", DataConverterTypes.TEXT_COMPONENT.in(schema)))));
        });
        schema.registerType(false, DataConverterTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, () -> {
            return DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(DataConverterTypes.STRUCTURE_FEATURE.in(schema))));
        });
        schema.registerType(false, DataConverterTypes.STRUCTURE_FEATURE, DSL::remainder);
        Map<String, Supplier<TypeTemplate>> map2 = DataConverterSchemaV1451_6.createCriterionTypes(schema);

        schema.registerType(false, DataConverterTypes.OBJECTIVE, () -> {
            return DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map2), "DisplayName", DataConverterTypes.TEXT_COMPONENT.in(schema)), DataConverterSchemaV1451_6.UNPACK_OBJECTIVE_ID, DataConverterSchemaV1451_6.REPACK_OBJECTIVE_ID);
        });
        schema.registerType(false, DataConverterTypes.TEAM, () -> {
            return DSL.optionalFields("MemberNamePrefix", DataConverterTypes.TEXT_COMPONENT.in(schema), "MemberNameSuffix", DataConverterTypes.TEXT_COMPONENT.in(schema), "DisplayName", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerType(true, DataConverterTypes.UNTAGGED_SPAWNER, () -> {
            return DSL.optionalFields("SpawnPotentials", DSL.list(DSL.fields("Entity", DataConverterTypes.ENTITY_TREE.in(schema))), "SpawnData", DataConverterTypes.ENTITY_TREE.in(schema));
        });
        schema.registerType(false, DataConverterTypes.ADVANCEMENTS, () -> {
            return DSL.optionalFields("minecraft:adventure/adventuring_time", DSL.optionalFields("criteria", DSL.compoundList(DataConverterTypes.BIOME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_a_mob", DSL.optionalFields("criteria", DSL.compoundList(DataConverterTypes.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_all_mobs", DSL.optionalFields("criteria", DSL.compoundList(DataConverterTypes.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:husbandry/bred_all_animals", DSL.optionalFields("criteria", DSL.compoundList(DataConverterTypes.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))));
        });
        schema.registerType(false, DataConverterTypes.BIOME, () -> {
            return DSL.constType(namespacedString());
        });
        schema.registerType(false, DataConverterTypes.ENTITY_NAME, () -> {
            return DSL.constType(namespacedString());
        });
        schema.registerType(false, DataConverterTypes.POI_CHUNK, DSL::remainder);
        schema.registerType(false, DataConverterTypes.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, DataConverterTypes.ENTITY_CHUNK, () -> {
            return DSL.optionalFields("Entities", DSL.list(DataConverterTypes.ENTITY_TREE.in(schema)));
        });
        schema.registerType(true, DataConverterTypes.DATA_COMPONENTS, DSL::remainder);
        schema.registerType(true, DataConverterTypes.VILLAGER_TRADE, () -> {
            return DSL.optionalFields("buy", DataConverterTypes.ITEM_STACK.in(schema), "buyB", DataConverterTypes.ITEM_STACK.in(schema), "sell", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerType(true, DataConverterTypes.PARTICLE, () -> {
            return DSL.constType(DSL.string());
        });
        schema.registerType(true, DataConverterTypes.TEXT_COMPONENT, () -> {
            return DSL.constType(DSL.string());
        });
        schema.registerType(true, DataConverterTypes.ENTITY_EQUIPMENT, () -> {
            return DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)))), new TypeTemplate[]{DSL.optional(DSL.field("HandItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)))), DSL.optional(DSL.field("body_armor_item", DataConverterTypes.ITEM_STACK.in(schema))), DSL.optional(DSL.field("saddle", DataConverterTypes.ITEM_STACK.in(schema)))});
        });
    }
}
