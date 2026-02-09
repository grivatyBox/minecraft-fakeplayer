package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockPotatoes extends BlockCrops {

    public static final MapCodec<BlockPotatoes> CODEC = simpleCodec(BlockPotatoes::new);
    private static final VoxelShape[] SHAPES = Block.boxes(7, (i) -> {
        return Block.column(16.0D, 0.0D, (double) (2 + i));
    });

    @Override
    public MapCodec<BlockPotatoes> codec() {
        return BlockPotatoes.CODEC;
    }

    public BlockPotatoes(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected IMaterial getBaseSeedId() {
        return Items.POTATO;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockPotatoes.SHAPES[this.getAge(iblockdata)];
    }
}
