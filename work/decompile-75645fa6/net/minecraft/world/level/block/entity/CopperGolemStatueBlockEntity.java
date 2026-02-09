package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.coppergolem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.state.IBlockData;

public class CopperGolemStatueBlockEntity extends TileEntity {

    public CopperGolemStatueBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.COPPER_GOLEM_STATUE, blockposition, iblockdata);
    }

    public void createStatue(CopperGolem coppergolem) {
        this.setComponents(DataComponentMap.builder().addAll(this.components()).set(DataComponents.CUSTOM_NAME, coppergolem.getCustomName()).build());
        super.setChanged();
    }

    @Nullable
    public CopperGolem removeStatue(IBlockData iblockdata) {
        CopperGolem coppergolem = EntityTypes.COPPER_GOLEM.create(this.level, EntitySpawnReason.TRIGGERED);

        if (coppergolem != null) {
            coppergolem.setCustomName((IChatBaseComponent) this.components().get(DataComponents.CUSTOM_NAME));
            return this.initCopperGolem(iblockdata, coppergolem);
        } else {
            return null;
        }
    }

    private CopperGolem initCopperGolem(IBlockData iblockdata, CopperGolem coppergolem) {
        BlockPosition blockposition = this.getBlockPos();

        coppergolem.snapTo(blockposition.getCenter().x, (double) blockposition.getY(), blockposition.getCenter().z, ((EnumDirection) iblockdata.getValue(CopperGolemStatueBlock.FACING)).toYRot(), 0.0F);
        coppergolem.yHeadRot = coppergolem.getYRot();
        coppergolem.yBodyRot = coppergolem.getYRot();
        coppergolem.playSpawnSound();
        return coppergolem;
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    public ItemStack getItem(ItemStack itemstack, CopperGolemStatueBlock.a coppergolemstatueblock_a) {
        itemstack.applyComponents(this.collectComponents());
        itemstack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CopperGolemStatueBlock.POSE, coppergolemstatueblock_a));
        return itemstack;
    }
}
