package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.block.state.IBlockData;

public class TileEntityComparator extends TileEntity {

    private static final int DEFAULT_OUTPUT = 0;
    private int output = 0;

    public TileEntityComparator(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.COMPARATOR, blockposition, iblockdata);
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.putInt("OutputSignal", this.output);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.output = nbttagcompound.getIntOr("OutputSignal", 0);
    }

    public int getOutputSignal() {
        return this.output;
    }

    public void setOutputSignal(int i) {
        this.output = i;
    }
}
