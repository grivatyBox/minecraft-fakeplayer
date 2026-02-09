package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.DataConverterTypes;
import org.slf4j.Logger;

public class DataConverterSchemaV99 extends Schema {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final Map<String, String> ITEM_TO_BLOCKENTITY = (Map) DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
        hashmap.put("minecraft:furnace", "Furnace");
        hashmap.put("minecraft:lit_furnace", "Furnace");
        hashmap.put("minecraft:chest", "Chest");
        hashmap.put("minecraft:trapped_chest", "Chest");
        hashmap.put("minecraft:ender_chest", "EnderChest");
        hashmap.put("minecraft:jukebox", "RecordPlayer");
        hashmap.put("minecraft:dispenser", "Trap");
        hashmap.put("minecraft:dropper", "Dropper");
        hashmap.put("minecraft:sign", "Sign");
        hashmap.put("minecraft:mob_spawner", "MobSpawner");
        hashmap.put("minecraft:noteblock", "Music");
        hashmap.put("minecraft:brewing_stand", "Cauldron");
        hashmap.put("minecraft:enhanting_table", "EnchantTable");
        hashmap.put("minecraft:command_block", "CommandBlock");
        hashmap.put("minecraft:beacon", "Beacon");
        hashmap.put("minecraft:skull", "Skull");
        hashmap.put("minecraft:daylight_detector", "DLDetector");
        hashmap.put("minecraft:hopper", "Hopper");
        hashmap.put("minecraft:banner", "Banner");
        hashmap.put("minecraft:flower_pot", "FlowerPot");
        hashmap.put("minecraft:repeating_command_block", "CommandBlock");
        hashmap.put("minecraft:chain_command_block", "CommandBlock");
        hashmap.put("minecraft:standing_sign", "Sign");
        hashmap.put("minecraft:wall_sign", "Sign");
        hashmap.put("minecraft:piston_head", "Piston");
        hashmap.put("minecraft:daylight_detector_inverted", "DLDetector");
        hashmap.put("minecraft:unpowered_comparator", "Comparator");
        hashmap.put("minecraft:powered_comparator", "Comparator");
        hashmap.put("minecraft:wall_banner", "Banner");
        hashmap.put("minecraft:standing_banner", "Banner");
        hashmap.put("minecraft:structure_block", "Structure");
        hashmap.put("minecraft:end_portal", "Airportal");
        hashmap.put("minecraft:end_gateway", "EndGateway");
        hashmap.put("minecraft:shield", "Banner");
    });
    public static final Map<String, String> ITEM_TO_ENTITY = Map.of("minecraft:armor_stand", "ArmorStand", "minecraft:painting", "Painting");
    protected static final HookFunction ADD_NAMES = new HookFunction() {
        public <T> T apply(DynamicOps<T> dynamicops, T t0) {
            return (T) DataConverterSchemaV99.addNames(new Dynamic(dynamicops, t0), DataConverterSchemaV99.ITEM_TO_BLOCKENTITY, DataConverterSchemaV99.ITEM_TO_ENTITY);
        }
    };

    public DataConverterSchemaV99(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.register(map, s, () -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
    }

    protected static void registerMinecart(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.register(map, s, () -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
        schema.register(map, s, () -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();

        schema.register(map, "Item", (s) -> {
            return DSL.optionalFields("Item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "XPOrb");
        registerThrowableProjectile(schema, map, "ThrownEgg");
        schema.registerSimple(map, "LeashKnot");
        schema.registerSimple(map, "Painting");
        schema.register(map, "Arrow", (s) -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        schema.register(map, "TippedArrow", (s) -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        schema.register(map, "SpectralArrow", (s) -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        registerThrowableProjectile(schema, map, "Snowball");
        registerThrowableProjectile(schema, map, "Fireball");
        registerThrowableProjectile(schema, map, "SmallFireball");
        registerThrowableProjectile(schema, map, "ThrownEnderpearl");
        schema.registerSimple(map, "EyeOfEnderSignal");
        schema.register(map, "ThrownPotion", (s) -> {
            return DSL.optionalFields("inTile", DataConverterTypes.BLOCK_NAME.in(schema), "Potion", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerThrowableProjectile(schema, map, "ThrownExpBottle");
        schema.register(map, "ItemFrame", (s) -> {
            return DSL.optionalFields("Item", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerThrowableProjectile(schema, map, "WitherSkull");
        schema.registerSimple(map, "PrimedTnt");
        schema.register(map, "FallingSand", (s) -> {
            return DSL.optionalFields("Block", DataConverterTypes.BLOCK_NAME.in(schema), "TileEntityData", DataConverterTypes.BLOCK_ENTITY.in(schema));
        });
        schema.register(map, "FireworksRocketEntity", (s) -> {
            return DSL.optionalFields("FireworksItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "Boat");
        schema.register(map, "Minecart", () -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        registerMinecart(schema, map, "MinecartRideable");
        schema.register(map, "MinecartChest", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        registerMinecart(schema, map, "MinecartFurnace");
        registerMinecart(schema, map, "MinecartTNT");
        schema.register(map, "MinecartSpawner", () -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), DataConverterTypes.UNTAGGED_SPAWNER.in(schema));
        });
        schema.register(map, "MinecartHopper", (s) -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        schema.register(map, "MinecartCommandBlock", () -> {
            return DSL.optionalFields("DisplayTile", DataConverterTypes.BLOCK_NAME.in(schema), "LastOutput", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerSimple(map, "ArmorStand");
        schema.registerSimple(map, "Creeper");
        schema.registerSimple(map, "Skeleton");
        schema.registerSimple(map, "Spider");
        schema.registerSimple(map, "Giant");
        schema.registerSimple(map, "Zombie");
        schema.registerSimple(map, "Slime");
        schema.registerSimple(map, "Ghast");
        schema.registerSimple(map, "PigZombie");
        schema.register(map, "Enderman", (s) -> {
            return DSL.optionalFields("carried", DataConverterTypes.BLOCK_NAME.in(schema));
        });
        schema.registerSimple(map, "CaveSpider");
        schema.registerSimple(map, "Silverfish");
        schema.registerSimple(map, "Blaze");
        schema.registerSimple(map, "LavaSlime");
        schema.registerSimple(map, "EnderDragon");
        schema.registerSimple(map, "WitherBoss");
        schema.registerSimple(map, "Bat");
        schema.registerSimple(map, "Witch");
        schema.registerSimple(map, "Endermite");
        schema.registerSimple(map, "Guardian");
        schema.registerSimple(map, "Pig");
        schema.registerSimple(map, "Sheep");
        schema.registerSimple(map, "Cow");
        schema.registerSimple(map, "Chicken");
        schema.registerSimple(map, "Squid");
        schema.registerSimple(map, "Wolf");
        schema.registerSimple(map, "MushroomCow");
        schema.registerSimple(map, "SnowMan");
        schema.registerSimple(map, "Ozelot");
        schema.registerSimple(map, "VillagerGolem");
        schema.register(map, "EntityHorse", (s) -> {
            return DSL.optionalFields("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "ArmorItem", DataConverterTypes.ITEM_STACK.in(schema), "SaddleItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        schema.registerSimple(map, "Rabbit");
        schema.register(map, "Villager", (s) -> {
            return DSL.optionalFields("Inventory", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DataConverterTypes.VILLAGER_TRADE.in(schema))));
        });
        schema.registerSimple(map, "EnderCrystal");
        schema.register(map, "AreaEffectCloud", (s) -> {
            return DSL.optionalFields("Particle", DataConverterTypes.PARTICLE.in(schema));
        });
        schema.registerSimple(map, "ShulkerBullet");
        schema.registerSimple(map, "DragonFireball");
        schema.registerSimple(map, "Shulker");
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();

        registerInventory(schema, map, "Furnace");
        registerInventory(schema, map, "Chest");
        schema.registerSimple(map, "EnderChest");
        schema.register(map, "RecordPlayer", (s) -> {
            return DSL.optionalFields("RecordItem", DataConverterTypes.ITEM_STACK.in(schema));
        });
        registerInventory(schema, map, "Trap");
        registerInventory(schema, map, "Dropper");
        schema.register(map, "Sign", () -> {
            return sign(schema);
        });
        schema.register(map, "MobSpawner", (s) -> {
            return DataConverterTypes.UNTAGGED_SPAWNER.in(schema);
        });
        schema.registerSimple(map, "Music");
        schema.registerSimple(map, "Piston");
        registerInventory(schema, map, "Cauldron");
        schema.registerSimple(map, "EnchantTable");
        schema.registerSimple(map, "Airportal");
        schema.register(map, "Control", () -> {
            return DSL.optionalFields("LastOutput", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerSimple(map, "Beacon");
        schema.register(map, "Skull", () -> {
            return DSL.optionalFields("custom_name", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerSimple(map, "DLDetector");
        registerInventory(schema, map, "Hopper");
        schema.registerSimple(map, "Comparator");
        schema.register(map, "FlowerPot", (s) -> {
            return DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), DataConverterTypes.ITEM_NAME.in(schema)));
        });
        schema.register(map, "Banner", () -> {
            return DSL.optionalFields("CustomName", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerSimple(map, "Structure");
        schema.registerSimple(map, "EndGateway");
        return map;
    }

    public static TypeTemplate sign(Schema schema) {
        return DSL.optionalFields(new Pair[]{Pair.of("Text1", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("Text2", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("Text3", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("Text4", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText1", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText2", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText3", DataConverterTypes.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText4", DataConverterTypes.TEXT_COMPONENT.in(schema))});
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
        schema.registerType(false, DataConverterTypes.LEVEL, () -> {
            return DSL.optionalFields("CustomBossEvents", DSL.compoundList(DSL.optionalFields("Name", DataConverterTypes.TEXT_COMPONENT.in(schema))));
        });
        schema.registerType(false, DataConverterTypes.PLAYER, () -> {
            return DSL.optionalFields("Inventory", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)), "EnderItems", DSL.list(DataConverterTypes.ITEM_STACK.in(schema)));
        });
        schema.registerType(false, DataConverterTypes.CHUNK, () -> {
            return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(DataConverterTypes.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(DataConverterTypes.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", DataConverterTypes.BLOCK_NAME.in(schema)))));
        });
        schema.registerType(true, DataConverterTypes.BLOCK_ENTITY, () -> {
            return DSL.optionalFields("components", DataConverterTypes.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", DSL.string(), map1));
        });
        schema.registerType(true, DataConverterTypes.ENTITY_TREE, () -> {
            return DSL.optionalFields("Riding", DataConverterTypes.ENTITY_TREE.in(schema), DataConverterTypes.ENTITY.in(schema));
        });
        schema.registerType(false, DataConverterTypes.ENTITY_NAME, () -> {
            return DSL.constType(DataConverterSchemaNamed.namespacedString());
        });
        schema.registerType(true, DataConverterTypes.ENTITY, () -> {
            return DSL.and(DataConverterTypes.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", DSL.constType(DSL.string()), DSL.taggedChoiceLazy("id", DSL.string(), map)));
        });
        schema.registerType(true, DataConverterTypes.ITEM_STACK, () -> {
            return DSL.hook(DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), DataConverterTypes.ITEM_NAME.in(schema)), "tag", itemStackTag(schema)), DataConverterSchemaV99.ADD_NAMES, HookFunction.IDENTITY);
        });
        schema.registerType(false, DataConverterTypes.OPTIONS, DSL::remainder);
        schema.registerType(false, DataConverterTypes.BLOCK_NAME, () -> {
            return DSL.or(DSL.constType(DSL.intType()), DSL.constType(DataConverterSchemaNamed.namespacedString()));
        });
        schema.registerType(false, DataConverterTypes.ITEM_NAME, () -> {
            return DSL.constType(DataConverterSchemaNamed.namespacedString());
        });
        schema.registerType(false, DataConverterTypes.STATS, DSL::remainder);
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
        schema.registerType(false, DataConverterTypes.OBJECTIVE, DSL::remainder);
        schema.registerType(false, DataConverterTypes.TEAM, () -> {
            return DSL.optionalFields("MemberNamePrefix", DataConverterTypes.TEXT_COMPONENT.in(schema), "MemberNameSuffix", DataConverterTypes.TEXT_COMPONENT.in(schema), "DisplayName", DataConverterTypes.TEXT_COMPONENT.in(schema));
        });
        schema.registerType(true, DataConverterTypes.UNTAGGED_SPAWNER, DSL::remainder);
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
        schema.registerType(false, DataConverterTypes.STRUCTURE, () -> {
            return DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", DataConverterTypes.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", DataConverterTypes.BLOCK_ENTITY.in(schema))), "palette", DSL.list(DataConverterTypes.BLOCK_STATE.in(schema)));
        });
        schema.registerType(false, DataConverterTypes.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, DataConverterTypes.FLAT_BLOCK_STATE, DSL::remainder);
        schema.registerType(true, DataConverterTypes.ENTITY_EQUIPMENT, () -> {
            return DSL.optional(DSL.field("Equipment", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))));
        });
    }

    public static TypeTemplate itemStackTag(Schema schema) {
        return DSL.optionalFields(new Pair[]{Pair.of("EntityTag", DataConverterTypes.ENTITY_TREE.in(schema)), Pair.of("BlockEntityTag", DataConverterTypes.BLOCK_ENTITY.in(schema)), Pair.of("CanDestroy", DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))), Pair.of("CanPlaceOn", DSL.list(DataConverterTypes.BLOCK_NAME.in(schema))), Pair.of("Items", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("ChargedProjectiles", DSL.list(DataConverterTypes.ITEM_STACK.in(schema))), Pair.of("pages", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema))), Pair.of("filtered_pages", DSL.compoundList(DataConverterTypes.TEXT_COMPONENT.in(schema))), Pair.of("display", DSL.optionalFields("Name", DataConverterTypes.TEXT_COMPONENT.in(schema), "Lore", DSL.list(DataConverterTypes.TEXT_COMPONENT.in(schema))))});
    }

    protected static <T> T addNames(Dynamic<T> dynamic, Map<String, String> map, Map<String, String> map1) {
        return (T) dynamic.update("tag", (dynamic1) -> {
            return dynamic1.update("BlockEntityTag", (dynamic2) -> {
                String s = (String) dynamic.get("id").asString().result().map(DataConverterSchemaNamed::ensureNamespaced).orElse("minecraft:air");

                if (!"minecraft:air".equals(s)) {
                    String s1 = (String) map.get(s);

                    if (s1 != null) {
                        return dynamic2.set("id", dynamic.createString(s1));
                    }

                    DataConverterSchemaV99.LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", s);
                }

                return dynamic2;
            }).update("EntityTag", (dynamic2) -> {
                if (dynamic2.get("id").result().isPresent()) {
                    return dynamic2;
                } else {
                    String s = DataConverterSchemaNamed.ensureNamespaced(dynamic.get("id").asString(""));
                    String s1 = (String) map1.get(s);

                    return s1 != null ? dynamic2.set("id", dynamic.createString(s1)) : dynamic2;
                }
            });
        }).getValue();
    }
}
