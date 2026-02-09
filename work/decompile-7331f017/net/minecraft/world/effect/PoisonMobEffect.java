package net.minecraft.world.effect;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;

class PoisonMobEffect extends MobEffectList {

    protected PoisonMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean applyEffectTick(WorldServer worldserver, EntityLiving entityliving, int i) {
        if (entityliving.getHealth() > 1.0F) {
            entityliving.hurtServer(worldserver, entityliving.damageSources().magic(), 1.0F);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        int k = 25 >> j;

        return k > 0 ? i % k == 0 : true;
    }
}
