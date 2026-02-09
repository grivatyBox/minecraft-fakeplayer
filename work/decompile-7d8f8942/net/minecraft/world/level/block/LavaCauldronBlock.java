package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class LavaCauldronBlock extends AbstractCauldronBlock {

    public static final MapCodec<LavaCauldronBlock> CODEC = simpleCodec(LavaCauldronBlock::new);
    private static final VoxelShape SHAPE_INSIDE = Block.column(12.0D, 4.0D, 15.0D);
    private static final VoxelShape FILLED_SHAPE = VoxelShapes.or(AbstractCauldronBlock.SHAPE, LavaCauldronBlock.SHAPE_INSIDE);

    @Override
    public MapCodec<LavaCauldronBlock> codec() {
        return LavaCauldronBlock.CODEC;
    }

    public LavaCauldronBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info, CauldronInteraction.LAVA);
    }

    @Override
    protected double getContentHeight(IBlockData iblockdata) {
        return 0.9375D;
    }

    @Override
    public boolean isFull(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, Entity entity) {
        return LavaCauldronBlock.FILLED_SHAPE;
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity, InsideBlockEffectApplier insideblockeffectapplier) {
        insideblockeffectapplier.apply(InsideBlockEffectType.LAVA_IGNITE);
        insideblockeffectapplier.runAfter(InsideBlockEffectType.LAVA_IGNITE, Entity::lavaHurt);
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return 3;
    }
}
