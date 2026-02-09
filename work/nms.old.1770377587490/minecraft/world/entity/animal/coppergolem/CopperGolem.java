package net.minecraft.world.entity.animal.coppergolem;

import com.mojang.serialization.Dynamic;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.IShearable;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.EntityGolem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.BlockChest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public class CopperGolem extends EntityGolem implements ContainerUser, IShearable {

    private static final long IGNORE_WEATHERING_TICK = -2L;
    private static final long UNSET_WEATHERING_TICK = -1L;
    private static final int WEATHERING_TICK_FROM = 504000;
    private static final int WEATHERING_TICK_TO = 552000;
    private static final int SPIN_ANIMATION_MIN_COOLDOWN = 200;
    private static final int SPIN_ANIMATION_MAX_COOLDOWN = 240;
    private static final float SPIN_SOUND_TIME_INTERVAL_OFFSET = 10.0F;
    private static final float TURN_TO_STATUE_CHANCE = 0.0058F;
    private static final int SPAWN_COOLDOWN_MIN = 60;
    private static final int SPAWN_COOLDOWN_MAX = 100;
    private static final DataWatcherObject<WeatheringCopper.a> DATA_WEATHER_STATE = DataWatcher.<WeatheringCopper.a>defineId(CopperGolem.class, DataWatcherRegistry.WEATHERING_COPPER_STATE);
    private static final DataWatcherObject<CopperGolemState> COPPER_GOLEM_STATE = DataWatcher.<CopperGolemState>defineId(CopperGolem.class, DataWatcherRegistry.COPPER_GOLEM_STATE);
    @Nullable
    private BlockPosition openedChestPos;
    @Nullable
    private UUID lastLightningBoltUUID;
    public long nextWeatheringTick = -1L;
    private int idleAnimationStartTick = 0;
    private final AnimationState idleAnimationState = new AnimationState();
    private final AnimationState interactionGetItemAnimationState = new AnimationState();
    private final AnimationState interactionGetNoItemAnimationState = new AnimationState();
    private final AnimationState interactionDropItemAnimationState = new AnimationState();
    private final AnimationState interactionDropNoItemAnimationState = new AnimationState();
    public static final EnumItemSlot EQUIPMENT_SLOT_ANTENNA = EnumItemSlot.SADDLE;

    public CopperGolem(EntityTypes<? extends EntityGolem> entitytypes, World world) {
        super(entitytypes, world);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.getNavigation().setCanOpenDoors(true);
        this.setPersistenceRequired();
        this.setState(CopperGolemState.IDLE);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DANGER_OTHER, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, this.getRandom().nextInt(60, 100));
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MOVEMENT_SPEED, (double) 0.2F).add(GenericAttributes.STEP_HEIGHT, 1.0D).add(GenericAttributes.MAX_HEALTH, 12.0D);
    }

    public CopperGolemState getState() {
        return (CopperGolemState) this.entityData.get(CopperGolem.COPPER_GOLEM_STATE);
    }

    public void setState(CopperGolemState coppergolemstate) {
        this.entityData.set(CopperGolem.COPPER_GOLEM_STATE, coppergolemstate);
    }

    public WeatheringCopper.a getWeatherState() {
        return (WeatheringCopper.a) this.entityData.get(CopperGolem.DATA_WEATHER_STATE);
    }

    public void setWeatherState(WeatheringCopper.a weatheringcopper_a) {
        this.entityData.set(CopperGolem.DATA_WEATHER_STATE, weatheringcopper_a);
    }

    public void setOpenedChestPos(BlockPosition blockposition) {
        this.openedChestPos = blockposition;
    }

    public void clearOpenedChestPos() {
        this.openedChestPos = null;
    }

    public AnimationState getIdleAnimationState() {
        return this.idleAnimationState;
    }

    public AnimationState getInteractionGetItemAnimationState() {
        return this.interactionGetItemAnimationState;
    }

    public AnimationState getInteractionGetNoItemAnimationState() {
        return this.interactionGetNoItemAnimationState;
    }

    public AnimationState getInteractionDropItemAnimationState() {
        return this.interactionDropItemAnimationState;
    }

    public AnimationState getInteractionDropNoItemAnimationState() {
        return this.interactionDropNoItemAnimationState;
    }

    @Override
    protected BehaviorController.b<CopperGolem> brainProvider() {
        return CopperGolemAi.brainProvider();
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return CopperGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public BehaviorController<CopperGolem> getBrain() {
        return (BehaviorController<CopperGolem>) super.getBrain(); // CraftBukkit - decompile error
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(CopperGolem.DATA_WEATHER_STATE, WeatheringCopper.a.UNAFFECTED);
        datawatcher_a.define(CopperGolem.COPPER_GOLEM_STATE, CopperGolemState.IDLE);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueoutput) {
        super.addAdditionalSaveData(valueoutput);
        valueoutput.putLong("next_weather_age", this.nextWeatheringTick);
        valueoutput.store("weather_state", WeatheringCopper.a.CODEC, this.getWeatherState());
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueinput) {
        super.readAdditionalSaveData(valueinput);
        this.nextWeatheringTick = valueinput.getLongOr("next_weather_age", -1L);
        this.setWeatherState((WeatheringCopper.a) valueinput.read("weather_state", WeatheringCopper.a.CODEC).orElse(WeatheringCopper.a.UNAFFECTED));
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("copperGolemBrain");
        this.getBrain().tick(worldserver, this);
        gameprofilerfiller.pop();
        gameprofilerfiller.push("copperGolemActivityUpdate");
        CopperGolemAi.updateActivity(this);
        gameprofilerfiller.pop();
        super.customServerAiStep(worldserver);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (!this.isNoAi()) {
                this.setupAnimationStates();
            }
        } else {
            this.updateWeathering((WorldServer) this.level(), this.level().getRandom(), this.level().getGameTime());
        }

    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.isEmpty()) {
            ItemStack itemstack1 = this.getMainHandItem();

            if (!itemstack1.isEmpty()) {
                BehaviorUtil.throwItem(this, itemstack1, entityhuman.position());
                this.setItemInHand(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                return EnumInteractionResult.SUCCESS;
            }
        }

        World world = this.level();

        if (itemstack.is(Items.SHEARS) && this.readyForShearing()) {
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                // CraftBukkit start
                if (!CraftEventFactory.handlePlayerShearEntityEvent(entityhuman, this, itemstack, enumhand)) {
                    return EnumInteractionResult.PASS;
                }
                // CraftBukkit end
                this.shear(worldserver, SoundCategory.PLAYERS, itemstack);
                this.gameEvent(GameEvent.SHEAR, entityhuman);
                itemstack.hurtAndBreak(1, entityhuman, enumhand);
            }

            return EnumInteractionResult.SUCCESS;
        } else if (world.isClientSide()) {
            return EnumInteractionResult.PASS;
        } else if (itemstack.is(Items.HONEYCOMB) && this.nextWeatheringTick != -2L) {
            world.levelEvent(this, 3003, this.blockPosition(), 0);
            this.nextWeatheringTick = -2L;
            this.usePlayerItem(entityhuman, enumhand, itemstack);
            return EnumInteractionResult.SUCCESS_SERVER;
        } else if (itemstack.is(TagsItem.AXES) && this.nextWeatheringTick == -2L) {
            world.playSound((Entity) null, (Entity) this, SoundEffects.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
            world.levelEvent(this, 3004, this.blockPosition(), 0);
            this.nextWeatheringTick = -1L;
            itemstack.hurtAndBreak(1, entityhuman, enumhand.asEquipmentSlot());
            return EnumInteractionResult.SUCCESS_SERVER;
        } else {
            if (itemstack.is(TagsItem.AXES)) {
                WeatheringCopper.a weatheringcopper_a = this.getWeatherState();

                if (weatheringcopper_a != WeatheringCopper.a.UNAFFECTED) {
                    world.playSound((Entity) null, (Entity) this, SoundEffects.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
                    world.levelEvent(this, 3005, this.blockPosition(), 0);
                    this.nextWeatheringTick = -1L;
                    this.entityData.set(CopperGolem.DATA_WEATHER_STATE, weatheringcopper_a.previous(), true);
                    itemstack.hurtAndBreak(1, entityhuman, enumhand.asEquipmentSlot());
                    return EnumInteractionResult.SUCCESS_SERVER;
                }
            }

            return super.mobInteract(entityhuman, enumhand);
        }
    }

    private void updateWeathering(WorldServer worldserver, RandomSource randomsource, long i) {
        if (this.nextWeatheringTick != -2L) {
            if (this.nextWeatheringTick == -1L) {
                this.nextWeatheringTick = i + (long) randomsource.nextIntBetweenInclusive(504000, 552000);
            } else {
                WeatheringCopper.a weatheringcopper_a = (WeatheringCopper.a) this.entityData.get(CopperGolem.DATA_WEATHER_STATE);
                boolean flag = weatheringcopper_a.equals(WeatheringCopper.a.OXIDIZED);

                if (i >= this.nextWeatheringTick && !flag) {
                    WeatheringCopper.a weatheringcopper_a1 = weatheringcopper_a.next();
                    boolean flag1 = weatheringcopper_a1.equals(WeatheringCopper.a.OXIDIZED);

                    this.setWeatherState(weatheringcopper_a1);
                    this.nextWeatheringTick = flag1 ? 0L : this.nextWeatheringTick + (long) randomsource.nextIntBetweenInclusive(504000, 552000);
                }

                if (flag && this.canTurnToStatue(worldserver)) {
                    this.turnToStatue(worldserver);
                }

            }
        }
    }

    private boolean canTurnToStatue(World world) {
        return world.getBlockState(this.blockPosition()).is(Blocks.AIR) && world.random.nextFloat() <= 0.0058F;
    }

    private void turnToStatue(WorldServer worldserver) {
        BlockPosition blockposition = this.blockPosition();

        // CraftBukkit start
        IBlockData blockState = (IBlockData) ((IBlockData) Blocks.OXIDIZED_COPPER_GOLEM_STATUE.defaultBlockState().setValue(CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.a.values()[this.random.nextInt(0, CopperGolemStatueBlock.a.values().length)])).setValue(CopperGolemStatueBlock.FACING, EnumDirection.fromYRot((double) this.getYRot()));
        if (!CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, blockState)) {
            return;
        }
        worldserver.setBlock(blockposition, blockState, 3);
        // CraftBukkit end
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof CopperGolemStatueBlockEntity coppergolemstatueblockentity) {
            coppergolemstatueblockentity.createStatue(this);
            this.forceDrops = true; // CraftBukkit
            this.dropPreservedEquipment(worldserver);
            this.forceDrops = false; // CraftBukkkit
            this.discard(EntityRemoveEvent.Cause.DESPAWN); // CraftBukkit - add Bukkit remove cause
            this.playSound(SoundEffects.COPPER_GOLEM_BECOME_STATUE);
            if (this.isLeashed()) {
                if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    this.dropLeash();
                } else {
                    this.removeLeash();
                }
            }
        }

    }

    private void setupAnimationStates() {
        switch (this.getState()) {
            case IDLE:
                this.interactionGetNoItemAnimationState.stop();
                this.interactionGetItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                if (this.idleAnimationStartTick == this.tickCount) {
                    this.idleAnimationState.start(this.tickCount);
                } else if (this.idleAnimationStartTick == 0) {
                    this.idleAnimationStartTick = this.tickCount + this.random.nextInt(200, 240);
                }

                if ((float) this.tickCount == (float) this.idleAnimationStartTick + 10.0F) {
                    this.playHeadSpinSound();
                    this.idleAnimationStartTick = 0;
                }
                break;
            case GETTING_ITEM:
                this.idleAnimationState.stop();
                this.idleAnimationStartTick = 0;
                this.interactionGetNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                this.interactionGetItemAnimationState.startIfStopped(this.tickCount);
                break;
            case GETTING_NO_ITEM:
                this.idleAnimationState.stop();
                this.idleAnimationStartTick = 0;
                this.interactionGetItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionGetNoItemAnimationState.startIfStopped(this.tickCount);
                break;
            case DROPPING_ITEM:
                this.idleAnimationState.stop();
                this.idleAnimationStartTick = 0;
                this.interactionGetItemAnimationState.stop();
                this.interactionGetNoItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.startIfStopped(this.tickCount);
                break;
            case DROPPING_NO_ITEM:
                this.idleAnimationState.stop();
                this.idleAnimationStartTick = 0;
                this.interactionGetItemAnimationState.stop();
                this.interactionGetNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.startIfStopped(this.tickCount);
        }

    }

    public void spawn(WeatheringCopper.a weatheringcopper_a) {
        this.setWeatherState(weatheringcopper_a);
        this.playSpawnSound();
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        this.playSpawnSound();
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public void playSpawnSound() {
        this.playSound(SoundEffects.COPPER_GOLEM_SPAWN);
    }

    private void playHeadSpinSound() {
        if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSpinHeadSound(), this.getSoundSource(), 1.0F, 1.0F, false);
        }

    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).hurtSound();
    }

    @Override
    protected SoundEffect getDeathSound() {
        return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).deathSound();
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).stepSound(), 1.0F, 1.0F);
    }

    private SoundEffect getSpinHeadSound() {
        return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).spinHeadSound();
    }

    @Override
    public Vec3D getLeashOffset() {
        return new Vec3D(0.0D, (double) (0.75F * this.getEyeHeight()), 0.0D);
    }

    @Override
    public boolean hasContainerOpen(ContainerOpenersCounter containeropenerscounter, BlockPosition blockposition) {
        if (this.openedChestPos == null) {
            return false;
        } else {
            IBlockData iblockdata = this.level().getBlockState(this.openedChestPos);

            return this.openedChestPos.equals(blockposition) || iblockdata.getBlock() instanceof BlockChest && iblockdata.getValue(BlockChest.TYPE) != BlockPropertyChestType.SINGLE && BlockChest.getConnectedBlockPos(this.openedChestPos, iblockdata).equals(blockposition);
        }
    }

    @Override
    public double getContainerInteractionRange() {
        return 3.0D;
    }

    @Override
    public void shear(WorldServer worldserver, SoundCategory soundcategory, ItemStack itemstack) {
        worldserver.playSound((Entity) null, (Entity) this, SoundEffects.COPPER_GOLEM_SHEAR, soundcategory, 1.0F, 1.0F);
        ItemStack itemstack1 = this.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA);

        this.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, ItemStack.EMPTY);
        this.forceDrops = true; // CraftBukkit
        this.spawnAtLocation(worldserver, itemstack1, 1.5F);
        this.forceDrops = false; // CraftBukkit
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && this.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA).is(TagsItem.SHEARABLE_FROM_COPPER_GOLEM);
    }

    @Override
    protected void dropEquipment(WorldServer worldserver) {
        super.dropEquipment(worldserver);
        this.dropPreservedEquipment(worldserver);
    }

    @Override
    // CraftBukkit start - void -> boolean
    public boolean actuallyHurt(WorldServer worldserver, DamageSource damagesource, float f, EntityDamageEvent event) {
        boolean damageResult = super.actuallyHurt(worldserver, damagesource, f, event);
        if (!damageResult) {
            return false;
        }
        // CraftBukkit end
        this.setState(CopperGolemState.IDLE);
        return true; // CraftBukkit
    }

    @Override
    public void thunderHit(WorldServer worldserver, EntityLightning entitylightning) {
        super.thunderHit(worldserver, entitylightning);
        UUID uuid = entitylightning.getUUID();

        if (!uuid.equals(this.lastLightningBoltUUID)) {
            this.lastLightningBoltUUID = uuid;
            WeatheringCopper.a weatheringcopper_a = this.getWeatherState();

            if (weatheringcopper_a != WeatheringCopper.a.UNAFFECTED) {
                this.nextWeatheringTick = -1L;
                this.entityData.set(CopperGolem.DATA_WEATHER_STATE, weatheringcopper_a.previous(), true);
            }
        }

    }
}
