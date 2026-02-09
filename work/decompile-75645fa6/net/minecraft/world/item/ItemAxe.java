package net.minecraft.world.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockChest;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyChestType;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemAxe extends Item {

    protected static final Map<Block, Block> STRIPPABLES = (new Builder()).put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD).put(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD).put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD).put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG).put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK).build();

    public ItemAxe(ToolMaterial toolmaterial, float f, float f1, Item.Info item_info) {
        super(item_info.axe(toolmaterial, f, f1));
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        EntityHuman entityhuman = itemactioncontext.getPlayer();

        if (playerHasBlockingItemUseIntent(itemactioncontext)) {
            return EnumInteractionResult.PASS;
        } else {
            Optional<IBlockData> optional = this.evaluateNewBlockState(world, blockposition, entityhuman, world.getBlockState(blockposition));

            if (optional.isEmpty()) {
                return EnumInteractionResult.PASS;
            } else {
                ItemStack itemstack = itemactioncontext.getItemInHand();

                if (entityhuman instanceof EntityPlayer) {
                    CriterionTriggers.ITEM_USED_ON_BLOCK.trigger((EntityPlayer) entityhuman, blockposition, itemstack);
                }

                world.setBlock(blockposition, (IBlockData) optional.get(), 11);
                world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entityhuman, (IBlockData) optional.get()));
                if (entityhuman != null) {
                    itemstack.hurtAndBreak(1, entityhuman, itemactioncontext.getHand().asEquipmentSlot());
                }

                return EnumInteractionResult.SUCCESS;
            }
        }
    }

    private static boolean playerHasBlockingItemUseIntent(ItemActionContext itemactioncontext) {
        EntityHuman entityhuman = itemactioncontext.getPlayer();

        return itemactioncontext.getHand().equals(EnumHand.MAIN_HAND) && entityhuman.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !entityhuman.isSecondaryUseActive();
    }

    private Optional<IBlockData> evaluateNewBlockState(World world, BlockPosition blockposition, @Nullable EntityHuman entityhuman, IBlockData iblockdata) {
        Optional<IBlockData> optional = this.getStripped(iblockdata);

        if (optional.isPresent()) {
            world.playSound(entityhuman, blockposition, SoundEffects.AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return optional;
        } else {
            Optional<IBlockData> optional1 = WeatheringCopper.getPrevious(iblockdata);

            if (optional1.isPresent()) {
                spawnSoundAndParticle(world, blockposition, entityhuman, iblockdata, SoundEffects.AXE_SCRAPE, 3005);
                return optional1;
            } else {
                Optional<IBlockData> optional2 = Optional.ofNullable((Block) ((BiMap) HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(iblockdata.getBlock())).map((block) -> {
                    return block.withPropertiesOf(iblockdata);
                });

                if (optional2.isPresent()) {
                    spawnSoundAndParticle(world, blockposition, entityhuman, iblockdata, SoundEffects.AXE_WAX_OFF, 3004);
                    return optional2;
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private static void spawnSoundAndParticle(World world, BlockPosition blockposition, @Nullable EntityHuman entityhuman, IBlockData iblockdata, SoundEffect soundeffect, int i) {
        world.playSound(entityhuman, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.levelEvent(entityhuman, i, blockposition, 0);
        if (iblockdata.getBlock() instanceof BlockChest && iblockdata.getValue(BlockChest.TYPE) != BlockPropertyChestType.SINGLE) {
            BlockPosition blockposition1 = BlockChest.getConnectedBlockPos(blockposition, iblockdata);

            world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition1, GameEvent.a.of(entityhuman, world.getBlockState(blockposition1)));
            world.levelEvent(entityhuman, i, blockposition1, 0);
        }

    }

    private Optional<IBlockData> getStripped(IBlockData iblockdata) {
        return Optional.ofNullable((Block) ItemAxe.STRIPPABLES.get(iblockdata.getBlock())).map((block) -> {
            return (IBlockData) block.defaultBlockState().setValue(BlockRotatable.AXIS, (EnumDirection.EnumAxis) iblockdata.getValue(BlockRotatable.AXIS));
        });
    }
}
