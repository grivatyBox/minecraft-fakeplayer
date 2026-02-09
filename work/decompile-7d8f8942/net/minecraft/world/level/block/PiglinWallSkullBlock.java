package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class PiglinWallSkullBlock extends BlockSkullWall {

    public static final MapCodec<PiglinWallSkullBlock> CODEC = simpleCodec(PiglinWallSkullBlock::new);
    private static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateHorizontal(Block.boxZ(10.0D, 8.0D, 8.0D, 16.0D));

    @Override
    public MapCodec<PiglinWallSkullBlock> codec() {
        return PiglinWallSkullBlock.CODEC;
    }

    public PiglinWallSkullBlock(BlockBase.Info blockbase_info) {
        super(BlockSkull.Type.PIGLIN, blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) PiglinWallSkullBlock.SHAPES.get(iblockdata.getValue(PiglinWallSkullBlock.FACING));
    }
}
