package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagKey;
import net.minecraft.util.EncoderCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.ChestLock;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.ChickenVariant;
import net.minecraft.world.entity.animal.CowVariant;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.entity.animal.EntityMushroomCow;
import net.minecraft.world.entity.animal.EntityParrot;
import net.minecraft.world.entity.animal.EntityRabbit;
import net.minecraft.world.entity.animal.EntitySalmon;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.horse.EntityLlama;
import net.minecraft.world.entity.animal.horse.HorseColor;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.food.FoodInfo;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.EnumItemRarity;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.EnumBannerPatternType;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;

public class DataComponents {

    static final EncoderCache ENCODER_CACHE = new EncoderCache(512);
    public static final DataComponentType<CustomData> CUSTOM_DATA = register("custom_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC);
    });
    public static final DataComponentType<Integer> MAX_STACK_SIZE = register("max_stack_size", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.intRange(1, 99)).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Integer> MAX_DAMAGE = register("max_damage", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Integer> DAMAGE = register("damage", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Unit> UNBREAKABLE = register("unbreakable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC);
    });
    public static final DataComponentType<IChatBaseComponent> CUSTOM_NAME = register("custom_name", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<IChatBaseComponent> ITEM_NAME = register("item_name", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<MinecraftKey> ITEM_MODEL = register("item_model", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MinecraftKey.CODEC).networkSynchronized(MinecraftKey.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemLore> LORE = register("lore", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemLore.CODEC).networkSynchronized(ItemLore.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<EnumItemRarity> RARITY = register("rarity", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumItemRarity.CODEC).networkSynchronized(EnumItemRarity.STREAM_CODEC);
    });
    public static final DataComponentType<ItemEnchantments> ENCHANTMENTS = register("enchantments", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<AdventureModePredicate> CAN_PLACE_ON = register("can_place_on", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<AdventureModePredicate> CAN_BREAK = register("can_break", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemAttributeModifiers> ATTRIBUTE_MODIFIERS = register("attribute_modifiers", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemAttributeModifiers.CODEC).networkSynchronized(ItemAttributeModifiers.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<CustomModelData> CUSTOM_MODEL_DATA = register("custom_model_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomModelData.CODEC).networkSynchronized(CustomModelData.STREAM_CODEC);
    });
    public static final DataComponentType<TooltipDisplay> TOOLTIP_DISPLAY = register("tooltip_display", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(TooltipDisplay.CODEC).networkSynchronized(TooltipDisplay.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Integer> REPAIR_COST = register("repair_cost", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT);
    });
    public static final DataComponentType<Unit> CREATIVE_SLOT_LOCK = register("creative_slot_lock", (datacomponenttype_a) -> {
        return datacomponenttype_a.networkSynchronized(Unit.STREAM_CODEC);
    });
    public static final DataComponentType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL);
    });
    public static final DataComponentType<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Unit.CODEC);
    });
    public static final DataComponentType<FoodInfo> FOOD = register("food", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(FoodInfo.DIRECT_CODEC).networkSynchronized(FoodInfo.DIRECT_STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Consumable> CONSUMABLE = register("consumable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Consumable.CODEC).networkSynchronized(Consumable.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<UseRemainder> USE_REMAINDER = register("use_remainder", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(UseRemainder.CODEC).networkSynchronized(UseRemainder.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<UseCooldown> USE_COOLDOWN = register("use_cooldown", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(UseCooldown.CODEC).networkSynchronized(UseCooldown.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<DamageResistant> DAMAGE_RESISTANT = register("damage_resistant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(DamageResistant.CODEC).networkSynchronized(DamageResistant.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Tool> TOOL = register("tool", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Tool.CODEC).networkSynchronized(Tool.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Weapon> WEAPON = register("weapon", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Weapon.CODEC).networkSynchronized(Weapon.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Enchantable> ENCHANTABLE = register("enchantable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Enchantable.CODEC).networkSynchronized(Enchantable.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Equippable> EQUIPPABLE = register("equippable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Equippable.CODEC).networkSynchronized(Equippable.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Repairable> REPAIRABLE = register("repairable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Repairable.CODEC).networkSynchronized(Repairable.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Unit> GLIDER = register("glider", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC);
    });
    public static final DataComponentType<MinecraftKey> TOOLTIP_STYLE = register("tooltip_style", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MinecraftKey.CODEC).networkSynchronized(MinecraftKey.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<DeathProtection> DEATH_PROTECTION = register("death_protection", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(DeathProtection.CODEC).networkSynchronized(DeathProtection.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<BlocksAttacks> BLOCKS_ATTACKS = register("blocks_attacks", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BlocksAttacks.CODEC).networkSynchronized(BlocksAttacks.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemEnchantments> STORED_ENCHANTMENTS = register("stored_enchantments", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<DyedItemColor> DYED_COLOR = register("dyed_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC);
    });
    public static final DataComponentType<MapItemColor> MAP_COLOR = register("map_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MapItemColor.CODEC).networkSynchronized(MapItemColor.STREAM_CODEC);
    });
    public static final DataComponentType<MapId> MAP_ID = register("map_id", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MapId.CODEC).networkSynchronized(MapId.STREAM_CODEC);
    });
    public static final DataComponentType<MapDecorations> MAP_DECORATIONS = register("map_decorations", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MapDecorations.CODEC).cacheEncoding();
    });
    public static final DataComponentType<MapPostProcessing> MAP_POST_PROCESSING = register("map_post_processing", (datacomponenttype_a) -> {
        return datacomponenttype_a.networkSynchronized(MapPostProcessing.STREAM_CODEC);
    });
    public static final DataComponentType<ChargedProjectiles> CHARGED_PROJECTILES = register("charged_projectiles", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ChargedProjectiles.CODEC).networkSynchronized(ChargedProjectiles.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<BundleContents> BUNDLE_CONTENTS = register("bundle_contents", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BundleContents.CODEC).networkSynchronized(BundleContents.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<PotionContents> POTION_CONTENTS = register("potion_contents", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(PotionContents.CODEC).networkSynchronized(PotionContents.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Float> POTION_DURATION_SCALE = register("potion_duration_scale", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ExtraCodecs.NON_NEGATIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT).cacheEncoding();
    });
    public static final DataComponentType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register("suspicious_stew_effects", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(SuspiciousStewEffects.CODEC).networkSynchronized(SuspiciousStewEffects.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT = register("writable_book_content", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(WritableBookContent.CODEC).networkSynchronized(WritableBookContent.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT = register("written_book_content", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(WrittenBookContent.CODEC).networkSynchronized(WrittenBookContent.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ArmorTrim> TRIM = register("trim", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ArmorTrim.CODEC).networkSynchronized(ArmorTrim.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<DebugStickState> DEBUG_STICK_STATE = register("debug_stick_state", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(DebugStickState.CODEC).cacheEncoding();
    });
    public static final DataComponentType<CustomData> ENTITY_DATA = register("entity_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC);
    });
    public static final DataComponentType<CustomData> BUCKET_ENTITY_DATA = register("bucket_entity_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC);
    });
    public static final DataComponentType<CustomData> BLOCK_ENTITY_DATA = register("block_entity_data", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC);
    });
    public static final DataComponentType<InstrumentComponent> INSTRUMENT = register("instrument", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(InstrumentComponent.CODEC).networkSynchronized(InstrumentComponent.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ProvidesTrimMaterial> PROVIDES_TRIM_MATERIAL = register("provides_trim_material", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ProvidesTrimMaterial.CODEC).networkSynchronized(ProvidesTrimMaterial.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<OminousBottleAmplifier> OMINOUS_BOTTLE_AMPLIFIER = register("ominous_bottle_amplifier", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(OminousBottleAmplifier.CODEC).networkSynchronized(OminousBottleAmplifier.STREAM_CODEC);
    });
    public static final DataComponentType<JukeboxPlayable> JUKEBOX_PLAYABLE = register("jukebox_playable", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(JukeboxPlayable.CODEC).networkSynchronized(JukeboxPlayable.STREAM_CODEC);
    });
    public static final DataComponentType<TagKey<EnumBannerPatternType>> PROVIDES_BANNER_PATTERNS = register("provides_banner_patterns", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(TagKey.hashedCodec(Registries.BANNER_PATTERN)).networkSynchronized(TagKey.streamCodec(Registries.BANNER_PATTERN)).cacheEncoding();
    });
    public static final DataComponentType<List<ResourceKey<IRecipe<?>>>> RECIPES = register("recipes", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(IRecipe.KEY_CODEC.listOf()).cacheEncoding();
    });
    public static final DataComponentType<LodestoneTracker> LODESTONE_TRACKER = register("lodestone_tracker", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(LodestoneTracker.CODEC).networkSynchronized(LodestoneTracker.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<FireworkExplosion> FIREWORK_EXPLOSION = register("firework_explosion", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(FireworkExplosion.CODEC).networkSynchronized(FireworkExplosion.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Fireworks> FIREWORKS = register("fireworks", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Fireworks.CODEC).networkSynchronized(Fireworks.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ResolvableProfile> PROFILE = register("profile", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ResolvableProfile.CODEC).networkSynchronized(ResolvableProfile.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<MinecraftKey> NOTE_BLOCK_SOUND = register("note_block_sound", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(MinecraftKey.CODEC).networkSynchronized(MinecraftKey.STREAM_CODEC);
    });
    public static final DataComponentType<BannerPatternLayers> BANNER_PATTERNS = register("banner_patterns", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BannerPatternLayers.CODEC).networkSynchronized(BannerPatternLayers.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<EnumColor> BASE_COLOR = register("base_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<PotDecorations> POT_DECORATIONS = register("pot_decorations", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(PotDecorations.CODEC).networkSynchronized(PotDecorations.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ItemContainerContents> CONTAINER = register("container", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<BlockItemStateProperties> BLOCK_STATE = register("block_state", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(BlockItemStateProperties.CODEC).networkSynchronized(BlockItemStateProperties.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Bees> BEES = register("bees", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Bees.CODEC).networkSynchronized(Bees.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<ChestLock> LOCK = register("lock", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ChestLock.CODEC);
    });
    public static final DataComponentType<SeededContainerLoot> CONTAINER_LOOT = register("container_loot", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(SeededContainerLoot.CODEC);
    });
    public static final DataComponentType<Holder<SoundEffect>> BREAK_SOUND = register("break_sound", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(SoundEffect.CODEC).networkSynchronized(SoundEffect.STREAM_CODEC).cacheEncoding();
    });
    public static final DataComponentType<Holder<VillagerType>> VILLAGER_VARIANT = register("villager/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(VillagerType.CODEC).networkSynchronized(VillagerType.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<WolfVariant>> WOLF_VARIANT = register("wolf/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(WolfVariant.CODEC).networkSynchronized(WolfVariant.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<WolfSoundVariant>> WOLF_SOUND_VARIANT = register("wolf/sound_variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(WolfSoundVariant.CODEC).networkSynchronized(WolfSoundVariant.STREAM_CODEC);
    });
    public static final DataComponentType<EnumColor> WOLF_COLLAR = register("wolf/collar", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<EntityFox.Type> FOX_VARIANT = register("fox/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntityFox.Type.CODEC).networkSynchronized(EntityFox.Type.STREAM_CODEC);
    });
    public static final DataComponentType<EntitySalmon.Variant> SALMON_SIZE = register("salmon/size", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntitySalmon.Variant.CODEC).networkSynchronized(EntitySalmon.Variant.STREAM_CODEC);
    });
    public static final DataComponentType<EntityParrot.Variant> PARROT_VARIANT = register("parrot/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntityParrot.Variant.CODEC).networkSynchronized(EntityParrot.Variant.STREAM_CODEC);
    });
    public static final DataComponentType<EntityTropicalFish.Variant> TROPICAL_FISH_PATTERN = register("tropical_fish/pattern", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntityTropicalFish.Variant.CODEC).networkSynchronized(EntityTropicalFish.Variant.STREAM_CODEC);
    });
    public static final DataComponentType<EnumColor> TROPICAL_FISH_BASE_COLOR = register("tropical_fish/base_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<EnumColor> TROPICAL_FISH_PATTERN_COLOR = register("tropical_fish/pattern_color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<EntityMushroomCow.Type> MOOSHROOM_VARIANT = register("mooshroom/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntityMushroomCow.Type.CODEC).networkSynchronized(EntityMushroomCow.Type.STREAM_CODEC);
    });
    public static final DataComponentType<EntityRabbit.Variant> RABBIT_VARIANT = register("rabbit/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntityRabbit.Variant.CODEC).networkSynchronized(EntityRabbit.Variant.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<PigVariant>> PIG_VARIANT = register("pig/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(PigVariant.CODEC).networkSynchronized(PigVariant.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<CowVariant>> COW_VARIANT = register("cow/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CowVariant.CODEC).networkSynchronized(CowVariant.STREAM_CODEC);
    });
    public static final DataComponentType<EitherHolder<ChickenVariant>> CHICKEN_VARIANT = register("chicken/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EitherHolder.codec(Registries.CHICKEN_VARIANT, ChickenVariant.CODEC)).networkSynchronized(EitherHolder.streamCodec(Registries.CHICKEN_VARIANT, ChickenVariant.STREAM_CODEC));
    });
    public static final DataComponentType<Holder<FrogVariant>> FROG_VARIANT = register("frog/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(FrogVariant.CODEC).networkSynchronized(FrogVariant.STREAM_CODEC);
    });
    public static final DataComponentType<HorseColor> HORSE_VARIANT = register("horse/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(HorseColor.CODEC).networkSynchronized(HorseColor.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<PaintingVariant>> PAINTING_VARIANT = register("painting/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(PaintingVariant.CODEC).networkSynchronized(PaintingVariant.STREAM_CODEC);
    });
    public static final DataComponentType<EntityLlama.Variant> LLAMA_VARIANT = register("llama/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EntityLlama.Variant.CODEC).networkSynchronized(EntityLlama.Variant.STREAM_CODEC);
    });
    public static final DataComponentType<Axolotl.Variant> AXOLOTL_VARIANT = register("axolotl/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Axolotl.Variant.CODEC).networkSynchronized(Axolotl.Variant.STREAM_CODEC);
    });
    public static final DataComponentType<Holder<CatVariant>> CAT_VARIANT = register("cat/variant", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(CatVariant.CODEC).networkSynchronized(CatVariant.STREAM_CODEC);
    });
    public static final DataComponentType<EnumColor> CAT_COLLAR = register("cat/collar", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<EnumColor> SHEEP_COLOR = register("sheep/color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentType<EnumColor> SHULKER_COLOR = register("shulker/color", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnumColor.CODEC).networkSynchronized(EnumColor.STREAM_CODEC);
    });
    public static final DataComponentMap COMMON_ITEM_COMPONENTS = DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).set(DataComponents.LORE, ItemLore.EMPTY).set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).set(DataComponents.REPAIR_COST, 0).set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).set(DataComponents.RARITY, EnumItemRarity.COMMON).set(DataComponents.BREAK_SOUND, SoundEffects.ITEM_BREAK).set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).build();

    public DataComponents() {}

    public static DataComponentType<?> bootstrap(IRegistry<DataComponentType<?>> iregistry) {
        return DataComponents.CUSTOM_DATA;
    }

    private static <T> DataComponentType<T> register(String s, UnaryOperator<DataComponentType.a<T>> unaryoperator) {
        return (DataComponentType) IRegistry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, s, ((DataComponentType.a) unaryoperator.apply(DataComponentType.builder())).build());
    }
}
