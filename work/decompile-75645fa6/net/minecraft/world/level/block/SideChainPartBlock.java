package net.minecraft.world.level.block;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.SideChainPart;

public interface SideChainPartBlock {

    SideChainPart getSideChainPart(IBlockData iblockdata);

    IBlockData setSideChainPart(IBlockData iblockdata, SideChainPart sidechainpart);

    EnumDirection getFacing(IBlockData iblockdata);

    boolean isConnectable(IBlockData iblockdata);

    int getMaxChainLength();

    default List<BlockPosition> getAllBlocksConnectedTo(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);

        if (!this.isConnectable(iblockdata)) {
            return List.of();
        } else {
            SideChainPartBlock.c sidechainpartblock_c = this.getNeighbors(generatoraccess, blockposition, this.getFacing(iblockdata));
            List<BlockPosition> list = new LinkedList();

            list.add(blockposition);
            Objects.requireNonNull(sidechainpartblock_c);
            IntFunction intfunction = sidechainpartblock_c::left;
            SideChainPart sidechainpart = SideChainPart.LEFT;

            Objects.requireNonNull(list);
            this.addBlocksConnectingTowards(intfunction, sidechainpart, list::addFirst);
            Objects.requireNonNull(sidechainpartblock_c);
            intfunction = sidechainpartblock_c::right;
            sidechainpart = SideChainPart.RIGHT;
            Objects.requireNonNull(list);
            this.addBlocksConnectingTowards(intfunction, sidechainpart, list::addLast);
            return list;
        }
    }

    private void addBlocksConnectingTowards(IntFunction<SideChainPartBlock.b> intfunction, SideChainPart sidechainpart, Consumer<BlockPosition> consumer) {
        for (int i = 1; i < this.getMaxChainLength(); ++i) {
            SideChainPartBlock.b sidechainpartblock_b = (SideChainPartBlock.b) intfunction.apply(i);

            if (sidechainpartblock_b.connectsTowards(sidechainpart)) {
                consumer.accept(sidechainpartblock_b.pos());
            }

            if (sidechainpartblock_b.isUnconnectableOrChainEnd()) {
                break;
            }
        }

    }

    default void updateNeighborsAfterPoweringDown(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        SideChainPartBlock.c sidechainpartblock_c = this.getNeighbors(generatoraccess, blockposition, this.getFacing(iblockdata));

        sidechainpartblock_c.left().disconnectFromRight();
        sidechainpartblock_c.right().disconnectFromLeft();
    }

    default void updateSelfAndNeighborsOnPoweringUp(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {
        if (this.isConnectable(iblockdata)) {
            if (!this.isBeingUpdatedByNeighbor(iblockdata, iblockdata1)) {
                SideChainPartBlock.c sidechainpartblock_c = this.getNeighbors(generatoraccess, blockposition, this.getFacing(iblockdata));
                SideChainPart sidechainpart = SideChainPart.UNCONNECTED;
                int i = sidechainpartblock_c.left().isConnectable() ? this.getAllBlocksConnectedTo(generatoraccess, sidechainpartblock_c.left().pos()).size() : 0;
                int j = sidechainpartblock_c.right().isConnectable() ? this.getAllBlocksConnectedTo(generatoraccess, sidechainpartblock_c.right().pos()).size() : 0;
                int k = 1;

                if (this.canConnect(i, k)) {
                    sidechainpart = sidechainpart.whenConnectedToTheLeft();
                    sidechainpartblock_c.left().connectToTheRight();
                    k += i;
                }

                if (this.canConnect(j, k)) {
                    sidechainpart = sidechainpart.whenConnectedToTheRight();
                    sidechainpartblock_c.right().connectToTheLeft();
                }

                this.setPart(generatoraccess, blockposition, sidechainpart);
            }
        }
    }

    private boolean canConnect(int i, int j) {
        return i > 0 && j + i <= this.getMaxChainLength();
    }

    private boolean isBeingUpdatedByNeighbor(IBlockData iblockdata, IBlockData iblockdata1) {
        boolean flag = this.getSideChainPart(iblockdata).isConnected();
        boolean flag1 = this.isConnectable(iblockdata1) && this.getSideChainPart(iblockdata1).isConnected();

        return flag || flag1;
    }

    private SideChainPartBlock.c getNeighbors(GeneratorAccess generatoraccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return new SideChainPartBlock.c(this, generatoraccess, enumdirection, blockposition, new HashMap());
    }

    default void setPart(GeneratorAccess generatoraccess, BlockPosition blockposition, SideChainPart sidechainpart) {
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);

        if (this.getSideChainPart(iblockdata) != sidechainpart) {
            generatoraccess.setBlock(blockposition, this.setSideChainPart(iblockdata, sidechainpart), 3);
        }

    }

    public static record c(SideChainPartBlock block, GeneratorAccess level, EnumDirection facing, BlockPosition center, Map<BlockPosition, SideChainPartBlock.b> cache) {

        private boolean isConnectableToThisBlock(IBlockData iblockdata) {
            return this.block.isConnectable(iblockdata) && this.block.getFacing(iblockdata) == this.facing;
        }

        private SideChainPartBlock.b createNewNeighbor(BlockPosition blockposition) {
            IBlockData iblockdata = this.level.getBlockState(blockposition);
            SideChainPart sidechainpart = this.isConnectableToThisBlock(iblockdata) ? this.block.getSideChainPart(iblockdata) : null;

            return (SideChainPartBlock.b) (sidechainpart == null ? new SideChainPartBlock.a(blockposition) : new SideChainPartBlock.d(this.level, this.block, blockposition, sidechainpart));
        }

        private SideChainPartBlock.b getOrCreateNeighbor(EnumDirection enumdirection, Integer integer) {
            return (SideChainPartBlock.b) this.cache.computeIfAbsent(this.center.relative(enumdirection, integer), this::createNewNeighbor);
        }

        public SideChainPartBlock.b left(int i) {
            return this.getOrCreateNeighbor(this.facing.getClockWise(), i);
        }

        public SideChainPartBlock.b right(int i) {
            return this.getOrCreateNeighbor(this.facing.getCounterClockWise(), i);
        }

        public SideChainPartBlock.b left() {
            return this.left(1);
        }

        public SideChainPartBlock.b right() {
            return this.right(1);
        }
    }

    public sealed interface b permits SideChainPartBlock.a, SideChainPartBlock.d {

        BlockPosition pos();

        boolean isConnectable();

        boolean isUnconnectableOrChainEnd();

        boolean connectsTowards(SideChainPart sidechainpart);

        default void connectToTheRight() {}

        default void connectToTheLeft() {}

        default void disconnectFromRight() {}

        default void disconnectFromLeft() {}
    }

    public static record a(BlockPosition pos) implements SideChainPartBlock.b {

        @Override
        public boolean isConnectable() {
            return false;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return true;
        }

        @Override
        public boolean connectsTowards(SideChainPart sidechainpart) {
            return false;
        }
    }

    public static record d(GeneratorAccess level, SideChainPartBlock block, BlockPosition pos, SideChainPart part) implements SideChainPartBlock.b {

        @Override
        public boolean isConnectable() {
            return true;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return this.part.isChainEnd();
        }

        @Override
        public boolean connectsTowards(SideChainPart sidechainpart) {
            return this.part.isConnectionTowards(sidechainpart);
        }

        @Override
        public void connectToTheRight() {
            this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheRight());
        }

        @Override
        public void connectToTheLeft() {
            this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheLeft());
        }

        @Override
        public void disconnectFromRight() {
            this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheRight());
        }

        @Override
        public void disconnectFromLeft() {
            this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheLeft());
        }
    }
}
