package net.minecraft.world.item;

import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityPotion;
import net.minecraft.world.entity.projectile.ThrownLingeringPotion;
import net.minecraft.world.level.World;

public class ItemLingeringPotion extends ItemPotionThrowable {

    public ItemLingeringPotion(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        world.playSound((Entity) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.LINGERING_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        return super.use(world, entityhuman, enumhand);
    }

    @Override
    protected EntityPotion createPotion(WorldServer worldserver, EntityLiving entityliving, ItemStack itemstack) {
        return new ThrownLingeringPotion(worldserver, entityliving, itemstack);
    }

    @Override
    protected EntityPotion createPotion(World world, IPosition iposition, ItemStack itemstack) {
        return new ThrownLingeringPotion(world, iposition.x(), iposition.y(), iposition.z(), itemstack);
    }
}
