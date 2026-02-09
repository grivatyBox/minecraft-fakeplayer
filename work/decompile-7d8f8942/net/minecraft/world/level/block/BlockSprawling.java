package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public abstract class BlockSprawling extends Block {

    public static final BlockStateBoolean NORTH = BlockProperties.NORTH;
    public static final BlockStateBoolean EAST = BlockProperties.EAST;
    public static final BlockStateBoolean SOUTH = BlockProperties.SOUTH;
    public static final BlockStateBoolean WEST = BlockProperties.WEST;
    public static final BlockStateBoolean UP = BlockProperties.UP;
    public static final BlockStateBoolean DOWN = BlockProperties.DOWN;
    public static final Map<EnumDirection, BlockStateBoolean> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(EnumDirection.NORTH, BlockSprawling.NORTH, EnumDirection.EAST, BlockSprawling.EAST, EnumDirection.SOUTH, BlockSprawling.SOUTH, EnumDirection.WEST, BlockSprawling.WEST, EnumDirection.UP, BlockSprawling.UP, EnumDirection.DOWN, BlockSprawling.DOWN)));
    private final Function<IBlockData, VoxelShape> shapes;

    protected BlockSprawling(float f, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.shapes = this.makeShapes(f);
    }

    @Override
    protected abstract MapCodec<? extends BlockSprawling> codec();

    private Function<IBlockData, VoxelShape> makeShapes(float f) {
        VoxelShape voxelshape = Block.cube((double) f);
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateAll(Block.boxZ((double) f, 0.0D, 8.0D));

        return this.getShapeForEachState((iblockdata) -> {
            VoxelShape voxelshape1 = voxelshape;

            for (Map.Entry<EnumDirection, BlockStateBoolean> map_entry : BlockSprawling.PROPERTY_BY_DIRECTION.entrySet()) {
                if ((Boolean) iblockdata.getValue((IBlockState) map_entry.getValue())) {
                    voxelshape1 = VoxelShapes.or((VoxelShape) map.get(map_entry.getKey()), voxelshape1);
                }
            }

            return voxelshape1;
        });
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return false;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }
}
