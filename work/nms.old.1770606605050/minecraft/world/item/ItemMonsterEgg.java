package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFluids;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class ItemMonsterEgg extends Item {

    private static final Map<EntityTypes<?>, ItemMonsterEgg> BY_ID = Maps.newIdentityHashMap();

    public ItemMonsterEgg(Item.Info item_info) {
        super(item_info);
        TypedEntityData<EntityTypes<?>> typedentitydata = (TypedEntityData) this.components().get(DataComponents.ENTITY_DATA);

        if (typedentitydata != null) {
            ItemMonsterEgg.BY_ID.put(typedentitydata.type(), this);
        }

    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();

        if (!(world instanceof WorldServer worldserver)) {
            return EnumInteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = itemactioncontext.getItemInHand();
            BlockPosition blockposition = itemactioncontext.getClickedPos();
            EnumDirection enumdirection = itemactioncontext.getClickedFace();
            IBlockData iblockdata = world.getBlockState(blockposition);
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof Spawner spawner) {
                EntityTypes<?> entitytypes = this.getType(itemstack);

                if (entitytypes == null) {
                    return EnumInteractionResult.FAIL;
                } else if (!worldserver.getServer().isSpawnerBlockEnabled()) {
                    EntityHuman entityhuman = itemactioncontext.getPlayer();

                    if (entityhuman instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entityhuman;

                        entityplayer.sendSystemMessage(IChatBaseComponent.translatable("advMode.notEnabled.spawner"));
                    }

                    return EnumInteractionResult.FAIL;
                } else {
                    spawner.setEntityId(entitytypes, world.getRandom());
                    world.sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
                    world.gameEvent(itemactioncontext.getPlayer(), (Holder) GameEvent.BLOCK_CHANGE, blockposition);
                    itemstack.shrink(1);
                    return EnumInteractionResult.SUCCESS;
                }
            } else {
                BlockPosition blockposition1;

                if (iblockdata.getCollisionShape(world, blockposition).isEmpty()) {
                    blockposition1 = blockposition;
                } else {
                    blockposition1 = blockposition.relative(enumdirection);
                }

                return this.spawnMob(itemactioncontext.getPlayer(), itemstack, world, blockposition1, true, !Objects.equals(blockposition, blockposition1) && enumdirection == EnumDirection.UP);
            }
        }
    }

    private EnumInteractionResult spawnMob(@Nullable EntityLiving entityliving, ItemStack itemstack, World world, BlockPosition blockposition, boolean flag, boolean flag1) {
        EntityTypes<?> entitytypes = this.getType(itemstack);

        if (entitytypes == null) {
            return EnumInteractionResult.FAIL;
        } else if (!entitytypes.isAllowedInPeaceful() && world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return EnumInteractionResult.FAIL;
        } else {
            if (entitytypes.spawn((WorldServer) world, itemstack, entityliving, blockposition, EntitySpawnReason.SPAWN_ITEM_USE, flag, flag1) != null) {
                itemstack.consume(1, entityliving);
                world.gameEvent(entityliving, (Holder) GameEvent.ENTITY_PLACE, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, RayTrace.FluidCollisionOption.SOURCE_ONLY);

        if (movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return EnumInteractionResult.PASS;
        } else if (world instanceof WorldServer) {
            WorldServer worldserver = (WorldServer) world;
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

            if (!(world.getBlockState(blockposition).getBlock() instanceof BlockFluids)) {
                return EnumInteractionResult.PASS;
            } else if (world.mayInteract(entityhuman, blockposition) && entityhuman.mayUseItemAt(blockposition, movingobjectpositionblock.getDirection(), itemstack)) {
                EnumInteractionResult enuminteractionresult = this.spawnMob(entityhuman, itemstack, world, blockposition, false, false);

                if (enuminteractionresult == EnumInteractionResult.SUCCESS) {
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                }

                return enuminteractionresult;
            } else {
                return EnumInteractionResult.FAIL;
            }
        } else {
            return EnumInteractionResult.SUCCESS;
        }
    }

    public boolean spawnsEntity(ItemStack itemstack, EntityTypes<?> entitytypes) {
        return Objects.equals(this.getType(itemstack), entitytypes);
    }

    @Nullable
    public static ItemMonsterEgg byId(@Nullable EntityTypes<?> entitytypes) {
        return (ItemMonsterEgg) ItemMonsterEgg.BY_ID.get(entitytypes);
    }

    public static Iterable<ItemMonsterEgg> eggs() {
        return Iterables.unmodifiableIterable(ItemMonsterEgg.BY_ID.values());
    }

    @Nullable
    public EntityTypes<?> getType(ItemStack itemstack) {
        TypedEntityData<EntityTypes<?>> typedentitydata = (TypedEntityData) itemstack.get(DataComponents.ENTITY_DATA);

        return typedentitydata != null ? (EntityTypes) typedentitydata.type() : null;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return (FeatureFlagSet) Optional.ofNullable(this.components().get(DataComponents.ENTITY_DATA)).map(TypedEntityData::type).map(EntityTypes::requiredFeatures).orElseGet(FeatureFlagSet::of); // CraftBukkit - decompile error
    }

    public Optional<EntityInsentient> spawnOffspringFromSpawnEgg(EntityHuman entityhuman, EntityInsentient entityinsentient, EntityTypes<? extends EntityInsentient> entitytypes, WorldServer worldserver, Vec3D vec3d, ItemStack itemstack) {
        if (!this.spawnsEntity(itemstack, entitytypes)) {
            return Optional.empty();
        } else {
            EntityInsentient entityinsentient1;

            if (entityinsentient instanceof EntityAgeable) {
                entityinsentient1 = ((EntityAgeable) entityinsentient).getBreedOffspring(worldserver, (EntityAgeable) entityinsentient);
            } else {
                entityinsentient1 = entitytypes.create(worldserver, EntitySpawnReason.SPAWN_ITEM_USE);
            }

            if (entityinsentient1 == null) {
                return Optional.empty();
            } else {
                entityinsentient1.setBaby(true);
                if (!entityinsentient1.isBaby()) {
                    return Optional.empty();
                } else {
                    entityinsentient1.snapTo(vec3d.x(), vec3d.y(), vec3d.z(), 0.0F, 0.0F);
                    entityinsentient1.applyComponentsFromItemStack(itemstack);
                    worldserver.addFreshEntityWithPassengers(entityinsentient1, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG); // CraftBukkit
                    itemstack.consume(1, entityhuman);
                    return Optional.of(entityinsentient1);
                }
            }
        }
    }

    @Override
    public boolean shouldPrintOpWarning(ItemStack itemstack, @Nullable EntityHuman entityhuman) {
        if (entityhuman != null && entityhuman.getPermissionLevel() >= 2) {
            TypedEntityData<EntityTypes<?>> typedentitydata = (TypedEntityData) itemstack.get(DataComponents.ENTITY_DATA);

            if (typedentitydata != null) {
                return ((EntityTypes) typedentitydata.type()).onlyOpCanSetNbt();
            }
        }

        return false;
    }
}
