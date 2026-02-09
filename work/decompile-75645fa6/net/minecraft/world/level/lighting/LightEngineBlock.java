package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.SectionPosition;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ILightAccess;
import net.minecraft.world.level.chunk.LightChunk;

public final class LightEngineBlock extends LightEngine<LightEngineStorageBlock.a, LightEngineStorageBlock> {

    private final BlockPosition.MutableBlockPosition mutablePos;

    public LightEngineBlock(ILightAccess ilightaccess) {
        this(ilightaccess, new LightEngineStorageBlock(ilightaccess));
    }

    @VisibleForTesting
    public LightEngineBlock(ILightAccess ilightaccess, LightEngineStorageBlock lightenginestorageblock) {
        super(ilightaccess, lightenginestorageblock);
        this.mutablePos = new BlockPosition.MutableBlockPosition();
    }

    @Override
    protected void checkNode(long i) {
        long j = SectionPosition.blockToSection(i);

        if (((LightEngineStorageBlock) this.storage).storingLightForSection(j)) {
            IBlockData iblockdata = this.getState(this.mutablePos.set(i));
            int k = this.getEmission(i, iblockdata);
            int l = ((LightEngineStorageBlock) this.storage).getStoredLevel(i);

            if (k < l) {
                ((LightEngineStorageBlock) this.storage).setStoredLevel(i, 0);
                this.enqueueDecrease(i, LightEngine.a.decreaseAllDirections(l));
            } else {
                this.enqueueDecrease(i, LightEngineBlock.PULL_LIGHT_IN_ENTRY);
            }

            if (k > 0) {
                this.enqueueIncrease(i, LightEngine.a.increaseLightFromEmission(k, isEmptyShape(iblockdata)));
            }

        }
    }

    @Override
    protected void propagateIncrease(long i, long j, int k) {
        IBlockData iblockdata = null;

        for (EnumDirection enumdirection : LightEngineBlock.PROPAGATION_DIRECTIONS) {
            if (LightEngine.a.shouldPropagateInDirection(j, enumdirection)) {
                long l = BlockPosition.offset(i, enumdirection);

                if (((LightEngineStorageBlock) this.storage).storingLightForSection(SectionPosition.blockToSection(l))) {
                    int i1 = ((LightEngineStorageBlock) this.storage).getStoredLevel(l);
                    int j1 = k - 1;

                    if (j1 > i1) {
                        this.mutablePos.set(l);
                        IBlockData iblockdata1 = this.getState(this.mutablePos);
                        int k1 = k - this.getOpacity(iblockdata1);

                        if (k1 > i1) {
                            if (iblockdata == null) {
                                iblockdata = LightEngine.a.isFromEmptyShape(j) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(i));
                            }

                            if (!this.shapeOccludes(iblockdata, iblockdata1, enumdirection)) {
                                ((LightEngineStorageBlock) this.storage).setStoredLevel(l, k1);
                                if (k1 > 1) {
                                    this.enqueueIncrease(l, LightEngine.a.increaseSkipOneDirection(k1, isEmptyShape(iblockdata1), enumdirection.getOpposite()));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void propagateDecrease(long i, long j) {
        int k = LightEngine.a.getFromLevel(j);

        for (EnumDirection enumdirection : LightEngineBlock.PROPAGATION_DIRECTIONS) {
            if (LightEngine.a.shouldPropagateInDirection(j, enumdirection)) {
                long l = BlockPosition.offset(i, enumdirection);

                if (((LightEngineStorageBlock) this.storage).storingLightForSection(SectionPosition.blockToSection(l))) {
                    int i1 = ((LightEngineStorageBlock) this.storage).getStoredLevel(l);

                    if (i1 != 0) {
                        if (i1 <= k - 1) {
                            IBlockData iblockdata = this.getState(this.mutablePos.set(l));
                            int j1 = this.getEmission(l, iblockdata);

                            ((LightEngineStorageBlock) this.storage).setStoredLevel(l, 0);
                            if (j1 < i1) {
                                this.enqueueDecrease(l, LightEngine.a.decreaseSkipOneDirection(i1, enumdirection.getOpposite()));
                            }

                            if (j1 > 0) {
                                this.enqueueIncrease(l, LightEngine.a.increaseLightFromEmission(j1, isEmptyShape(iblockdata)));
                            }
                        } else {
                            this.enqueueIncrease(l, LightEngine.a.increaseOnlyOneDirection(i1, false, enumdirection.getOpposite()));
                        }
                    }
                }
            }
        }

    }

    private int getEmission(long i, IBlockData iblockdata) {
        int j = iblockdata.getLightEmission();

        return j > 0 && ((LightEngineStorageBlock) this.storage).lightOnInSection(SectionPosition.blockToSection(i)) ? j : 0;
    }

    @Override
    public void propagateLightSources(ChunkCoordIntPair chunkcoordintpair) {
        this.setLightEnabled(chunkcoordintpair, true);
        LightChunk lightchunk = this.chunkSource.getChunkForLighting(chunkcoordintpair.x, chunkcoordintpair.z);

        if (lightchunk != null) {
            lightchunk.findBlockLightSources((blockposition, iblockdata) -> {
                int i = iblockdata.getLightEmission();

                this.enqueueIncrease(blockposition.asLong(), LightEngine.a.increaseLightFromEmission(i, isEmptyShape(iblockdata)));
            });
        }

    }
}
