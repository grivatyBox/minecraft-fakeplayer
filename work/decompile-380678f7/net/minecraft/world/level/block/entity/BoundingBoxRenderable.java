package net.minecraft.world.level.block.entity;

import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;

public interface BoundingBoxRenderable {

    BoundingBoxRenderable.a renderMode();

    BoundingBoxRenderable.b getRenderableBox();

    public static record b(BlockPosition localPos, BaseBlockPosition size) {

        public static BoundingBoxRenderable.b fromCorners(int i, int j, int k, int l, int i1, int j1) {
            int k1 = Math.min(i, l);
            int l1 = Math.min(j, i1);
            int i2 = Math.min(k, j1);

            return new BoundingBoxRenderable.b(new BlockPosition(k1, l1, i2), new BaseBlockPosition(Math.max(i, l) - k1, Math.max(j, i1) - l1, Math.max(k, j1) - i2));
        }
    }

    public static enum a {

        NONE, BOX, BOX_AND_INVISIBLE_BLOCKS;

        private a() {}
    }
}
