package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.shapes.VoxelShape;

// CraftBukkit start
import java.util.LinkedList;
import java.util.List;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
// CraftBukkit end

public interface CaveVines {

    VoxelShape SHAPE = Block.column(14.0D, 0.0D, 16.0D);
    BlockStateBoolean BERRIES = BlockProperties.BERRIES;

    static EnumInteractionResult use(Entity entity, IBlockData iblockdata, World world, BlockPosition blockposition) {
        if ((Boolean) iblockdata.getValue(CaveVines.BERRIES)) {
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                // CraftBukkit start
                if (!org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(entity, blockposition, (IBlockData) iblockdata.setValue(CaveVines.BERRIES, false))) {
                    return EnumInteractionResult.SUCCESS;
                }

                if (entity instanceof EntityHuman) {
                    List<ItemStack> dropped = new LinkedList<>();
                    Block.dropFromBlockInteractLootTable(worldserver, LootTables.HARVEST_CAVE_VINE, iblockdata, world.getBlockEntity(blockposition), (ItemStack) null, entity, (worldserver1, itemstack) -> {
                        dropped.add(itemstack);
                    });
                    PlayerHarvestBlockEvent event = CraftEventFactory.callPlayerHarvestBlockEvent(world, blockposition, (EntityHuman) entity, net.minecraft.world.EnumHand.MAIN_HAND, dropped);
                    if (event.isCancelled()) {
                        return EnumInteractionResult.SUCCESS; // We need to return a success either way, because making it PASS or FAIL will result in a bug where cancelling while harvesting w/ block in hand places block
                    }
                    for (org.bukkit.inventory.ItemStack itemStack : event.getItemsHarvested()) {
                        Block.popResource(world, blockposition, CraftItemStack.asNMSCopy(itemStack));
                    }
                } else {
                    Block.dropFromBlockInteractLootTable(worldserver, LootTables.HARVEST_CAVE_VINE, iblockdata, world.getBlockEntity(blockposition), (ItemStack) null, entity, (worldserver1, itemstack) -> {
                        Block.popResource(worldserver1, blockposition, itemstack);
                    });
                }
                // CraftBukkit end
                float f = MathHelper.randomBetween(worldserver.random, 0.8F, 1.2F);

                worldserver.playSound((Entity) null, blockposition, SoundEffects.CAVE_VINES_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, f);
                IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(CaveVines.BERRIES, false);

                worldserver.setBlock(blockposition, iblockdata1, 2);
                worldserver.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entity, iblockdata1));
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    static boolean hasGlowBerries(IBlockData iblockdata) {
        return iblockdata.hasProperty(CaveVines.BERRIES) && (Boolean) iblockdata.getValue(CaveVines.BERRIES);
    }

    static ToIntFunction<IBlockData> emission(int i) {
        return (iblockdata) -> {
            return (Boolean) iblockdata.getValue(BlockProperties.BERRIES) ? i : 0;
        };
    }
}
