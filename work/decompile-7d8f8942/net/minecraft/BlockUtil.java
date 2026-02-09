package net.minecraft;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockUtil {

    public BlockUtil() {}

    public static BlockUtil.Rectangle getLargestRectangleAround(BlockPosition blockposition, EnumDirection.EnumAxis enumdirection_enumaxis, int i, EnumDirection.EnumAxis enumdirection_enumaxis1, int j, Predicate<BlockPosition> predicate) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();
        EnumDirection enumdirection = EnumDirection.get(EnumDirection.EnumAxisDirection.NEGATIVE, enumdirection_enumaxis);
        EnumDirection enumdirection1 = enumdirection.getOpposite();
        EnumDirection enumdirection2 = EnumDirection.get(EnumDirection.EnumAxisDirection.NEGATIVE, enumdirection_enumaxis1);
        EnumDirection enumdirection3 = enumdirection2.getOpposite();
        int k = getLimit(predicate, blockposition_mutableblockposition.set(blockposition), enumdirection, i);
        int l = getLimit(predicate, blockposition_mutableblockposition.set(blockposition), enumdirection1, i);
        int i1 = k;
        BlockUtil.IntBounds[] ablockutil_intbounds = new BlockUtil.IntBounds[k + 1 + l];

        ablockutil_intbounds[k] = new BlockUtil.IntBounds(getLimit(predicate, blockposition_mutableblockposition.set(blockposition), enumdirection2, j), getLimit(predicate, blockposition_mutableblockposition.set(blockposition), enumdirection3, j));
        int j1 = ablockutil_intbounds[k].min;

        for (int k1 = 1; k1 <= k; ++k1) {
            BlockUtil.IntBounds blockutil_intbounds = ablockutil_intbounds[i1 - (k1 - 1)];

            ablockutil_intbounds[i1 - k1] = new BlockUtil.IntBounds(getLimit(predicate, blockposition_mutableblockposition.set(blockposition).move(enumdirection, k1), enumdirection2, blockutil_intbounds.min), getLimit(predicate, blockposition_mutableblockposition.set(blockposition).move(enumdirection, k1), enumdirection3, blockutil_intbounds.max));
        }

        for (int l1 = 1; l1 <= l; ++l1) {
            BlockUtil.IntBounds blockutil_intbounds1 = ablockutil_intbounds[i1 + l1 - 1];

            ablockutil_intbounds[i1 + l1] = new BlockUtil.IntBounds(getLimit(predicate, blockposition_mutableblockposition.set(blockposition).move(enumdirection1, l1), enumdirection2, blockutil_intbounds1.min), getLimit(predicate, blockposition_mutableblockposition.set(blockposition).move(enumdirection1, l1), enumdirection3, blockutil_intbounds1.max));
        }

        int i2 = 0;
        int j2 = 0;
        int k2 = 0;
        int l2 = 0;
        int[] aint = new int[ablockutil_intbounds.length];

        for (int i3 = j1; i3 >= 0; --i3) {
            for (int j3 = 0; j3 < ablockutil_intbounds.length; ++j3) {
                BlockUtil.IntBounds blockutil_intbounds2 = ablockutil_intbounds[j3];
                int k3 = j1 - blockutil_intbounds2.min;
                int l3 = j1 + blockutil_intbounds2.max;

                aint[j3] = i3 >= k3 && i3 <= l3 ? l3 + 1 - i3 : 0;
            }

            Pair<BlockUtil.IntBounds, Integer> pair = getMaxRectangleLocation(aint);
            BlockUtil.IntBounds blockutil_intbounds3 = (BlockUtil.IntBounds) pair.getFirst();
            int i4 = 1 + blockutil_intbounds3.max - blockutil_intbounds3.min;
            int j4 = (Integer) pair.getSecond();

            if (i4 * j4 > k2 * l2) {
                i2 = blockutil_intbounds3.min;
                j2 = i3;
                k2 = i4;
                l2 = j4;
            }
        }

        return new BlockUtil.Rectangle(blockposition.relative(enumdirection_enumaxis, i2 - i1).relative(enumdirection_enumaxis1, j2 - j1), k2, l2);
    }

    private static int getLimit(Predicate<BlockPosition> predicate, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, EnumDirection enumdirection, int i) {
        int j;

        for (j = 0; j < i && predicate.test(blockposition_mutableblockposition.move(enumdirection)); ++j) {
            ;
        }

        return j;
    }

    @VisibleForTesting
    static Pair<BlockUtil.IntBounds, Integer> getMaxRectangleLocation(int[] aint) {
        int i = 0;
        int j = 0;
        int k = 0;
        IntStack intstack = new IntArrayList();

        intstack.push(0);

        for (int l = 1; l <= aint.length; ++l) {
            int i1 = l == aint.length ? 0 : aint[l];

            while (!((IntStack) intstack).isEmpty()) {
                int j1 = aint[intstack.topInt()];

                if (i1 >= j1) {
                    intstack.push(l);
                    break;
                }

                intstack.popInt();
                int k1 = intstack.isEmpty() ? 0 : intstack.topInt() + 1;

                if (j1 * (l - k1) > k * (j - i)) {
                    j = l;
                    i = k1;
                    k = j1;
                }
            }

            if (intstack.isEmpty()) {
                intstack.push(l);
            }
        }

        return new Pair(new BlockUtil.IntBounds(i, j - 1), k);
    }

    public static Optional<BlockPosition> getTopConnectedBlock(IBlockAccess iblockaccess, BlockPosition blockposition, Block block, EnumDirection enumdirection, Block block1) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        IBlockData iblockdata;

        do {
            blockposition_mutableblockposition.move(enumdirection);
            iblockdata = iblockaccess.getBlockState(blockposition_mutableblockposition);
        } while (iblockdata.is(block));

        return iblockdata.is(block1) ? Optional.of(blockposition_mutableblockposition) : Optional.empty();
    }

    public static class IntBounds {

        public final int min;
        public final int max;

        public IntBounds(int i, int j) {
            this.min = i;
            this.max = j;
        }

        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
        }
    }

    public static class Rectangle {

        public final BlockPosition minCorner;
        public final int axis1Size;
        public final int axis2Size;

        public Rectangle(BlockPosition blockposition, int i, int j) {
            this.minCorner = blockposition;
            this.axis1Size = i;
            this.axis2Size = j;
        }
    }
}
