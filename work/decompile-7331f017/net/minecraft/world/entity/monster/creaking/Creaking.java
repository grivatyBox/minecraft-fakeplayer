package net.minecraft.world.entity.monster.creaking;

import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerJump;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.ai.control.EntityAIBodyControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public class Creaking extends EntityMonster {

    private static final DataWatcherObject<Boolean> CAN_MOVE = DataWatcher.defineId(Creaking.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Boolean> IS_ACTIVE = DataWatcher.defineId(Creaking.class, DataWatcherRegistry.BOOLEAN);
    private static final int ATTACK_ANIMATION_DURATION = 20;
    private static final int MAX_HEALTH = 1;
    private static final float ATTACK_DAMAGE = 2.0F;
    private static final float FOLLOW_RANGE = 32.0F;
    private static final float ACTIVATION_RANGE_SQ = 144.0F;
    public static final int ATTACK_INTERVAL = 40;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.2F;
    public static final int CREAKING_ORANGE = 16545810;
    public static final int CREAKING_GRAY = 6250335;
    private int attackAnimationRemainingTicks;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState invulnerabilityAnimationState = new AnimationState();

    public Creaking(EntityTypes<? extends Creaking> entitytypes, World world) {
        super(entitytypes, world);
        this.lookControl = new Creaking.c(this);
        this.moveControl = new Creaking.d(this);
        this.jumpControl = new Creaking.b(this);
        Navigation navigation = (Navigation) this.getNavigation();

        navigation.setCanFloat(true);
        this.xpReward = 0;
    }

    @Override
    protected EntityAIBodyControl createBodyControl() {
        return new Creaking.a(this);
    }

    @Override
    protected BehaviorController.b<Creaking> brainProvider() {
        return CreakingAi.brainProvider();
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return CreakingAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Creaking.CAN_MOVE, true);
        datawatcher_a.define(Creaking.IS_ACTIVE, false);
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MAX_HEALTH, 1.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.30000001192092896D).add(GenericAttributes.ATTACK_DAMAGE, 2.0D).add(GenericAttributes.FOLLOW_RANGE, 32.0D).add(GenericAttributes.STEP_HEIGHT, 1.0D);
    }

    public boolean canMove() {
        return (Boolean) this.entityData.get(Creaking.CAN_MOVE);
    }

    @Override
    public boolean doHurtTarget(WorldServer worldserver, Entity entity) {
        if (!(entity instanceof EntityLiving)) {
            return false;
        } else {
            this.attackAnimationRemainingTicks = 20;
            this.level().broadcastEntityEvent(this, (byte) 4);
            return super.doHurtTarget(worldserver, entity);
        }
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.canMove();
    }

    @Override
    public BehaviorController<Creaking> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("creakingBrain");
        this.getBrain().tick((WorldServer) this.level(), this);
        gameprofilerfiller.pop();
        CreakingAi.updateActivity(this);
    }

    @Override
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }

        if (!this.level().isClientSide) {
            boolean flag = (Boolean) this.entityData.get(Creaking.CAN_MOVE);
            boolean flag1 = this.checkCanMove();

            if (flag1 != flag) {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                if (flag1) {
                    this.makeSound(SoundEffects.CREAKING_UNFREEZE);
                } else {
                    this.stopInPlace();
                    this.makeSound(SoundEffects.CREAKING_FREEZE);
                }
            }

            this.entityData.set(Creaking.CAN_MOVE, flag1);
        }

        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }

    }

    private void setupAnimationStates() {
        this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 4) {
            this.attackAnimationRemainingTicks = 20;
            this.playAttackSound();
        } else {
            super.handleEntityEvent(b0);
        }

    }

    @Override
    public void playAttackSound() {
        this.makeSound(SoundEffects.CREAKING_ATTACK);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.isActive() ? null : SoundEffects.CREAKING_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.CREAKING_SWAY;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.CREAKING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.CREAKING_STEP, 0.15F, 1.0F);
    }

    @Nullable
    @Override
    public EntityLiving getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    @Override
    public void knockback(double d0, double d1, double d2) {
        if (this.canMove()) {
            super.knockback(d0, d1, d2);
        }
    }

    public boolean checkCanMove() {
        List<EntityHuman> list = (List) this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());

        if (list.isEmpty()) {
            if (this.isActive()) {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                this.makeSound(SoundEffects.CREAKING_DEACTIVATE);
                this.setIsActive(false);
            }

            return true;
        } else {
            Predicate<EntityLiving> predicate = this.isActive() ? EntityLiving.PLAYER_NOT_WEARING_DISGUISE_ITEM : (entityliving) -> {
                return true;
            };
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (!entityhuman.isCreative() && this.isLookingAtMe(entityhuman, 0.5D, false, true, predicate, new DoubleSupplier[]{this::getEyeY, this::getY, () -> {
                            return (this.getEyeY() + this.getY()) / 2.0D;
                        }})) {
                    if (this.isActive()) {
                        return false;
                    }

                    if (entityhuman.distanceToSqr((Entity) this) < 144.0D) {
                        this.gameEvent(GameEvent.ENTITY_ACTION);
                        this.makeSound(SoundEffects.CREAKING_ACTIVATE);
                        this.setIsActive(true);
                        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, (Object) entityhuman);
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public void setIsActive(boolean flag) {
        this.entityData.set(Creaking.IS_ACTIVE, flag);
    }

    public boolean isActive() {
        return (Boolean) this.entityData.get(Creaking.IS_ACTIVE);
    }

    @Override
    public float getWalkTargetValue(BlockPosition blockposition, IWorldReader iworldreader) {
        return 0.0F;
    }

    private class c extends ControllerLook {

        public c(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }

        }
    }

    private class d extends ControllerMove {

        public d(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }

        }
    }

    private class b extends ControllerJump {

        public b(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            } else {
                Creaking.this.setJumping(false);
            }

        }
    }

    private class a extends EntityAIBodyControl {

        public a(final Creaking creaking) {
            super(creaking);
        }

        @Override
        public void clientTick() {
            if (Creaking.this.canMove()) {
                super.clientTick();
            }

        }
    }
}
