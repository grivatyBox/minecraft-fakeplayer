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

public interface CaveVines {

    VoxelShape SHAPE = Block.column(14.0D, 0.0D, 16.0D);
    BlockStateBoolean BERRIES = BlockProperties.BERRIES;

    static EnumInteractionResult use(Entity entity, IBlockData iblockdata, World world, BlockPosition blockposition) {
        if ((Boolean) iblockdata.getValue(CaveVines.BERRIES)) {
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                Block.dropFromBlockInteractLootTable(worldserver, LootTables.HARVEST_CAVE_VINE, iblockdata, world.getBlockEntity(blockposition), (ItemStack) null, entity, (worldserver1, itemstack) -> {
                    Block.popResource(worldserver1, blockposition, itemstack);
                });
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
