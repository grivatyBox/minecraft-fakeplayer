package net.minecraft.world.level.block.entity;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;

public class TrialSpawnerBlockEntity extends TileEntity implements Spawner, TrialSpawner.b {

    public TrialSpawner trialSpawner = this.createDefaultSpawner();

    public TrialSpawnerBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.TRIAL_SPAWNER, blockposition, iblockdata);
    }

    private TrialSpawner createDefaultSpawner() {
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.a playerdetector_a = PlayerDetector.a.SELECT_FROM_LEVEL;

        return new TrialSpawner(this, playerdetector, playerdetector_a);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.trialSpawner = (TrialSpawner) nbttagcompound.read(this.trialSpawner.codec(), holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE)).orElseGet(this::createDefaultSpawner);
        if (this.level != null) {
            this.markUpdated();
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.store(this.trialSpawner.codec(), holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), this.trialSpawner);
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.trialSpawner.getData().getUpdateTag((TrialSpawnerState) this.getBlockState().getValue(TrialSpawnerBlock.STATE));
    }

    @Override
    public void setEntityId(EntityTypes<?> entitytypes, RandomSource randomsource) {
        if (this.level == null) {
            SystemUtils.logAndPauseIfInIde("Expected non-null level");
        } else {
            this.trialSpawner.overrideEntityToSpawn(entitytypes, this.level);
            this.setChanged();
        }
    }

    public TrialSpawner getTrialSpawner() {
        return this.trialSpawner;
    }

    @Override
    public TrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(BlockProperties.TRIAL_SPAWNER_STATE) ? TrialSpawnerState.INACTIVE : (TrialSpawnerState) this.getBlockState().getValue(BlockProperties.TRIAL_SPAWNER_STATE);
    }

    @Override
    public void setState(World world, TrialSpawnerState trialspawnerstate) {
        this.setChanged();
        world.setBlockAndUpdate(this.worldPosition, (IBlockData) this.getBlockState().setValue(BlockProperties.TRIAL_SPAWNER_STATE, trialspawnerstate));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }

    }
}
