package net.minecraft.world.level.storage.loot.parameters;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public class LootContextParameters {

    public static final ContextKey<Entity> THIS_ENTITY = ContextKey.<Entity>vanilla("this_entity");
    public static final ContextKey<EntityHuman> LAST_DAMAGE_PLAYER = ContextKey.<EntityHuman>vanilla("last_damage_player");
    public static final ContextKey<DamageSource> DAMAGE_SOURCE = ContextKey.<DamageSource>vanilla("damage_source");
    public static final ContextKey<Entity> ATTACKING_ENTITY = ContextKey.<Entity>vanilla("attacking_entity");
    public static final ContextKey<Entity> DIRECT_ATTACKING_ENTITY = ContextKey.<Entity>vanilla("direct_attacking_entity");
    public static final ContextKey<Vec3D> ORIGIN = ContextKey.<Vec3D>vanilla("origin");
    public static final ContextKey<IBlockData> BLOCK_STATE = ContextKey.<IBlockData>vanilla("block_state");
    public static final ContextKey<TileEntity> BLOCK_ENTITY = ContextKey.<TileEntity>vanilla("block_entity");
    public static final ContextKey<ItemStack> TOOL = ContextKey.<ItemStack>vanilla("tool");
    public static final ContextKey<Float> EXPLOSION_RADIUS = ContextKey.<Float>vanilla("explosion_radius");
    public static final ContextKey<Integer> ENCHANTMENT_LEVEL = ContextKey.<Integer>vanilla("enchantment_level");
    public static final ContextKey<Boolean> ENCHANTMENT_ACTIVE = ContextKey.<Boolean>vanilla("enchantment_active");

    public LootContextParameters() {}
}
