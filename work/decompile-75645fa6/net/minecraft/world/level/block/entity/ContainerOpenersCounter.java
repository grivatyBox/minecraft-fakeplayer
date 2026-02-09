package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AxisAlignedBB;

public abstract class ContainerOpenersCounter {

    private static final int CHECK_TICK_DELAY = 5;
    private int openCount;
    private double maxInteractionRange;

    public ContainerOpenersCounter() {}

    protected abstract void onOpen(World world, BlockPosition blockposition, IBlockData iblockdata);

    protected abstract void onClose(World world, BlockPosition blockposition, IBlockData iblockdata);

    protected abstract void openerCountChanged(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j);

    public abstract boolean isOwnContainer(EntityHuman entityhuman);

    public void incrementOpeners(EntityLiving entityliving, World world, BlockPosition blockposition, IBlockData iblockdata, double d0) {
        int i = this.openCount++;

        if (i == 0) {
            this.onOpen(world, blockposition, iblockdata);
            world.gameEvent(entityliving, (Holder) GameEvent.CONTAINER_OPEN, blockposition);
            scheduleRecheck(world, blockposition, iblockdata);
        }

        this.openerCountChanged(world, blockposition, iblockdata, i, this.openCount);
        this.maxInteractionRange = Math.max(d0, this.maxInteractionRange);
    }

    public void decrementOpeners(EntityLiving entityliving, World world, BlockPosition blockposition, IBlockData iblockdata) {
        int i = this.openCount--;

        if (this.openCount == 0) {
            this.onClose(world, blockposition, iblockdata);
            world.gameEvent(entityliving, (Holder) GameEvent.CONTAINER_CLOSE, blockposition);
            this.maxInteractionRange = 0.0D;
        }

        this.openerCountChanged(world, blockposition, iblockdata, i, this.openCount);
    }

    public List<ContainerUser> getEntitiesWithContainerOpen(World world, BlockPosition blockposition) {
        double d0 = this.maxInteractionRange + 4.0D;
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition)).inflate(d0);

        return (List) world.getEntities((Entity) null, axisalignedbb, (entity) -> {
            return this.hasContainerOpen(entity, blockposition);
        }).stream().map((entity) -> {
            return (ContainerUser) entity;
        }).collect(Collectors.toList());
    }

    private boolean hasContainerOpen(Entity entity, BlockPosition blockposition) {
        if (entity instanceof ContainerUser containeruser) {
            if (!containeruser.getLivingEntity().isSpectator()) {
                return containeruser.hasContainerOpen(this, blockposition);
            }
        }

        return false;
    }

    public void recheckOpeners(World world, BlockPosition blockposition, IBlockData iblockdata) {
        List<ContainerUser> list = this.getEntitiesWithContainerOpen(world, blockposition);

        this.maxInteractionRange = 0.0D;

        for (ContainerUser containeruser : list) {
            this.maxInteractionRange = Math.max(containeruser.getContainerInteractionRange(), this.maxInteractionRange);
        }

        int i = list.size();
        int j = this.openCount;

        if (j != i) {
            boolean flag = i != 0;
            boolean flag1 = j != 0;

            if (flag && !flag1) {
                this.onOpen(world, blockposition, iblockdata);
                world.gameEvent((Entity) null, (Holder) GameEvent.CONTAINER_OPEN, blockposition);
            } else if (!flag) {
                this.onClose(world, blockposition, iblockdata);
                world.gameEvent((Entity) null, (Holder) GameEvent.CONTAINER_CLOSE, blockposition);
            }

            this.openCount = i;
        }

        this.openerCountChanged(world, blockposition, iblockdata, j, i);
        if (i > 0) {
            scheduleRecheck(world, blockposition, iblockdata);
        }

    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.scheduleTick(blockposition, iblockdata.getBlock(), 5);
    }
}
