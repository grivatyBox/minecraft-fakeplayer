package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;

public interface RandomizableContainer extends IInventory {

    String LOOT_TABLE_TAG = "LootTable";
    String LOOT_TABLE_SEED_TAG = "LootTableSeed";

    @Nullable
    MinecraftKey getLootTable();

    void setLootTable(@Nullable MinecraftKey minecraftkey);

    default void setLootTable(MinecraftKey minecraftkey, long i) {
        this.setLootTable(minecraftkey);
        this.setLootTableSeed(i);
    }

    long getLootTableSeed();

    void setLootTableSeed(long i);

    BlockPosition getBlockPos();

    @Nullable
    World getLevel();

    static void setBlockEntityLootTable(IBlockAccess iblockaccess, RandomSource randomsource, BlockPosition blockposition, MinecraftKey minecraftkey) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof RandomizableContainer) {
            RandomizableContainer randomizablecontainer = (RandomizableContainer) tileentity;

            randomizablecontainer.setLootTable(minecraftkey, randomsource.nextLong());
        }

    }

    default boolean tryLoadLootTable(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.contains("LootTable", 8)) {
            this.setLootTable(new MinecraftKey(nbttagcompound.getString("LootTable")));
            this.setLootTableSeed(nbttagcompound.getLong("LootTableSeed"));
            return true;
        } else {
            return false;
        }
    }

    default boolean trySaveLootTable(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = this.getLootTable();

        if (minecraftkey == null) {
            return false;
        } else {
            nbttagcompound.putString("LootTable", minecraftkey.toString());
            long i = this.getLootTableSeed();

            if (i != 0L) {
                nbttagcompound.putLong("LootTableSeed", i);
            }

            return true;
        }
    }

    default void unpackLootTable(@Nullable EntityHuman entityhuman) {
        World world = this.getLevel();
        BlockPosition blockposition = this.getBlockPos();
        MinecraftKey minecraftkey = this.getLootTable();

        if (minecraftkey != null && world != null && world.getServer() != null) {
            LootTable loottable = world.getServer().getLootData().getLootTable(minecraftkey);

            if (entityhuman instanceof EntityPlayer) {
                CriterionTriggers.GENERATE_LOOT.trigger((EntityPlayer) entityhuman, minecraftkey);
            }

            this.setLootTable((MinecraftKey) null);
            LootParams.a lootparams_a = (new LootParams.a((WorldServer) world)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition));

            if (entityhuman != null) {
                lootparams_a.withLuck(entityhuman.getLuck()).withParameter(LootContextParameters.THIS_ENTITY, entityhuman);
            }

            loottable.fill(this, lootparams_a.create(LootContextParameterSets.CHEST), this.getLootTableSeed());
        }

    }
}
