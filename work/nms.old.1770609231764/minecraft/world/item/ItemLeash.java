package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.EntityLeash;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import java.util.Iterator;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.hanging.HangingPlaceEvent;
// CraftBukkit end

public class ItemLeash extends Item {

    public ItemLeash(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (iblockdata.is(TagsBlock.FENCES)) {
            EntityHuman entityhuman = itemactioncontext.getPlayer();

            if (!world.isClientSide && entityhuman != null) {
                return bindPlayerMobs(entityhuman, world, blockposition, itemactioncontext.getHand()); // CraftBukkit - Pass hand
            }
        }

        return EnumInteractionResult.PASS;
    }

    public static EnumInteractionResult bindPlayerMobs(EntityHuman entityhuman, World world, BlockPosition blockposition, net.minecraft.world.EnumHand enumhand) { // CraftBukkit - Add EnumHand
        EntityLeash entityleash = null;
        List<Leashable> list = Leashable.leashableInArea(world, Vec3D.atCenterOf(blockposition), (leashable) -> {
            return leashable.getLeashHolder() == entityhuman;
        });
        boolean flag = false;

        for (Iterator iterator = list.iterator(); iterator.hasNext();) { // CraftBukkit - handle setLeashedTo at end of loop
            Leashable leashable = (Leashable) iterator.next();
            if (entityleash == null) {
                entityleash = EntityLeash.getOrCreateKnot(world, blockposition);

                // CraftBukkit start - fire HangingPlaceEvent
                org.bukkit.inventory.EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);
                HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) entityleash.getBukkitEntity(), entityhuman != null ? (org.bukkit.entity.Player) entityhuman.getBukkitEntity() : null, CraftBlock.at(world, blockposition), org.bukkit.block.BlockFace.SELF, hand);
                world.getCraftServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    entityleash.discard(null); // CraftBukkit - add Bukkit remove cause
                    return EnumInteractionResult.PASS;
                }
                // CraftBukkit end
                entityleash.playPlacementSound();
            }

            if (leashable.canHaveALeashAttachedTo(entityleash)) {
                // CraftBukkit start
                if (leashable instanceof Entity leashed) {
                    if (org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerLeashEntityEvent(leashed, entityleash, entityhuman, enumhand).isCancelled()) {
                        iterator.remove();
                        continue;
                    }
                }
                // CraftBukkit end
                leashable.setLeashedTo(entityleash, true);
                flag = true;
            }
        }

        if (flag) {
            world.gameEvent(GameEvent.BLOCK_ATTACH, blockposition, GameEvent.a.of((Entity) entityhuman));
            return EnumInteractionResult.SUCCESS_SERVER;
        } else {
            // CraftBukkit start- remove leash if we do not leash any entity because of the cancelled event
            if (entityleash != null) {
                entityleash.discard(null);
            }
            // CraftBukkit end
            return EnumInteractionResult.PASS;
        }
    }

    // CraftBukkit start
    public static EnumInteractionResult bindPlayerMobs(EntityHuman entityhuman, World world, BlockPosition blockposition) {
        return bindPlayerMobs(entityhuman, world, blockposition, net.minecraft.world.EnumHand.MAIN_HAND);
    }
    // CraftBukkit end
}
