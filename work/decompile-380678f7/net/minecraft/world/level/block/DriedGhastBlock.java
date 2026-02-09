package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class DriedGhastBlock extends BlockFacingHorizontal implements IBlockWaterlogged {

    public static final MapCodec<DriedGhastBlock> CODEC = simpleCodec(DriedGhastBlock::new);
    public static final int MAX_HYDRATION_LEVEL = 3;
    public static final BlockStateInteger HYDRATION_LEVEL = BlockProperties.DRIED_GHAST_HYDRATION_LEVELS;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    public static final int HYDRATION_TICK_DELAY = 5000;
    private static final VoxelShape SHAPE = Block.column(10.0D, 10.0D, 0.0D, 10.0D);

    @Override
    public MapCodec<DriedGhastBlock> codec() {
        return DriedGhastBlock.CODEC;
    }

    public DriedGhastBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(DriedGhastBlock.FACING, EnumDirection.NORTH)).setValue(DriedGhastBlock.HYDRATION_LEVEL, 0)).setValue(DriedGhastBlock.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(DriedGhastBlock.FACING, DriedGhastBlock.HYDRATION_LEVEL, DriedGhastBlock.WATERLOGGED);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(DriedGhastBlock.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return DriedGhastBlock.SHAPE;
    }

    public int getHydrationLevel(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(DriedGhastBlock.HYDRATION_LEVEL);
    }

    private boolean isReadyToSpawn(IBlockData iblockdata) {
        return this.getHydrationLevel(iblockdata) == 3;
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(DriedGhastBlock.WATERLOGGED)) {
            this.tickWaterlogged(iblockdata, worldserver, blockposition, randomsource);
        } else {
            int i = this.getHydrationLevel(iblockdata);

            if (i > 0) {
                worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(DriedGhastBlock.HYDRATION_LEVEL, i - 1), 2);
                worldserver.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(iblockdata));
            }

        }
    }

    private void tickWaterlogged(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!this.isReadyToSpawn(iblockdata)) {
            worldserver.playSound((Entity) null, blockposition, SoundEffects.DRIED_GHAST_TRANSITION, SoundCategory.BLOCKS, 1.0F, 1.0F);
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(DriedGhastBlock.HYDRATION_LEVEL, this.getHydrationLevel(iblockdata) + 1), 2);
            worldserver.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(iblockdata));
        } else {
            this.spawnGhastling(worldserver, blockposition, iblockdata);
        }

    }

    private void spawnGhastling(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata) {
        worldserver.removeBlock(blockposition, false);
        HappyGhast happyghast = EntityTypes.HAPPY_GHAST.create(worldserver, EntitySpawnReason.BREEDING);

        if (happyghast != null) {
            Vec3D vec3d = blockposition.getBottomCenter();

            happyghast.setBaby(true);
            float f = EnumDirection.getYRot((EnumDirection) iblockdata.getValue(DriedGhastBlock.FACING));

            happyghast.setYHeadRot(f);
            happyghast.snapTo(vec3d.x(), vec3d.y(), vec3d.z(), f, 0.0F);
            worldserver.addFreshEntity(happyghast);
            worldserver.playSound((Entity) null, (Entity) happyghast, SoundEffects.GHASTLING_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        double d0 = (double) blockposition.getX() + 0.5D;
        double d1 = (double) blockposition.getY() + 0.5D;
        double d2 = (double) blockposition.getZ() + 0.5D;

        if (!(Boolean) iblockdata.getValue(DriedGhastBlock.WATERLOGGED)) {
            if (randomsource.nextInt(40) == 0 && world.getBlockState(blockposition.below()).is(TagsBlock.TRIGGERS_AMBIENT_DRIED_GHAST_BLOCK_SOUNDS)) {
                world.playLocalSound(d0, d1, d2, SoundEffects.DRIED_GHAST_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            if (randomsource.nextInt(6) == 0) {
                world.addParticle(Particles.WHITE_SMOKE, d0, d1, d2, 0.0D, 0.02D, 0.0D);
            }
        } else {
            if (randomsource.nextInt(40) == 0) {
                world.playLocalSound(d0, d1, d2, SoundEffects.DRIED_GHAST_AMBIENT_WATER, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            if (randomsource.nextInt(6) == 0) {
                world.addParticle(Particles.HAPPY_VILLAGER, d0 + (double) ((randomsource.nextFloat() * 2.0F - 1.0F) / 3.0F), d1 + 0.4D, d2 + (double) ((randomsource.nextFloat() * 2.0F - 1.0F) / 3.0F), 0.0D, (double) randomsource.nextFloat(), 0.0D);
            }
        }

    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (((Boolean) iblockdata.getValue(DriedGhastBlock.WATERLOGGED) || (Integer) iblockdata.getValue(DriedGhastBlock.HYDRATION_LEVEL) > 0) && !worldserver.getBlockTicks().hasScheduledTick(blockposition, this)) {
            worldserver.scheduleTick(blockposition, (Block) this, 5000);
        }

    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
        boolean flag = fluid.getType() == FluidTypes.WATER;

        return (IBlockData) ((IBlockData) super.getStateForPlacement(blockactioncontext).setValue(DriedGhastBlock.WATERLOGGED, flag)).setValue(DriedGhastBlock.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(DriedGhastBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    public boolean placeLiquid(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, Fluid fluid) {
        if (!(Boolean) iblockdata.getValue(BlockProperties.WATERLOGGED) && fluid.getType() == FluidTypes.WATER) {
            if (!generatoraccess.isClientSide()) {
                generatoraccess.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockProperties.WATERLOGGED, true), 3);
                generatoraccess.scheduleTick(blockposition, fluid.getType(), fluid.getType().getTickDelay(generatoraccess));
                generatoraccess.playSound((Entity) null, blockposition, SoundEffects.DRIED_GHAST_PLACE_IN_WATER, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPosition blockposition, IBlockData iblockdata, @Nullable EntityLiving entityliving, ItemStack itemstack) {
        super.setPlacedBy(world, blockposition, iblockdata, entityliving, itemstack);
        world.playSound((Entity) null, blockposition, (Boolean) iblockdata.getValue(DriedGhastBlock.WATERLOGGED) ? SoundEffects.DRIED_GHAST_PLACE_IN_WATER : SoundEffects.DRIED_GHAST_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
