package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
// CraftBukkit end

public class PathfinderGoalTempt extends PathfinderGoal {

    private static final PathfinderTargetCondition TEMPT_TARGETING = PathfinderTargetCondition.forNonCombat().ignoreLineOfSight();
    private static final double DEFAULT_STOP_DISTANCE = 2.5D;
    private final PathfinderTargetCondition targetingConditions;
    protected final EntityInsentient mob;
    protected final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    @Nullable
    protected EntityLiving player; // CraftBukkit
    private int calmDown;
    private boolean isRunning;
    private final Predicate<ItemStack> items;
    private final boolean canScare;
    private final double stopDistance;

    public PathfinderGoalTempt(EntityCreature entitycreature, double d0, Predicate<ItemStack> predicate, boolean flag) {
        this((EntityInsentient) entitycreature, d0, predicate, flag, 2.5D);
    }

    public PathfinderGoalTempt(EntityCreature entitycreature, double d0, Predicate<ItemStack> predicate, boolean flag, double d1) {
        this((EntityInsentient) entitycreature, d0, predicate, flag, d1);
    }

    PathfinderGoalTempt(EntityInsentient entityinsentient, double d0, Predicate<ItemStack> predicate, boolean flag, double d1) {
        this.mob = entityinsentient;
        this.speedModifier = d0;
        this.items = predicate;
        this.canScare = flag;
        this.stopDistance = d1;
        this.setFlags(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        this.targetingConditions = PathfinderGoalTempt.TEMPT_TARGETING.copy().selector((entityliving, worldserver) -> {
            return this.shouldFollow(entityliving);
        });
    }

    @Override
    public boolean canUse() {
        if (this.calmDown > 0) {
            --this.calmDown;
            return false;
        } else {
            this.player = getServerLevel((Entity) this.mob).getNearestPlayer(this.targetingConditions.range(this.mob.getAttributeValue(GenericAttributes.TEMPT_RANGE)), this.mob);
            // CraftBukkit start
            if (this.player != null) {
                EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, this.player, EntityTargetEvent.TargetReason.TEMPT);
                if (event.isCancelled()) {
                    return false;
                }
                this.player = (event.getTarget() == null) ? null : ((CraftLivingEntity) event.getTarget()).getHandle();
            }
            // CraftBukkit end
            return this.player != null;
        }
    }

    private boolean shouldFollow(EntityLiving entityliving) {
        return this.items.test(entityliving.getMainHandItem()) || this.items.test(entityliving.getOffhandItem());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.canScare()) {
            if (this.mob.distanceToSqr((Entity) this.player) < 36.0D) {
                if (this.player.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double) this.player.getXRot() - this.pRotX) > 5.0D || Math.abs((double) this.player.getYRot() - this.pRotY) > 5.0D) {
                    return false;
                }
            } else {
                this.px = this.player.getX();
                this.py = this.player.getY();
                this.pz = this.player.getZ();
            }

            this.pRotX = (double) this.player.getXRot();
            this.pRotY = (double) this.player.getYRot();
        }

        return this.canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    @Override
    public void start() {
        this.px = this.player.getX();
        this.py = this.player.getY();
        this.pz = this.player.getZ();
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.player = null;
        this.stopNavigation();
        this.calmDown = reducedTickDelay(100);
        this.isRunning = false;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.player, (float) (this.mob.getMaxHeadYRot() + 20), (float) this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr((Entity) this.player) < this.stopDistance * this.stopDistance) {
            this.stopNavigation();
        } else {
            this.navigateTowards(this.player);
        }

    }

    protected void stopNavigation() {
        this.mob.getNavigation().stop();
    }

    protected void navigateTowards(EntityHuman entityhuman) {
        // CraftBukkit start
        this.navigateTowards((Entity) entityhuman);
    }

    protected void navigateTowards(Entity entity) {
        this.mob.getNavigation().moveTo(entity, this.speedModifier);
        // CraftBukkit end
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public static class a extends PathfinderGoalTempt {

        public a(EntityInsentient entityinsentient, double d0, Predicate<ItemStack> predicate, boolean flag, double d1) {
            super(entityinsentient, d0, predicate, flag, d1);
        }

        @Override
        protected void stopNavigation() {
            this.mob.getMoveControl().setWait();
        }

        @Override
        protected void navigateTowards(EntityHuman entityhuman) {
            Vec3D vec3d = entityhuman.getEyePosition().subtract(this.mob.position()).scale(this.mob.getRandom().nextDouble()).add(this.mob.position());

            this.mob.getMoveControl().setWantedPosition(vec3d.x, vec3d.y, vec3d.z, this.speedModifier);
        }
    }
}
