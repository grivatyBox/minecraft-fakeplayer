package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntityComparator extends TileEntity {

    private static final int DEFAULT_OUTPUT = 0;
    private int output = 0;

    public TileEntityComparator(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.COMPARATOR, blockposition, iblockdata);
    }

    @Override
    protected void saveAdditional(ValueOutput valueoutput) {
        super.saveAdditional(valueoutput);
        valueoutput.putInt("OutputSignal", this.output);
    }

    @Override
    protected void loadAdditional(ValueInput valueinput) {
        super.loadAdditional(valueinput);
        this.output = valueinput.getIntOr("OutputSignal", 0);
    }

    public int getOutputSignal() {
        return this.output;
    }

    public void setOutputSignal(int i) {
        this.output = i;
    }
}
