package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryBlockID;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FluidType {

    public static final RegistryBlockID<Fluid> FLUID_STATE_REGISTRY = new RegistryBlockID<Fluid>();
    protected final BlockStateList<FluidType, Fluid> stateDefinition;
    private Fluid defaultFluidState;
    private final Holder.c<FluidType> builtInRegistryHolder;

    protected FluidType() {
        this.builtInRegistryHolder = BuiltInRegistries.FLUID.createIntrusiveHolder(this);
        BlockStateList.a<FluidType, Fluid> blockstatelist_a = new BlockStateList.a<FluidType, Fluid>(this);

        this.createFluidStateDefinition(blockstatelist_a);
        this.stateDefinition = blockstatelist_a.create(FluidType::defaultFluidState, Fluid::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    protected void createFluidStateDefinition(BlockStateList.a<FluidType, Fluid> blockstatelist_a) {}

    public BlockStateList<FluidType, Fluid> getStateDefinition() {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(Fluid fluid) {
        this.defaultFluidState = fluid;
    }

    public final Fluid defaultFluidState() {
        return this.defaultFluidState;
    }

    public abstract Item getBucket();

    protected void animateTick(World world, BlockPosition blockposition, Fluid fluid, RandomSource randomsource) {}

    protected void tick(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {}

    protected void randomTick(WorldServer worldserver, BlockPosition blockposition, Fluid fluid, RandomSource randomsource) {}

    protected void entityInside(World world, BlockPosition blockposition, Entity entity, InsideBlockEffectApplier insideblockeffectapplier) {}

    @Nullable
    protected ParticleParam getDripParticle() {
        return null;
    }

    protected abstract boolean canBeReplacedWith(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition, FluidType fluidtype, EnumDirection enumdirection);

    protected abstract Vec3D getFlow(IBlockAccess iblockaccess, BlockPosition blockposition, Fluid fluid);

    public abstract int getTickDelay(IWorldReader iworldreader);

    protected boolean isRandomlyTicking() {
        return false;
    }

    protected boolean isEmpty() {
        return false;
    }

    protected abstract float getExplosionResistance();

    public abstract float getHeight(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition);

    public abstract float getOwnHeight(Fluid fluid);

    protected abstract IBlockData createLegacyBlock(Fluid fluid);

    public abstract boolean isSource(Fluid fluid);

    public abstract int getAmount(Fluid fluid);

    public boolean isSame(FluidType fluidtype) {
        return fluidtype == this;
    }

    /** @deprecated */
    @Deprecated
    public boolean is(TagKey<FluidType> tagkey) {
        return this.builtInRegistryHolder.is(tagkey);
    }

    public abstract VoxelShape getShape(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition);

    @Nullable
    public AxisAlignedBB getAABB(Fluid fluid, IBlockAccess iblockaccess, BlockPosition blockposition) {
        if (this.isEmpty()) {
            return null;
        } else {
            float f = fluid.getHeight(iblockaccess, blockposition);

            return new AxisAlignedBB((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), (double) blockposition.getX() + 1.0D, (double) ((float) blockposition.getY() + f), (double) blockposition.getZ() + 1.0D);
        }
    }

    public Optional<SoundEffect> getPickupSound() {
        return Optional.empty();
    }

    /** @deprecated */
    @Deprecated
    public Holder.c<FluidType> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }
}
