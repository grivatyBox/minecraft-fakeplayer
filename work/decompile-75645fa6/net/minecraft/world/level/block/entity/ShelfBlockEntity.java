package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class ShelfBlockEntity extends TileEntity implements ItemOwner, ListBackedContainer {

    public static final int MAX_ITEMS = 3;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ALIGN_ITEMS_TO_BOTTOM_TAG = "align_items_to_bottom";
    private final NonNullList<ItemStack> items;
    private boolean alignItemsToBottom;

    public ShelfBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.SHELF, blockposition, iblockdata);
        this.items = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
    }

    @Override
    protected void loadAdditional(ValueInput valueinput) {
        super.loadAdditional(valueinput);
        this.items.clear();
        ContainerUtil.loadAllItems(valueinput, this.items);
        this.alignItemsToBottom = valueinput.getBooleanOr("align_items_to_bottom", false);
    }

    @Override
    protected void saveAdditional(ValueOutput valueoutput) {
        super.saveAdditional(valueoutput);
        ContainerUtil.saveAllItems(valueoutput, this.items, true);
        valueoutput.putBoolean("align_items_to_bottom", this.alignItemsToBottom);
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(this.problemPath(), ShelfBlockEntity.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, holderlookup_a);

            ContainerUtil.saveAllItems(tagvalueoutput, this.items, true);
            tagvalueoutput.putBoolean("align_items_to_bottom", this.alignItemsToBottom);
            return tagvalueoutput.buildResult();
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return IInventory.stillValidBlockEntity(this, entityhuman);
    }

    public ItemStack swapItemNoUpdate(int i, ItemStack itemstack) {
        ItemStack itemstack1 = this.removeItemNoUpdate(i);

        this.setItemNoUpdate(i, itemstack);
        return itemstack1;
    }

    public void setChanged(Holder.c<GameEvent> holder_c) {
        super.setChanged();
        if (this.level != null) {
            this.level.gameEvent(holder_c, this.worldPosition, GameEvent.a.of(this.getBlockState()));
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

    }

    @Override
    public void setChanged() {
        this.setChanged(GameEvent.BLOCK_ACTIVATE);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        super.applyImplicitComponents(datacomponentgetter);
        ((ItemContainerContents) datacomponentgetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyInto(this.items);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueoutput) {
        valueoutput.discard("Items");
    }

    @Override
    public World level() {
        return this.level;
    }

    @Override
    public Vec3D position() {
        return this.getBlockPos().getCenter();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return ((EnumDirection) this.getBlockState().getValue(ShelfBlock.FACING)).getOpposite().toYRot();
    }

    public boolean getAlignItemsToBottom() {
        return this.alignItemsToBottom;
    }
}
