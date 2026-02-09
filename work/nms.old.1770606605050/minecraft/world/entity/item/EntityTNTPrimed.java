package net.minecraft.world.entity.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// CraftBukkit start;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
// CraftBukkit end

public class EntityTNTPrimed extends Entity implements TraceableEntity {

    private static final DataWatcherObject<Integer> DATA_FUSE_ID = DataWatcher.<Integer>defineId(EntityTNTPrimed.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<IBlockData> DATA_BLOCK_STATE_ID = DataWatcher.<IBlockData>defineId(EntityTNTPrimed.class, DataWatcherRegistry.BLOCK_STATE);
    private static final short DEFAULT_FUSE_TIME = 80;
    private static final float DEFAULT_EXPLOSION_POWER = 4.0F;
    private static final IBlockData DEFAULT_BLOCK_STATE = Blocks.TNT.defaultBlockState();
    private static final String TAG_BLOCK_STATE = "block_state";
    public static final String TAG_FUSE = "fuse";
    private static final String TAG_EXPLOSION_POWER = "explosion_power";
    private static final ExplosionDamageCalculator USED_PORTAL_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, float f) {
            return iblockdata.is(Blocks.NETHER_PORTAL) ? false : super.shouldBlockExplode(explosion, iblockaccess, blockposition, iblockdata, f);
        }

        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
            return iblockdata.is(Blocks.NETHER_PORTAL) ? Optional.empty() : super.getBlockExplosionResistance(explosion, iblockaccess, blockposition, iblockdata, fluid);
        }
    };
    @Nullable
    public EntityReference<EntityLiving> owner;
    private boolean usedPortal;
    public float explosionPower;
    public boolean isIncendiary = false; // CraftBukkit - add field

    public EntityTNTPrimed(EntityTypes<? extends EntityTNTPrimed> entitytypes, World world) {
        super(entitytypes, world);
        this.explosionPower = 4.0F;
        this.blocksBuilding = true;
    }

    public EntityTNTPrimed(World world, double d0, double d1, double d2, @Nullable EntityLiving entityliving) {
        this(EntityTypes.TNT, world);
        this.setPos(d0, d1, d2);
        double d3 = world.random.nextDouble() * (double) ((float) Math.PI * 2F);

        this.setDeltaMovement(-Math.sin(d3) * 0.02D, (double) 0.2F, -Math.cos(d3) * 0.02D);
        this.setFuse(80);
        this.xo = d0;
        this.yo = d1;
        this.zo = d2;
        this.owner = EntityReference.of(entityliving);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityTNTPrimed.DATA_FUSE_ID, 80);
        datawatcher_a.define(EntityTNTPrimed.DATA_BLOCK_STATE_ID, EntityTNTPrimed.DEFAULT_BLOCK_STATE);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04D;
    }

    @Override
    public void tick() {
        this.handlePortal();
        this.applyGravity();
        this.move(EnumMoveType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int i = this.getFuse() - 1;

        this.setFuse(i);
        if (i <= 0) {
            // CraftBukkit start - Need to reverse the order of the explosion and the entity death so we have a location for the event
            // this.discard();
            if (!this.level().isClientSide()) {
                this.explode();
            }
            this.discard(EntityRemoveEvent.Cause.EXPLODE); // CraftBukkit - add Bukkit remove cause
            // CraftBukkit end
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide()) {
                this.level().addParticle(Particles.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    private void explode() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (worldserver.getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
                // CraftBukkit start
                ExplosionPrimeEvent event = CraftEventFactory.callExplosionPrimeEvent((org.bukkit.entity.Explosive) this.getBukkitEntity());
                if (event.isCancelled()) {
                    return;
                }
                this.level().explode(this, Explosion.getDefaultDamageSource(this.level(), this), this.usedPortal ? EntityTNTPrimed.USED_PORTAL_DAMAGE_CALCULATOR : null, this.getX(), this.getY(0.0625D), this.getZ(), event.getRadius(), event.getFire(), World.a.TNT);
                // CraftBukkit end
            }
        }

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {
        valueoutput.putShort("fuse", (short) this.getFuse());
        valueoutput.store("block_state", IBlockData.CODEC, this.getBlockState());
        if (this.explosionPower != 4.0F) {
            valueoutput.putFloat("explosion_power", this.explosionPower);
        }

        EntityReference.store(this.owner, valueoutput, "owner");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {
        this.setFuse(valueinput.getShortOr("fuse", (short) 80));
        this.setBlockState((IBlockData) valueinput.read("block_state", IBlockData.CODEC).orElse(EntityTNTPrimed.DEFAULT_BLOCK_STATE));
        this.explosionPower = MathHelper.clamp(valueinput.getFloatOr("explosion_power", 4.0F), 0.0F, 128.0F);
        this.owner = EntityReference.<EntityLiving>read(valueinput, "owner");
    }

    @Nullable
    @Override
    public EntityLiving getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof EntityTNTPrimed entitytntprimed) {
            this.owner = entitytntprimed.owner;
        }

    }

    public void setFuse(int i) {
        this.entityData.set(EntityTNTPrimed.DATA_FUSE_ID, i);
    }

    public int getFuse() {
        return (Integer) this.entityData.get(EntityTNTPrimed.DATA_FUSE_ID);
    }

    public void setBlockState(IBlockData iblockdata) {
        this.entityData.set(EntityTNTPrimed.DATA_BLOCK_STATE_ID, iblockdata);
    }

    public IBlockData getBlockState() {
        return (IBlockData) this.entityData.get(EntityTNTPrimed.DATA_BLOCK_STATE_ID);
    }

    private void setUsedPortal(boolean flag) {
        this.usedPortal = flag;
    }

    @Nullable
    @Override
    public Entity teleport(TeleportTransition teleporttransition) {
        Entity entity = super.teleport(teleporttransition);

        if (entity instanceof EntityTNTPrimed entitytntprimed) {
            entitytntprimed.setUsedPortal(true);
        }

        return entity;
    }

    @Override
    public final boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        return false;
    }
}
