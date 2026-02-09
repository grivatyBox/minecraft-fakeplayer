package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockSkull;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntitySkull extends TileEntity {

    private static final String TAG_PROFILE = "profile";
    private static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    private static final String TAG_CUSTOM_NAME = "custom_name";
    @Nullable
    public ResolvableProfile owner;
    @Nullable
    public MinecraftKey noteBlockSound;
    private int animationTickCount;
    private boolean isAnimating;
    @Nullable
    private IChatBaseComponent customName;

    public TileEntitySkull(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.SKULL, blockposition, iblockdata);
    }

    @Override
    protected void saveAdditional(ValueOutput valueoutput) {
        super.saveAdditional(valueoutput);
        valueoutput.storeNullable("profile", ResolvableProfile.CODEC, this.owner);
        valueoutput.storeNullable("note_block_sound", MinecraftKey.CODEC, this.noteBlockSound);
        valueoutput.storeNullable("custom_name", ComponentSerialization.CODEC, this.customName);
    }

    @Override
    protected void loadAdditional(ValueInput valueinput) {
        super.loadAdditional(valueinput);
        this.owner = (ResolvableProfile) valueinput.read("profile", ResolvableProfile.CODEC).orElse((Object) null);
        this.noteBlockSound = (MinecraftKey) valueinput.read("note_block_sound", MinecraftKey.CODEC).orElse((Object) null);
        this.customName = parseCustomNameSafe(valueinput, "custom_name");
    }

    public static void animation(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntitySkull tileentityskull) {
        if (iblockdata.hasProperty(BlockSkull.POWERED) && (Boolean) iblockdata.getValue(BlockSkull.POWERED)) {
            tileentityskull.isAnimating = true;
            ++tileentityskull.animationTickCount;
        } else {
            tileentityskull.isAnimating = false;
        }

    }

    public float getAnimation(float f) {
        return this.isAnimating ? (float) this.animationTickCount + f : (float) this.animationTickCount;
    }

    @Nullable
    public ResolvableProfile getOwnerProfile() {
        return this.owner;
    }

    @Nullable
    public MinecraftKey getNoteBlockSound() {
        return this.noteBlockSound;
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        super.applyImplicitComponents(datacomponentgetter);
        this.owner = (ResolvableProfile) datacomponentgetter.get(DataComponents.PROFILE);
        this.noteBlockSound = (MinecraftKey) datacomponentgetter.get(DataComponents.NOTE_BLOCK_SOUND);
        this.customName = (IChatBaseComponent) datacomponentgetter.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.PROFILE, this.owner);
        datacomponentmap_a.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
        datacomponentmap_a.set(DataComponents.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueoutput) {
        super.removeComponentsFromTag(valueoutput);
        valueoutput.discard("profile");
        valueoutput.discard("note_block_sound");
        valueoutput.discard("custom_name");
    }
}
