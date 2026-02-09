package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.IInventory;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public abstract class TileEntity implements DebugValueSource {

    private static final Codec<TileEntityTypes<?>> TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TileEntityTypes<?> type;
    @Nullable
    protected World level;
    protected final BlockPosition worldPosition;
    protected boolean remove;
    private IBlockData blockState;
    private DataComponentMap components;

    public TileEntity(TileEntityTypes<?> tileentitytypes, BlockPosition blockposition, IBlockData iblockdata) {
        this.components = DataComponentMap.EMPTY;
        this.type = tileentitytypes;
        this.worldPosition = blockposition.immutable();
        this.validateBlockState(iblockdata);
        this.blockState = iblockdata;
    }

    private void validateBlockState(IBlockData iblockdata) {
        if (!this.isValidBlockState(iblockdata)) {
            String s = this.getNameForReporting();

            throw new IllegalStateException("Invalid block entity " + s + " state at " + String.valueOf(this.worldPosition) + ", got " + String.valueOf(iblockdata));
        }
    }

    public boolean isValidBlockState(IBlockData iblockdata) {
        return this.type.isValid(iblockdata);
    }

    public static BlockPosition getPosFromTag(ChunkCoordIntPair chunkcoordintpair, NBTTagCompound nbttagcompound) {
        int i = nbttagcompound.getIntOr("x", 0);
        int j = nbttagcompound.getIntOr("y", 0);
        int k = nbttagcompound.getIntOr("z", 0);
        int l = SectionPosition.blockToSectionCoord(i);
        int i1 = SectionPosition.blockToSectionCoord(k);

        if (l != chunkcoordintpair.x || i1 != chunkcoordintpair.z) {
            TileEntity.LOGGER.warn("Block entity {} found in a wrong chunk, expected position from chunk {}", nbttagcompound, chunkcoordintpair);
            i = chunkcoordintpair.getBlockX(SectionPosition.sectionRelative(i));
            k = chunkcoordintpair.getBlockZ(SectionPosition.sectionRelative(k));
        }

        return new BlockPosition(i, j, k);
    }

    @Nullable
    public World getLevel() {
        return this.level;
    }

    public void setLevel(World world) {
        this.level = world;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    protected void loadAdditional(ValueInput valueinput) {}

    public final void loadWithComponents(ValueInput valueinput) {
        this.loadAdditional(valueinput);
        this.components = (DataComponentMap) valueinput.read("components", DataComponentMap.CODEC).orElse(DataComponentMap.EMPTY);
    }

    public final void loadCustomOnly(ValueInput valueinput) {
        this.loadAdditional(valueinput);
    }

    protected void saveAdditional(ValueOutput valueoutput) {}

    public final NBTTagCompound saveWithFullMetadata(HolderLookup.a holderlookup_a) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(this.problemPath(), TileEntity.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, holderlookup_a);

            this.saveWithFullMetadata((ValueOutput) tagvalueoutput);
            return tagvalueoutput.buildResult();
        }
    }

    public void saveWithFullMetadata(ValueOutput valueoutput) {
        this.saveWithoutMetadata(valueoutput);
        this.saveMetadata(valueoutput);
    }

    public void saveWithId(ValueOutput valueoutput) {
        this.saveWithoutMetadata(valueoutput);
        this.saveId(valueoutput);
    }

    public final NBTTagCompound saveWithoutMetadata(HolderLookup.a holderlookup_a) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(this.problemPath(), TileEntity.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, holderlookup_a);

            this.saveWithoutMetadata((ValueOutput) tagvalueoutput);
            return tagvalueoutput.buildResult();
        }
    }

    public void saveWithoutMetadata(ValueOutput valueoutput) {
        this.saveAdditional(valueoutput);
        valueoutput.store("components", DataComponentMap.CODEC, this.components);
    }

    public final NBTTagCompound saveCustomOnly(HolderLookup.a holderlookup_a) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(this.problemPath(), TileEntity.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, holderlookup_a);

            this.saveCustomOnly((ValueOutput) tagvalueoutput);
            return tagvalueoutput.buildResult();
        }
    }

    public void saveCustomOnly(ValueOutput valueoutput) {
        this.saveAdditional(valueoutput);
    }

    private void saveId(ValueOutput valueoutput) {
        addEntityType(valueoutput, this.getType());
    }

    public static void addEntityType(ValueOutput valueoutput, TileEntityTypes<?> tileentitytypes) {
        valueoutput.store("id", TileEntity.TYPE_CODEC, tileentitytypes);
    }

    private void saveMetadata(ValueOutput valueoutput) {
        this.saveId(valueoutput);
        valueoutput.putInt("x", this.worldPosition.getX());
        valueoutput.putInt("y", this.worldPosition.getY());
        valueoutput.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static TileEntity loadStatic(BlockPosition blockposition, IBlockData iblockdata, NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        TileEntityTypes<?> tileentitytypes = (TileEntityTypes) nbttagcompound.read("id", TileEntity.TYPE_CODEC).orElse((Object) null);

        if (tileentitytypes == null) {
            TileEntity.LOGGER.error("Skipping block entity with invalid type: {}", nbttagcompound.get("id"));
            return null;
        } else {
            TileEntity tileentity;

            try {
                tileentity = tileentitytypes.create(blockposition, iblockdata);
            } catch (Throwable throwable) {
                TileEntity.LOGGER.error("Failed to create block entity {} for block {} at position {} ", new Object[]{tileentitytypes, blockposition, iblockdata, throwable});
                return null;
            }

            try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(tileentity.problemPath(), TileEntity.LOGGER)) {
                tileentity.loadWithComponents(TagValueInput.create(problemreporter_j, holderlookup_a, nbttagcompound));
                return tileentity;
            } catch (Throwable throwable1) {
                TileEntity.LOGGER.error("Failed to load data for block entity {} for block {} at position {}", new Object[]{tileentitytypes, blockposition, iblockdata, throwable1});
                return null;
            }
        }
    }

    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.blockState);
        }

    }

    protected static void setChanged(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.blockEntityChanged(blockposition);
        if (!iblockdata.isAir()) {
            world.updateNeighbourForOutputSignal(blockposition, iblockdata.getBlock());
        }

    }

    public BlockPosition getBlockPos() {
        return this.worldPosition;
    }

    public IBlockData getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<PacketListenerPlayOut> getUpdatePacket() {
        return null;
    }

    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return new NBTTagCompound();
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
    }

    public void clearRemoved() {
        this.remove = false;
    }

    public void preRemoveSideEffects(BlockPosition blockposition, IBlockData iblockdata) {
        if (this instanceof IInventory iinventory) {
            if (this.level != null) {
                InventoryUtils.dropContents(this.level, blockposition, iinventory);
            }
        }

    }

    public boolean triggerEvent(int i, int j) {
        return false;
    }

    public void fillCrashReportCategory(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.setDetail("Name", this::getNameForReporting);
        IBlockData iblockdata = this.getBlockState();

        Objects.requireNonNull(iblockdata);
        crashreportsystemdetails.setDetail("Cached block", iblockdata::toString);
        if (this.level == null) {
            crashreportsystemdetails.setDetail("Block location", () -> {
                return String.valueOf(this.worldPosition) + " (world missing)";
            });
        } else {
            iblockdata = this.level.getBlockState(this.worldPosition);
            Objects.requireNonNull(iblockdata);
            crashreportsystemdetails.setDetail("Actual block", iblockdata::toString);
            CrashReportSystemDetails.populateBlockLocationDetails(crashreportsystemdetails, this.level, this.worldPosition);
        }

    }

    public String getNameForReporting() {
        String s = String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()));

        return s + " // " + this.getClass().getCanonicalName();
    }

    public TileEntityTypes<?> getType() {
        return this.type;
    }

    /** @deprecated */
    @Deprecated
    public void setBlockState(IBlockData iblockdata) {
        this.validateBlockState(iblockdata);
        this.blockState = iblockdata;
    }

    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {}

    public final void applyComponentsFromItemStack(ItemStack itemstack) {
        this.applyComponents(itemstack.getPrototype(), itemstack.getComponentsPatch());
    }

    public final void applyComponents(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        final Set<DataComponentType<?>> set = new HashSet();

        set.add(DataComponents.BLOCK_ENTITY_DATA);
        set.add(DataComponents.BLOCK_STATE);
        final DataComponentMap datacomponentmap1 = PatchedDataComponentMap.fromPatch(datacomponentmap, datacomponentpatch);

        this.applyImplicitComponents(new DataComponentGetter() {
            @Nullable
            @Override
            public <T> T get(DataComponentType<? extends T> datacomponenttype) {
                set.add(datacomponenttype);
                return (T) datacomponentmap1.get(datacomponenttype);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0) {
                set.add(datacomponenttype);
                return (T) datacomponentmap1.getOrDefault(datacomponenttype, t0);
            }
        });
        Objects.requireNonNull(set);
        DataComponentPatch datacomponentpatch1 = datacomponentpatch.forget(set::contains);

        this.components = datacomponentpatch1.split().added();
    }

    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {}

    /** @deprecated */
    @Deprecated
    public void removeComponentsFromTag(ValueOutput valueoutput) {}

    public final DataComponentMap collectComponents() {
        DataComponentMap.a datacomponentmap_a = DataComponentMap.builder();

        datacomponentmap_a.addAll(this.components);
        this.collectImplicitComponents(datacomponentmap_a);
        return datacomponentmap_a.build();
    }

    public DataComponentMap components() {
        return this.components;
    }

    public void setComponents(DataComponentMap datacomponentmap) {
        this.components = datacomponentmap;
    }

    @Nullable
    public static IChatBaseComponent parseCustomNameSafe(ValueInput valueinput, String s) {
        return (IChatBaseComponent) valueinput.read(s, ComponentSerialization.CODEC).orElse((Object) null);
    }

    public ProblemReporter.f problemPath() {
        return new TileEntity.a(this);
    }

    @Override
    public void registerDebugValues(WorldServer worldserver, DebugValueSource.a debugvaluesource_a) {}

    private static record a(TileEntity blockEntity) implements ProblemReporter.f {

        @Override
        public String get() {
            String s = this.blockEntity.getNameForReporting();

            return s + "@" + String.valueOf(this.blockEntity.getBlockPos());
        }
    }
}
