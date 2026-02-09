package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.phys.Vec3D;

public class RamTarget extends Behavior<Goat> {

    public static final int TIME_OUT_DURATION = 200;
    public static final float RAM_SPEED_FORCE_FACTOR = 1.65F;
    private final Function<Goat, UniformInt> getTimeBetweenRams;
    private final PathfinderTargetCondition ramTargeting;
    private final float speed;
    private final ToDoubleFunction<Goat> getKnockbackForce;
    private Vec3D ramDirection;
    private final Function<Goat, SoundEffect> getImpactSound;
    private final Function<Goat, SoundEffect> getHornBreakSound;

    public RamTarget(Function<Goat, UniformInt> function, PathfinderTargetCondition pathfindertargetcondition, float f, ToDoubleFunction<Goat> todoublefunction, Function<Goat, SoundEffect> function1, Function<Goat, SoundEffect> function2) {
        super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_PRESENT), 200);
        this.getTimeBetweenRams = function;
        this.ramTargeting = pathfindertargetcondition;
        this.speed = f;
        this.getKnockbackForce = todoublefunction;
        this.getImpactSound = function1;
        this.getHornBreakSound = function2;
        this.ramDirection = Vec3D.ZERO;
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, Goat goat) {
        return goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    protected boolean canStillUse(WorldServer worldserver, Goat goat, long i) {
        return goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    protected void start(WorldServer worldserver, Goat goat, long i) {
        BlockPosition blockposition = goat.blockPosition();
        BehaviorController<?> behaviorcontroller = goat.getBrain();
        Vec3D vec3d = (Vec3D) behaviorcontroller.getMemory(MemoryModuleType.RAM_TARGET).get();

        this.ramDirection = (new Vec3D((double) blockposition.getX() - vec3d.x(), 0.0D, (double) blockposition.getZ() - vec3d.z())).normalize();
        behaviorcontroller.setMemory(MemoryModuleType.WALK_TARGET, new MemoryTarget(vec3d, this.speed, 0));
    }

    protected void tick(WorldServer worldserver, Goat goat, long i) {
        List<EntityLiving> list = worldserver.<EntityLiving>getNearbyEntities(EntityLiving.class, this.ramTargeting, goat, goat.getBoundingBox());
        BehaviorController<?> behaviorcontroller = goat.getBrain();

        if (!list.isEmpty()) {
            EntityLiving entityliving = (EntityLiving) list.get(0);
            DamageSource damagesource = worldserver.damageSources().noAggroMobAttack(goat);
            float f = (float) goat.getAttributeValue(GenericAttributes.ATTACK_DAMAGE);

            if (entityliving.hurtServer(worldserver, damagesource, f)) {
                EnchantmentManager.doPostAttackEffects(worldserver, entityliving, damagesource);
            }

            int j = goat.hasEffect(MobEffects.SPEED) ? goat.getEffect(MobEffects.SPEED).getAmplifier() + 1 : 0;
            int k = goat.hasEffect(MobEffects.SLOWNESS) ? goat.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1 : 0;
            float f1 = 0.25F * (float) (j - k);
            float f2 = MathHelper.clamp(goat.getSpeed() * 1.65F, 0.2F, 3.0F) + f1;
            DamageSource damagesource1 = worldserver.damageSources().mobAttack(goat);
            float f3 = entityliving.applyItemBlocking(worldserver, damagesource1, f);
            float f4 = f3 > 0.0F ? 0.5F : 1.0F;

            entityliving.knockback((double) (f4 * f2) * this.getKnockbackForce.applyAsDouble(goat), this.ramDirection.x(), this.ramDirection.z());
            this.finishRam(worldserver, goat);
            worldserver.playSound((Entity) null, (Entity) goat, (SoundEffect) this.getImpactSound.apply(goat), SoundCategory.NEUTRAL, 1.0F, 1.0F);
        } else if (this.hasRammedHornBreakingBlock(worldserver, goat)) {
            worldserver.playSound((Entity) null, (Entity) goat, (SoundEffect) this.getImpactSound.apply(goat), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            boolean flag = goat.dropHorn();

            if (flag) {
                worldserver.playSound((Entity) null, (Entity) goat, (SoundEffect) this.getHornBreakSound.apply(goat), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }

            this.finishRam(worldserver, goat);
        } else {
            Optional<MemoryTarget> optional = behaviorcontroller.<MemoryTarget>getMemory(MemoryModuleType.WALK_TARGET);
            Optional<Vec3D> optional1 = behaviorcontroller.<Vec3D>getMemory(MemoryModuleType.RAM_TARGET);
            boolean flag1 = optional.isEmpty() || optional1.isEmpty() || ((MemoryTarget) optional.get()).getTarget().currentPosition().closerThan((IPosition) optional1.get(), 0.25D);

            if (flag1) {
                this.finishRam(worldserver, goat);
            }
        }

    }

    private boolean hasRammedHornBreakingBlock(WorldServer worldserver, Goat goat) {
        Vec3D vec3d = goat.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize();
        BlockPosition blockposition = BlockPosition.containing(goat.position().add(vec3d));

        return worldserver.getBlockState(blockposition).is(TagsBlock.SNAPS_GOAT_HORN) || worldserver.getBlockState(blockposition.above()).is(TagsBlock.SNAPS_GOAT_HORN);
    }

    protected void finishRam(WorldServer worldserver, Goat goat) {
        worldserver.broadcastEntityEvent(goat, (byte) 59);
        goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, ((UniformInt) this.getTimeBetweenRams.apply(goat)).sample(worldserver.random));
        goat.getBrain().eraseMemory(MemoryModuleType.RAM_TARGET);
    }
}
