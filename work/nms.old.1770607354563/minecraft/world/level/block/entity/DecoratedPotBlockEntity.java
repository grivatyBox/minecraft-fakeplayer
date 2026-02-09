package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.ticks.ContainerSingleItem;

// CraftBukkit start
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class DecoratedPotBlockEntity extends TileEntity implements RandomizableContainer, ContainerSingleItem {

    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;

    @Override
    public List<ItemStack> getContents() {
        return Arrays.asList(this.item);
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public int getMaxStackSize() {
       return maxStack;
    }

    @Override
    public void setMaxStackSize(int i) {
        maxStack = i;
    }

    @Override
    public Location getLocation() {
        if (level == null) return null;
        return CraftLocation.toBukkit(worldPosition, level.getWorld());
    }
    // CraftBukkit end

    public static final String TAG_SHERDS = "sherds";
    public static final String TAG_ITEM = "item";
    public static final int EVENT_POT_WOBBLES = 1;
    public long wobbleStartedAtTick;
    @Nullable
    public DecoratedPotBlockEntity.b lastWobbleStyle;
    public DecoratedPotBlockEntity.Decoration decorations;
    private ItemStack item;
    @Nullable
    protected MinecraftKey lootTable;
    protected long lootTableSeed;

    public DecoratedPotBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.DECORATED_POT, blockposition, iblockdata);
        this.item = ItemStack.EMPTY;
        this.decorations = DecoratedPotBlockEntity.Decoration.EMPTY;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound) {
        super.saveAdditional(nbttagcompound);
        this.decorations.save(nbttagcompound);
        if (!this.trySaveLootTable(nbttagcompound) && !this.item.isEmpty()) {
            nbttagcompound.put("item", this.item.save(new NBTTagCompound()));
        }

    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        this.decorations = DecoratedPotBlockEntity.Decoration.load(nbttagcompound);
        if (!this.tryLoadLootTable(nbttagcompound)) {
            if (nbttagcompound.contains("item", 10)) {
                this.item = ItemStack.of(nbttagcompound.getCompound("item"));
            } else {
                this.item = ItemStack.EMPTY;
            }
        }

    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public EnumDirection getDirection() {
        return (EnumDirection) this.getBlockState().getValue(BlockProperties.HORIZONTAL_FACING);
    }

    public DecoratedPotBlockEntity.Decoration getDecorations() {
        return this.decorations;
    }

    public void setFromItem(ItemStack itemstack) {
        this.decorations = DecoratedPotBlockEntity.Decoration.load(ItemBlock.getBlockEntityData(itemstack));
    }

    public ItemStack getPotAsItem() {
        return createDecoratedPotItem(this.decorations);
    }

    public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decoration decoratedpotblockentity_decoration) {
        ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();
        NBTTagCompound nbttagcompound = decoratedpotblockentity_decoration.save(new NBTTagCompound());

        ItemBlock.setBlockEntityData(itemstack, TileEntityTypes.DECORATED_POT, nbttagcompound);
        return itemstack;
    }

    @Nullable
    @Override
    public MinecraftKey getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable MinecraftKey minecraftkey) {
        this.lootTable = minecraftkey;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long i) {
        this.lootTableSeed = i;
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable((EntityHuman) null);
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int i) {
        this.unpackLootTable((EntityHuman) null);
        ItemStack itemstack = this.item.split(i);

        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }

        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack itemstack) {
        this.unpackLootTable((EntityHuman) null);
        this.item = itemstack;
    }

    @Override
    public TileEntity getContainerBlockEntity() {
        return this;
    }

    public void wobble(DecoratedPotBlockEntity.b decoratedpotblockentity_b) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, decoratedpotblockentity_b.ordinal());
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (this.level != null && i == 1 && j >= 0 && j < DecoratedPotBlockEntity.b.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = DecoratedPotBlockEntity.b.values()[j];
            return true;
        } else {
            return super.triggerEvent(i, j);
        }
    }

    public static record Decoration(Item back, Item left, Item right, Item front) {

        public static final DecoratedPotBlockEntity.Decoration EMPTY = new DecoratedPotBlockEntity.Decoration(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);

        public NBTTagCompound save(NBTTagCompound nbttagcompound) {
            if (this.equals(DecoratedPotBlockEntity.Decoration.EMPTY)) {
                return nbttagcompound;
            } else {
                NBTTagList nbttaglist = new NBTTagList();

                this.sorted().forEach((item) -> {
                    nbttaglist.add(NBTTagString.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
                });
                nbttagcompound.put("sherds", nbttaglist);
                return nbttagcompound;
            }
        }

        public Stream<Item> sorted() {
            return Stream.of(this.back, this.left, this.right, this.front);
        }

        public static DecoratedPotBlockEntity.Decoration load(@Nullable NBTTagCompound nbttagcompound) {
            if (nbttagcompound != null && nbttagcompound.contains("sherds", 9)) {
                NBTTagList nbttaglist = nbttagcompound.getList("sherds", 8);

                return new DecoratedPotBlockEntity.Decoration(itemFromTag(nbttaglist, 0), itemFromTag(nbttaglist, 1), itemFromTag(nbttaglist, 2), itemFromTag(nbttaglist, 3));
            } else {
                return DecoratedPotBlockEntity.Decoration.EMPTY;
            }
        }

        private static Item itemFromTag(NBTTagList nbttaglist, int i) {
            if (i >= nbttaglist.size()) {
                return Items.BRICK;
            } else {
                NBTBase nbtbase = nbttaglist.get(i);

                return (Item) BuiltInRegistries.ITEM.get(MinecraftKey.tryParse(nbtbase.getAsString()));
            }
        }
    }

    public static enum b {

        POSITIVE(7), NEGATIVE(10);

        public final int duration;

        private b(int i) {
            this.duration = i;
        }
    }
}
