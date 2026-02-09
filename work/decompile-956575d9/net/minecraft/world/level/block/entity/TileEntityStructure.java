package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ResourceKeyInvalidException;
import net.minecraft.SystemUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.UtilColor;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockStructure;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyStructureMode;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorRotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TileEntityStructure extends TileEntity implements BoundingBoxRenderable {

    private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
    public static final int MAX_OFFSET_PER_AXIS = 48;
    public static final int MAX_SIZE_PER_AXIS = 48;
    public static final String AUTHOR_TAG = "author";
    private static final String DEFAULT_AUTHOR = "";
    private static final String DEFAULT_METADATA = "";
    private static final BlockPosition DEFAULT_POS = new BlockPosition(0, 1, 0);
    private static final BaseBlockPosition DEFAULT_SIZE = BaseBlockPosition.ZERO;
    private static final EnumBlockRotation DEFAULT_ROTATION = EnumBlockRotation.NONE;
    private static final EnumBlockMirror DEFAULT_MIRROR = EnumBlockMirror.NONE;
    private static final boolean DEFAULT_IGNORE_ENTITIES = true;
    private static final boolean DEFAULT_STRICT = false;
    private static final boolean DEFAULT_POWERED = false;
    private static final boolean DEFAULT_SHOW_AIR = false;
    private static final boolean DEFAULT_SHOW_BOUNDING_BOX = true;
    private static final float DEFAULT_INTEGRITY = 1.0F;
    private static final long DEFAULT_SEED = 0L;
    @Nullable
    private MinecraftKey structureName;
    public String author = "";
    public String metaData = "";
    public BlockPosition structurePos;
    public BaseBlockPosition structureSize;
    public EnumBlockMirror mirror;
    public EnumBlockRotation rotation;
    public BlockPropertyStructureMode mode;
    public boolean ignoreEntities;
    private boolean strict;
    private boolean powered;
    public boolean showAir;
    public boolean showBoundingBox;
    public float integrity;
    public long seed;

    public TileEntityStructure(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.STRUCTURE_BLOCK, blockposition, iblockdata);
        this.structurePos = TileEntityStructure.DEFAULT_POS;
        this.structureSize = TileEntityStructure.DEFAULT_SIZE;
        this.mirror = EnumBlockMirror.NONE;
        this.rotation = EnumBlockRotation.NONE;
        this.ignoreEntities = true;
        this.strict = false;
        this.powered = false;
        this.showAir = false;
        this.showBoundingBox = true;
        this.integrity = 1.0F;
        this.seed = 0L;
        this.mode = (BlockPropertyStructureMode) iblockdata.getValue(BlockStructure.MODE);
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.putString("name", this.getStructureName());
        nbttagcompound.putString("author", this.author);
        nbttagcompound.putString("metadata", this.metaData);
        nbttagcompound.putInt("posX", this.structurePos.getX());
        nbttagcompound.putInt("posY", this.structurePos.getY());
        nbttagcompound.putInt("posZ", this.structurePos.getZ());
        nbttagcompound.putInt("sizeX", this.structureSize.getX());
        nbttagcompound.putInt("sizeY", this.structureSize.getY());
        nbttagcompound.putInt("sizeZ", this.structureSize.getZ());
        nbttagcompound.store("rotation", EnumBlockRotation.LEGACY_CODEC, this.rotation);
        nbttagcompound.store("mirror", EnumBlockMirror.LEGACY_CODEC, this.mirror);
        nbttagcompound.store("mode", BlockPropertyStructureMode.LEGACY_CODEC, this.mode);
        nbttagcompound.putBoolean("ignoreEntities", this.ignoreEntities);
        nbttagcompound.putBoolean("strict", this.strict);
        nbttagcompound.putBoolean("powered", this.powered);
        nbttagcompound.putBoolean("showair", this.showAir);
        nbttagcompound.putBoolean("showboundingbox", this.showBoundingBox);
        nbttagcompound.putFloat("integrity", this.integrity);
        nbttagcompound.putLong("seed", this.seed);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.setStructureName(nbttagcompound.getStringOr("name", ""));
        this.author = nbttagcompound.getStringOr("author", "");
        this.metaData = nbttagcompound.getStringOr("metadata", "");
        int i = MathHelper.clamp(nbttagcompound.getIntOr("posX", TileEntityStructure.DEFAULT_POS.getX()), -48, 48);
        int j = MathHelper.clamp(nbttagcompound.getIntOr("posY", TileEntityStructure.DEFAULT_POS.getY()), -48, 48);
        int k = MathHelper.clamp(nbttagcompound.getIntOr("posZ", TileEntityStructure.DEFAULT_POS.getZ()), -48, 48);

        this.structurePos = new BlockPosition(i, j, k);
        int l = MathHelper.clamp(nbttagcompound.getIntOr("sizeX", TileEntityStructure.DEFAULT_SIZE.getX()), 0, 48);
        int i1 = MathHelper.clamp(nbttagcompound.getIntOr("sizeY", TileEntityStructure.DEFAULT_SIZE.getY()), 0, 48);
        int j1 = MathHelper.clamp(nbttagcompound.getIntOr("sizeZ", TileEntityStructure.DEFAULT_SIZE.getZ()), 0, 48);

        this.structureSize = new BaseBlockPosition(l, i1, j1);
        this.rotation = (EnumBlockRotation) nbttagcompound.read("rotation", EnumBlockRotation.LEGACY_CODEC).orElse(TileEntityStructure.DEFAULT_ROTATION);
        this.mirror = (EnumBlockMirror) nbttagcompound.read("mirror", EnumBlockMirror.LEGACY_CODEC).orElse(TileEntityStructure.DEFAULT_MIRROR);
        this.mode = (BlockPropertyStructureMode) nbttagcompound.read("mode", BlockPropertyStructureMode.LEGACY_CODEC).orElse(BlockPropertyStructureMode.DATA);
        this.ignoreEntities = nbttagcompound.getBooleanOr("ignoreEntities", true);
        this.strict = nbttagcompound.getBooleanOr("strict", false);
        this.powered = nbttagcompound.getBooleanOr("powered", false);
        this.showAir = nbttagcompound.getBooleanOr("showair", false);
        this.showBoundingBox = nbttagcompound.getBooleanOr("showboundingbox", true);
        this.integrity = nbttagcompound.getFloatOr("integrity", 1.0F);
        this.seed = nbttagcompound.getLongOr("seed", 0L);
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (this.level != null) {
            BlockPosition blockposition = this.getBlockPos();
            IBlockData iblockdata = this.level.getBlockState(blockposition);

            if (iblockdata.is(Blocks.STRUCTURE_BLOCK)) {
                this.level.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockStructure.MODE, this.mode), 2);
            }

        }
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public boolean usedBy(EntityHuman entityhuman) {
        if (!entityhuman.canUseGameMasterBlocks()) {
            return false;
        } else {
            if (entityhuman.getCommandSenderWorld().isClientSide) {
                entityhuman.openStructureBlock(this);
            }

            return true;
        }
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String s) {
        this.setStructureName(UtilColor.isNullOrEmpty(s) ? null : MinecraftKey.tryParse(s));
    }

    public void setStructureName(@Nullable MinecraftKey minecraftkey) {
        this.structureName = minecraftkey;
    }

    public void createdBy(EntityLiving entityliving) {
        this.author = entityliving.getName().getString();
    }

    public BlockPosition getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPosition blockposition) {
        this.structurePos = blockposition;
    }

    public BaseBlockPosition getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(BaseBlockPosition baseblockposition) {
        this.structureSize = baseblockposition;
    }

    public EnumBlockMirror getMirror() {
        return this.mirror;
    }

    public void setMirror(EnumBlockMirror enumblockmirror) {
        this.mirror = enumblockmirror;
    }

    public EnumBlockRotation getRotation() {
        return this.rotation;
    }

    public void setRotation(EnumBlockRotation enumblockrotation) {
        this.rotation = enumblockrotation;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String s) {
        this.metaData = s;
    }

    public BlockPropertyStructureMode getMode() {
        return this.mode;
    }

    public void setMode(BlockPropertyStructureMode blockpropertystructuremode) {
        this.mode = blockpropertystructuremode;
        IBlockData iblockdata = this.level.getBlockState(this.getBlockPos());

        if (iblockdata.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(this.getBlockPos(), (IBlockData) iblockdata.setValue(BlockStructure.MODE, blockpropertystructuremode), 2);
        }

    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public void setIgnoreEntities(boolean flag) {
        this.ignoreEntities = flag;
    }

    public void setStrict(boolean flag) {
        this.strict = flag;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float f) {
        this.integrity = f;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long i) {
        this.seed = i;
    }

    public boolean detectSize() {
        if (this.mode != BlockPropertyStructureMode.SAVE) {
            return false;
        } else {
            BlockPosition blockposition = this.getBlockPos();
            int i = 80;
            BlockPosition blockposition1 = new BlockPosition(blockposition.getX() - 80, this.level.getMinY(), blockposition.getZ() - 80);
            BlockPosition blockposition2 = new BlockPosition(blockposition.getX() + 80, this.level.getMaxY(), blockposition.getZ() + 80);
            Stream<BlockPosition> stream = this.getRelatedCorners(blockposition1, blockposition2);

            return calculateEnclosingBoundingBox(blockposition, stream).filter((structureboundingbox) -> {
                int j = structureboundingbox.maxX() - structureboundingbox.minX();
                int k = structureboundingbox.maxY() - structureboundingbox.minY();
                int l = structureboundingbox.maxZ() - structureboundingbox.minZ();

                if (j > 1 && k > 1 && l > 1) {
                    this.structurePos = new BlockPosition(structureboundingbox.minX() - blockposition.getX() + 1, structureboundingbox.minY() - blockposition.getY() + 1, structureboundingbox.minZ() - blockposition.getZ() + 1);
                    this.structureSize = new BaseBlockPosition(j - 1, k - 1, l - 1);
                    this.setChanged();
                    IBlockData iblockdata = this.level.getBlockState(blockposition);

                    this.level.sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
                    return true;
                } else {
                    return false;
                }
            }).isPresent();
        }
    }

    private Stream<BlockPosition> getRelatedCorners(BlockPosition blockposition, BlockPosition blockposition1) {
        Stream stream = BlockPosition.betweenClosedStream(blockposition, blockposition1).filter((blockposition2) -> {
            return this.level.getBlockState(blockposition2).is(Blocks.STRUCTURE_BLOCK);
        });
        World world = this.level;

        Objects.requireNonNull(this.level);
        return stream.map(world::getBlockEntity).filter((tileentity) -> {
            return tileentity instanceof TileEntityStructure;
        }).map((tileentity) -> {
            return (TileEntityStructure) tileentity;
        }).filter((tileentitystructure) -> {
            return tileentitystructure.mode == BlockPropertyStructureMode.CORNER && Objects.equals(this.structureName, tileentitystructure.structureName);
        }).map(TileEntity::getBlockPos);
    }

    private static Optional<StructureBoundingBox> calculateEnclosingBoundingBox(BlockPosition blockposition, Stream<BlockPosition> stream) {
        Iterator<BlockPosition> iterator = stream.iterator();

        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BlockPosition blockposition1 = (BlockPosition) iterator.next();
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(blockposition1);

            if (iterator.hasNext()) {
                Objects.requireNonNull(structureboundingbox);
                iterator.forEachRemaining(structureboundingbox::encapsulate);
            } else {
                structureboundingbox.encapsulate(blockposition);
            }

            return Optional.of(structureboundingbox);
        }
    }

    public boolean saveStructure() {
        return this.mode != BlockPropertyStructureMode.SAVE ? false : this.saveStructure(true);
    }

    public boolean saveStructure(boolean flag) {
        if (this.structureName != null) {
            World world = this.level;

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                BlockPosition blockposition = this.getBlockPos().offset(this.structurePos);

                return saveStructure(worldserver, this.structureName, blockposition, this.structureSize, this.ignoreEntities, this.author, flag);
            }
        }

        return false;
    }

    public static boolean saveStructure(WorldServer worldserver, MinecraftKey minecraftkey, BlockPosition blockposition, BaseBlockPosition baseblockposition, boolean flag, String s, boolean flag1) {
        StructureTemplateManager structuretemplatemanager = worldserver.getStructureManager();

        DefinedStructure definedstructure;

        try {
            definedstructure = structuretemplatemanager.getOrCreate(minecraftkey);
        } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
            return false;
        }

        definedstructure.fillFromWorld(worldserver, blockposition, baseblockposition, !flag, Blocks.STRUCTURE_VOID);
        definedstructure.setAuthor(s);
        if (flag1) {
            try {
                return structuretemplatemanager.save(minecraftkey);
            } catch (ResourceKeyInvalidException resourcekeyinvalidexception1) {
                return false;
            }
        } else {
            return true;
        }
    }

    public static RandomSource createRandom(long i) {
        return i == 0L ? RandomSource.create(SystemUtils.getMillis()) : RandomSource.create(i);
    }

    public boolean placeStructureIfSameSize(WorldServer worldserver) {
        if (this.mode == BlockPropertyStructureMode.LOAD && this.structureName != null) {
            DefinedStructure definedstructure = (DefinedStructure) worldserver.getStructureManager().get(this.structureName).orElse((Object) null);

            if (definedstructure == null) {
                return false;
            } else if (definedstructure.getSize().equals(this.structureSize)) {
                this.placeStructure(worldserver, definedstructure);
                return true;
            } else {
                this.loadStructureInfo(definedstructure);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean loadStructureInfo(WorldServer worldserver) {
        DefinedStructure definedstructure = this.getStructureTemplate(worldserver);

        if (definedstructure == null) {
            return false;
        } else {
            this.loadStructureInfo(definedstructure);
            return true;
        }
    }

    private void loadStructureInfo(DefinedStructure definedstructure) {
        this.author = !UtilColor.isNullOrEmpty(definedstructure.getAuthor()) ? definedstructure.getAuthor() : "";
        this.structureSize = definedstructure.getSize();
        this.setChanged();
    }

    public void placeStructure(WorldServer worldserver) {
        DefinedStructure definedstructure = this.getStructureTemplate(worldserver);

        if (definedstructure != null) {
            this.placeStructure(worldserver, definedstructure);
        }

    }

    @Nullable
    private DefinedStructure getStructureTemplate(WorldServer worldserver) {
        return this.structureName == null ? null : (DefinedStructure) worldserver.getStructureManager().get(this.structureName).orElse((Object) null);
    }

    private void placeStructure(WorldServer worldserver, DefinedStructure definedstructure) {
        this.loadStructureInfo(definedstructure);
        DefinedStructureInfo definedstructureinfo = (new DefinedStructureInfo()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setKnownShape(this.strict);

        if (this.integrity < 1.0F) {
            definedstructureinfo.clearProcessors().addProcessor(new DefinedStructureProcessorRotation(MathHelper.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
        }

        BlockPosition blockposition = this.getBlockPos().offset(this.structurePos);

        definedstructure.placeInWorld(worldserver, blockposition, blockposition, definedstructureinfo, createRandom(this.seed), 2 | (this.strict ? 816 : 0));
    }

    public void unloadStructure() {
        if (this.structureName != null) {
            WorldServer worldserver = (WorldServer) this.level;
            StructureTemplateManager structuretemplatemanager = worldserver.getStructureManager();

            structuretemplatemanager.remove(this.structureName);
        }
    }

    public boolean isStructureLoadable() {
        if (this.mode == BlockPropertyStructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
            WorldServer worldserver = (WorldServer) this.level;
            StructureTemplateManager structuretemplatemanager = worldserver.getStructureManager();

            try {
                return structuretemplatemanager.get(this.structureName).isPresent();
            } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean flag) {
        this.powered = flag;
    }

    public boolean getShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean flag) {
        this.showAir = flag;
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean flag) {
        this.showBoundingBox = flag;
    }

    @Override
    public BoundingBoxRenderable.a renderMode() {
        return this.mode != BlockPropertyStructureMode.SAVE && this.mode != BlockPropertyStructureMode.LOAD ? BoundingBoxRenderable.a.NONE : (this.mode == BlockPropertyStructureMode.SAVE && this.showAir ? BoundingBoxRenderable.a.BOX_AND_INVISIBLE_BLOCKS : (this.mode != BlockPropertyStructureMode.SAVE && !this.showBoundingBox ? BoundingBoxRenderable.a.NONE : BoundingBoxRenderable.a.BOX));
    }

    @Override
    public BoundingBoxRenderable.b getRenderableBox() {
        BlockPosition blockposition = this.getStructurePos();
        BaseBlockPosition baseblockposition = this.getStructureSize();
        int i = blockposition.getX();
        int j = blockposition.getZ();
        int k = blockposition.getY();
        int l = k + baseblockposition.getY();
        int i1;
        int j1;

        switch (this.mirror) {
            case LEFT_RIGHT:
                i1 = baseblockposition.getX();
                j1 = -baseblockposition.getZ();
                break;
            case FRONT_BACK:
                i1 = -baseblockposition.getX();
                j1 = baseblockposition.getZ();
                break;
            default:
                i1 = baseblockposition.getX();
                j1 = baseblockposition.getZ();
        }

        int k1;
        int l1;
        int i2;
        int j2;

        switch (this.rotation) {
            case CLOCKWISE_90:
                k1 = j1 < 0 ? i : i + 1;
                l1 = i1 < 0 ? j + 1 : j;
                i2 = k1 - j1;
                j2 = l1 + i1;
                break;
            case CLOCKWISE_180:
                k1 = i1 < 0 ? i : i + 1;
                l1 = j1 < 0 ? j : j + 1;
                i2 = k1 - i1;
                j2 = l1 - j1;
                break;
            case COUNTERCLOCKWISE_90:
                k1 = j1 < 0 ? i + 1 : i;
                l1 = i1 < 0 ? j : j + 1;
                i2 = k1 + j1;
                j2 = l1 - i1;
                break;
            default:
                k1 = i1 < 0 ? i + 1 : i;
                l1 = j1 < 0 ? j + 1 : j;
                i2 = k1 + i1;
                j2 = l1 + j1;
        }

        return BoundingBoxRenderable.b.fromCorners(k1, k, l1, i2, l, j2);
    }

    public static enum UpdateType {

        UPDATE_DATA, SAVE_AREA, LOAD_AREA, SCAN_AREA;

        private UpdateType() {}
    }
}
