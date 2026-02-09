package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockPumpkin extends Block {

    public static final MapCodec<BlockPumpkin> CODEC = simpleCodec(BlockPumpkin::new);

    @Override
    public MapCodec<BlockPumpkin> codec() {
        return BlockPumpkin.CODEC;
    }

    protected BlockPumpkin(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!itemstack.is(Items.SHEARS)) {
            return super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
        } else if (world instanceof WorldServer) {
            WorldServer worldserver = (WorldServer) world;
            EnumDirection enumdirection = movingobjectpositionblock.getDirection();
            EnumDirection enumdirection1 = enumdirection.getAxis() == EnumDirection.EnumAxis.Y ? entityhuman.getDirection().getOpposite() : enumdirection;

            dropFromBlockInteractLootTable(worldserver, LootTables.CARVE_PUMPKIN, iblockdata, world.getBlockEntity(blockposition), itemstack, entityhuman, (worldserver1, itemstack1) -> {
                EntityItem entityitem = new EntityItem(world, (double) blockposition.getX() + 0.5D + (double) enumdirection1.getStepX() * 0.65D, (double) blockposition.getY() + 0.1D, (double) blockposition.getZ() + 0.5D + (double) enumdirection1.getStepZ() * 0.65D, itemstack1);

                entityitem.setDeltaMovement(0.05D * (double) enumdirection1.getStepX() + world.random.nextDouble() * 0.02D, 0.05D, 0.05D * (double) enumdirection1.getStepZ() + world.random.nextDouble() * 0.02D);
                world.addFreshEntity(entityitem);
            });
            world.playSound((Entity) null, blockposition, SoundEffects.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlock(blockposition, (IBlockData) Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(BlockPumpkinCarved.FACING, enumdirection1), 11);
            itemstack.hurtAndBreak(1, entityhuman, enumhand.asEquipmentSlot());
            world.gameEvent(entityhuman, (Holder) GameEvent.SHEAR, blockposition);
            entityhuman.awardStat(StatisticList.ITEM_USED.get(Items.SHEARS));
            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.SUCCESS;
        }
    }
}
