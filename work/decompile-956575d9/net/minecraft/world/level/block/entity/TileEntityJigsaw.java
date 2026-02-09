package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.BlockPropertyJigsawOrientation;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.WorldGenFeaturePieces;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.INamable;
import net.minecraft.world.level.block.BlockJigsaw;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructureJigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;

public class TileEntityJigsaw extends TileEntity {

    public static final Codec<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> POOL_CODEC = ResourceKey.codec(Registries.TEMPLATE_POOL);
    public static final MinecraftKey EMPTY_ID = MinecraftKey.withDefaultNamespace("empty");
    private static final int DEFAULT_PLACEMENT_PRIORITY = 0;
    private static final int DEFAULT_SELECTION_PRIORITY = 0;
    public static final String TARGET = "target";
    public static final String POOL = "pool";
    public static final String JOINT = "joint";
    public static final String PLACEMENT_PRIORITY = "placement_priority";
    public static final String SELECTION_PRIORITY = "selection_priority";
    public static final String NAME = "name";
    public static final String FINAL_STATE = "final_state";
    public static final String DEFAULT_FINAL_STATE = "minecraft:air";
    private MinecraftKey name;
    private MinecraftKey target;
    private ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> pool;
    private TileEntityJigsaw.JointType joint;
    private String finalState;
    private int placementPriority;
    private int selectionPriority;

    public TileEntityJigsaw(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.JIGSAW, blockposition, iblockdata);
        this.name = TileEntityJigsaw.EMPTY_ID;
        this.target = TileEntityJigsaw.EMPTY_ID;
        this.pool = WorldGenFeaturePieces.EMPTY;
        this.joint = TileEntityJigsaw.JointType.ROLLABLE;
        this.finalState = "minecraft:air";
        this.placementPriority = 0;
        this.selectionPriority = 0;
    }

    public MinecraftKey getName() {
        return this.name;
    }

    public MinecraftKey getTarget() {
        return this.target;
    }

    public ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public TileEntityJigsaw.JointType getJoint() {
        return this.joint;
    }

    public int getPlacementPriority() {
        return this.placementPriority;
    }

    public int getSelectionPriority() {
        return this.selectionPriority;
    }

    public void setName(MinecraftKey minecraftkey) {
        this.name = minecraftkey;
    }

    public void setTarget(MinecraftKey minecraftkey) {
        this.target = minecraftkey;
    }

    public void setPool(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey) {
        this.pool = resourcekey;
    }

    public void setFinalState(String s) {
        this.finalState = s;
    }

    public void setJoint(TileEntityJigsaw.JointType tileentityjigsaw_jointtype) {
        this.joint = tileentityjigsaw_jointtype;
    }

    public void setPlacementPriority(int i) {
        this.placementPriority = i;
    }

    public void setSelectionPriority(int i) {
        this.selectionPriority = i;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        nbttagcompound.store("name", MinecraftKey.CODEC, this.name);
        nbttagcompound.store("target", MinecraftKey.CODEC, this.target);
        nbttagcompound.store("pool", TileEntityJigsaw.POOL_CODEC, this.pool);
        nbttagcompound.putString("final_state", this.finalState);
        nbttagcompound.store("joint", TileEntityJigsaw.JointType.CODEC, this.joint);
        nbttagcompound.putInt("placement_priority", this.placementPriority);
        nbttagcompound.putInt("selection_priority", this.selectionPriority);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.name = (MinecraftKey) nbttagcompound.read("name", MinecraftKey.CODEC).orElse(TileEntityJigsaw.EMPTY_ID);
        this.target = (MinecraftKey) nbttagcompound.read("target", MinecraftKey.CODEC).orElse(TileEntityJigsaw.EMPTY_ID);
        this.pool = (ResourceKey) nbttagcompound.read("pool", TileEntityJigsaw.POOL_CODEC).orElse(WorldGenFeaturePieces.EMPTY);
        this.finalState = nbttagcompound.getStringOr("final_state", "minecraft:air");
        this.joint = (TileEntityJigsaw.JointType) nbttagcompound.read("joint", TileEntityJigsaw.JointType.CODEC).orElseGet(() -> {
            return DefinedStructure.getDefaultJointType(this.getBlockState());
        });
        this.placementPriority = nbttagcompound.getIntOr("placement_priority", 0);
        this.selectionPriority = nbttagcompound.getIntOr("selection_priority", 0);
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public void generate(WorldServer worldserver, int i, boolean flag) {
        BlockPosition blockposition = this.getBlockPos().relative(((BlockPropertyJigsawOrientation) this.getBlockState().getValue(BlockJigsaw.ORIENTATION)).front());
        IRegistry<WorldGenFeatureDefinedStructurePoolTemplate> iregistry = worldserver.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder = iregistry.getOrThrow(this.pool);

        WorldGenFeatureDefinedStructureJigsawPlacement.generateJigsaw(worldserver, holder, this.target, i, blockposition, flag);
    }

    public static enum JointType implements INamable {

        ROLLABLE("rollable"), ALIGNED("aligned");

        public static final INamable.a<TileEntityJigsaw.JointType> CODEC = INamable.<TileEntityJigsaw.JointType>fromEnum(TileEntityJigsaw.JointType::values);
        private final String name;

        private JointType(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public IChatBaseComponent getTranslatedName() {
            return IChatBaseComponent.translatable("jigsaw_block.joint." + this.name);
        }
    }
}
