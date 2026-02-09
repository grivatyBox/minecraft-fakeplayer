package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import org.slf4j.Logger;

public class TestBlockEntity extends TileEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_MESSAGE = "";
    private static final boolean DEFAULT_POWERED = false;
    private TestBlockMode mode;
    private String message = "";
    private boolean powered = false;
    private boolean triggered;

    public TestBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.TEST_BLOCK, blockposition, iblockdata);
        this.mode = (TestBlockMode) iblockdata.getValue(TestBlock.MODE);
    }

    @Override
    public void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        nbttagcompound.store("mode", TestBlockMode.CODEC, this.mode);
        nbttagcompound.putString("message", this.message);
        nbttagcompound.putBoolean("powered", this.powered);
    }

    @Override
    public void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        this.mode = (TestBlockMode) nbttagcompound.read("mode", TestBlockMode.CODEC).orElse(TestBlockMode.FAIL);
        this.message = nbttagcompound.getStringOr("message", "");
        this.powered = nbttagcompound.getBooleanOr("powered", false);
    }

    private void updateBlockState() {
        if (this.level != null) {
            BlockPosition blockposition = this.getBlockPos();
            IBlockData iblockdata = this.level.getBlockState(blockposition);

            if (iblockdata.is(Blocks.TEST_BLOCK)) {
                this.level.setBlock(blockposition, (IBlockData) iblockdata.setValue(TestBlock.MODE, this.mode), 2);
            }

        }
    }

    @Nullable
    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean flag) {
        this.powered = flag;
    }

    public TestBlockMode getMode() {
        return this.mode;
    }

    public void setMode(TestBlockMode testblockmode) {
        this.mode = testblockmode;
        this.updateBlockState();
    }

    private Block getBlockType() {
        return this.getBlockState().getBlock();
    }

    public void reset() {
        this.triggered = false;
        if (this.mode == TestBlockMode.START && this.level != null) {
            this.setPowered(false);
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockType());
        }

    }

    public void trigger() {
        if (this.mode == TestBlockMode.START && this.level != null) {
            this.setPowered(true);
            BlockPosition blockposition = this.getBlockPos();

            this.level.updateNeighborsAt(blockposition, this.getBlockType());
            this.level.getBlockTicks().willTickThisTick(blockposition, this.getBlockType());
            this.log();
        } else {
            if (this.mode == TestBlockMode.LOG) {
                this.log();
            }

            this.triggered = true;
        }
    }

    public void log() {
        if (!this.message.isBlank()) {
            TestBlockEntity.LOGGER.info("Test {} (at {}): {}", new Object[]{this.mode.getSerializedName(), this.getBlockPos(), this.message});
        }

    }

    public boolean hasTriggered() {
        return this.triggered;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String s) {
        this.message = s;
    }
}
