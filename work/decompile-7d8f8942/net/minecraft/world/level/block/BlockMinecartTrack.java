package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.IBlockState;

public class BlockMinecartTrack extends BlockMinecartTrackAbstract {

    public static final MapCodec<BlockMinecartTrack> CODEC = simpleCodec(BlockMinecartTrack::new);
    public static final BlockStateEnum<BlockPropertyTrackPosition> SHAPE = BlockProperties.RAIL_SHAPE;

    @Override
    public MapCodec<BlockMinecartTrack> codec() {
        return BlockMinecartTrack.CODEC;
    }

    protected BlockMinecartTrack(BlockBase.Info blockbase_info) {
        super(false, blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockMinecartTrack.SHAPE, BlockPropertyTrackPosition.NORTH_SOUTH)).setValue(BlockMinecartTrack.WATERLOGGED, false));
    }

    @Override
    protected void updateState(IBlockData iblockdata, World world, BlockPosition blockposition, Block block) {
        if (block.defaultBlockState().isSignalSource() && (new MinecartTrackLogic(world, blockposition, iblockdata)).countPotentialConnections() == 3) {
            this.updateDir(world, blockposition, iblockdata, false);
        }

    }

    @Override
    public IBlockState<BlockPropertyTrackPosition> getShapeProperty() {
        return BlockMinecartTrack.SHAPE;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(BlockMinecartTrack.SHAPE);
        BlockPropertyTrackPosition blockpropertytrackposition1 = this.rotate(blockpropertytrackposition, enumblockrotation);

        return (IBlockData) iblockdata.setValue(BlockMinecartTrack.SHAPE, blockpropertytrackposition1);
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        BlockPropertyTrackPosition blockpropertytrackposition = (BlockPropertyTrackPosition) iblockdata.getValue(BlockMinecartTrack.SHAPE);
        BlockPropertyTrackPosition blockpropertytrackposition1 = this.mirror(blockpropertytrackposition, enumblockmirror);

        return (IBlockData) iblockdata.setValue(BlockMinecartTrack.SHAPE, blockpropertytrackposition1);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockMinecartTrack.SHAPE, BlockMinecartTrack.WATERLOGGED);
    }
}
