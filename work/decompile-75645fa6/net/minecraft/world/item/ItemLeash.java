package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.EntityLeash;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class ItemLeash extends Item {

    public ItemLeash(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        IBlockData iblockdata = world.getBlockState(blockposition);

        if (iblockdata.is(TagsBlock.FENCES)) {
            EntityHuman entityhuman = itemactioncontext.getPlayer();

            if (!world.isClientSide() && entityhuman != null) {
                return bindPlayerMobs(entityhuman, world, blockposition);
            }
        }

        return EnumInteractionResult.PASS;
    }

    public static EnumInteractionResult bindPlayerMobs(EntityHuman entityhuman, World world, BlockPosition blockposition) {
        EntityLeash entityleash = null;
        List<Leashable> list = Leashable.leashableInArea(world, Vec3D.atCenterOf(blockposition), (leashable) -> {
            return leashable.getLeashHolder() == entityhuman;
        });
        boolean flag = false;

        for (Leashable leashable : list) {
            if (entityleash == null) {
                entityleash = EntityLeash.getOrCreateKnot(world, blockposition);
                entityleash.playPlacementSound();
            }

            if (leashable.canHaveALeashAttachedTo(entityleash)) {
                leashable.setLeashedTo(entityleash, true);
                flag = true;
            }
        }

        if (flag) {
            world.gameEvent(GameEvent.BLOCK_ATTACH, blockposition, GameEvent.a.of((Entity) entityhuman));
            return EnumInteractionResult.SUCCESS_SERVER;
        } else {
            return EnumInteractionResult.PASS;
        }
    }
}
