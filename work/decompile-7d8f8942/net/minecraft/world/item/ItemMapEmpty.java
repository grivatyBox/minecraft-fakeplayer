package net.minecraft.world.item;

import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class ItemMapEmpty extends Item {

    public ItemMapEmpty(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (world instanceof WorldServer worldserver) {
            itemstack.consume(1, entityhuman);
            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            worldserver.playSound((Entity) null, (Entity) entityhuman, SoundEffects.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, entityhuman.getSoundSource(), 1.0F, 1.0F);
            ItemStack itemstack1 = ItemWorldMap.create(worldserver, entityhuman.getBlockX(), entityhuman.getBlockZ(), (byte) 0, true, false);

            if (itemstack.isEmpty()) {
                return EnumInteractionResult.SUCCESS.heldItemTransformedTo(itemstack1);
            } else {
                if (!entityhuman.getInventory().add(itemstack1.copy())) {
                    entityhuman.drop(itemstack1, false);
                }

                return EnumInteractionResult.SUCCESS;
            }
        } else {
            return EnumInteractionResult.SUCCESS;
        }
    }
}
