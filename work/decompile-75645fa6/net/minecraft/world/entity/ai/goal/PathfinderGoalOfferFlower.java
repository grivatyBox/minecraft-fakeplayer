package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.tags.TagsEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.animal.coppergolem.CopperGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AxisAlignedBB;

public class PathfinderGoalOfferFlower extends PathfinderGoal {

    private static final PathfinderTargetCondition OFFER_TARGET_CONTEXT = PathfinderTargetCondition.forNonCombat().range(6.0D);
    private static final Item OFFER_ITEM = Items.POPPY;
    public static final int OFFER_TICKS = 400;
    private final EntityIronGolem golem;
    @Nullable
    private EntityLiving entity;
    private int tick;

    public PathfinderGoalOfferFlower(EntityIronGolem entityirongolem) {
        this.golem = entityirongolem;
        this.setFlags(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.golem.level().isBrightOutside()) {
            return false;
        } else if (this.golem.getRandom().nextInt(8000) != 0) {
            return false;
        } else {
            this.entity = getServerLevel((Entity) this.golem).getNearestEntity(TagsEntity.CANDIDATE_FOR_IRON_GOLEM_GIFT, PathfinderGoalOfferFlower.OFFER_TARGET_CONTEXT, this.golem, this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.getGolemBoundingBox());
            return this.entity != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.tick > 0;
    }

    @Override
    public void start() {
        this.tick = this.adjustedTickDelay(400);
        this.golem.offerFlower(true);
    }

    @Override
    public void stop() {
        this.golem.offerFlower(false);
        if (this.tick == 0) {
            EntityLiving entityliving = this.entity;

            if (entityliving instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient) entityliving;

                if (entityinsentient.getType().is(TagsEntity.ACCEPTS_IRON_GOLEM_GIFT) && entityinsentient.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA).isEmpty() && this.getGolemBoundingBox().intersects(entityinsentient.getBoundingBox())) {
                    entityinsentient.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, PathfinderGoalOfferFlower.OFFER_ITEM.getDefaultInstance());
                    entityinsentient.setGuaranteedDrop(CopperGolem.EQUIPMENT_SLOT_ANTENNA);
                }
            }
        }

        this.entity = null;
    }

    @Override
    public void tick() {
        if (this.entity != null) {
            this.golem.getLookControl().setLookAt(this.entity, 30.0F, 30.0F);
        }

        --this.tick;
    }

    private AxisAlignedBB getGolemBoundingBox() {
        return this.golem.getBoundingBox().inflate(6.0D, 2.0D, 6.0D);
    }
}
