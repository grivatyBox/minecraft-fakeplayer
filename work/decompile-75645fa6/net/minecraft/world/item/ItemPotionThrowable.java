package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityPotion;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.level.World;

public abstract class ItemPotionThrowable extends ItemPotion implements ProjectileItem {

    public static float PROJECTILE_SHOOT_POWER = 0.5F;

    public ItemPotionThrowable(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (world instanceof WorldServer worldserver) {
            IProjectile.spawnProjectileFromRotation(this::createPotion, worldserver, itemstack, entityhuman, -20.0F, ItemPotionThrowable.PROJECTILE_SHOOT_POWER, 1.0F);
        }

        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        itemstack.consume(1, entityhuman);
        return EnumInteractionResult.SUCCESS;
    }

    protected abstract EntityPotion createPotion(WorldServer worldserver, EntityLiving entityliving, ItemStack itemstack);

    protected abstract EntityPotion createPotion(World world, IPosition iposition, ItemStack itemstack);

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        return this.createPotion(world, iposition, itemstack);
    }

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().uncertainty(ProjectileItem.a.DEFAULT.uncertainty() * 0.5F).power(ProjectileItem.a.DEFAULT.power() * 1.25F).build();
    }
}
