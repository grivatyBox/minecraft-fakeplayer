package net.minecraft.commands.arguments.blocks;

import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public class ArgumentTileLocation implements Predicate<ShapeDetectorBlock> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final IBlockData state;
    private final Set<IBlockState<?>> properties;
    @Nullable
    private final NBTTagCompound tag;

    public ArgumentTileLocation(IBlockData iblockdata, Set<IBlockState<?>> set, @Nullable NBTTagCompound nbttagcompound) {
        this.state = iblockdata;
        this.properties = set;
        this.tag = nbttagcompound;
    }

    public IBlockData getState() {
        return this.state;
    }

    public Set<IBlockState<?>> getDefinedProperties() {
        return this.properties;
    }

    public boolean test(ShapeDetectorBlock shapedetectorblock) {
        IBlockData iblockdata = shapedetectorblock.getState();

        if (!iblockdata.is(this.state.getBlock())) {
            return false;
        } else {
            for (IBlockState<?> iblockstate : this.properties) {
                if (iblockdata.getValue(iblockstate) != this.state.getValue(iblockstate)) {
                    return false;
                }
            }

            if (this.tag == null) {
                return true;
            } else {
                TileEntity tileentity = shapedetectorblock.getEntity();

                return tileentity != null && GameProfileSerializer.compareNbt(this.tag, tileentity.saveWithFullMetadata((HolderLookup.a) shapedetectorblock.getLevel().registryAccess()), true);
            }
        }
    }

    public boolean test(WorldServer worldserver, BlockPosition blockposition) {
        return this.test(new ShapeDetectorBlock(worldserver, blockposition, false));
    }

    public boolean place(WorldServer worldserver, BlockPosition blockposition, int i) {
        IBlockData iblockdata = (i & 16) != 0 ? this.state : Block.updateFromNeighbourShapes(this.state, worldserver, blockposition);

        if (iblockdata.isAir()) {
            iblockdata = this.state;
        }

        iblockdata = this.overwriteWithDefinedProperties(iblockdata);
        boolean flag = false;

        if (worldserver.setBlock(blockposition, iblockdata, i)) {
            flag = true;
        }

        if (this.tag != null) {
            TileEntity tileentity = worldserver.getBlockEntity(blockposition);

            if (tileentity != null) {
                try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(ArgumentTileLocation.LOGGER)) {
                    HolderLookup.a holderlookup_a = worldserver.registryAccess();
                    ProblemReporter problemreporter = problemreporter_j.forChild(tileentity.problemPath());
                    TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter.forChild(() -> {
                        return "(before)";
                    }), holderlookup_a);

                    tileentity.saveWithoutMetadata((ValueOutput) tagvalueoutput);
                    NBTTagCompound nbttagcompound = tagvalueoutput.buildResult();

                    tileentity.loadWithComponents(TagValueInput.create(problemreporter_j, holderlookup_a, this.tag));
                    TagValueOutput tagvalueoutput1 = TagValueOutput.createWithContext(problemreporter.forChild(() -> {
                        return "(after)";
                    }), holderlookup_a);

                    tileentity.saveWithoutMetadata((ValueOutput) tagvalueoutput1);
                    NBTTagCompound nbttagcompound1 = tagvalueoutput1.buildResult();

                    if (!nbttagcompound1.equals(nbttagcompound)) {
                        flag = true;
                        tileentity.setChanged();
                        worldserver.getChunkSource().blockChanged(blockposition);
                    }
                }
            }
        }

        return flag;
    }

    private IBlockData overwriteWithDefinedProperties(IBlockData iblockdata) {
        if (iblockdata == this.state) {
            return iblockdata;
        } else {
            for (IBlockState<?> iblockstate : this.properties) {
                iblockdata = copyProperty(iblockdata, this.state, iblockstate);
            }

            return iblockdata;
        }
    }

    private static <T extends Comparable<T>> IBlockData copyProperty(IBlockData iblockdata, IBlockData iblockdata1, IBlockState<T> iblockstate) {
        return (IBlockData) iblockdata.trySetValue(iblockstate, iblockdata1.getValue(iblockstate));
    }
}
