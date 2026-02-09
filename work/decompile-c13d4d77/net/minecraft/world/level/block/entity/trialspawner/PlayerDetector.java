package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AxisAlignedBB;

public interface PlayerDetector {

    PlayerDetector PLAYERS = (worldserver, blockposition, i) -> {
        return worldserver.getPlayers((entityplayer) -> {
            return entityplayer.blockPosition().closerThan(blockposition, (double) i) && !entityplayer.isCreative() && !entityplayer.isSpectator();
        }).stream().map(Entity::getUUID).toList();
    };
    PlayerDetector SHEEP = (worldserver, blockposition, i) -> {
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition)).inflate((double) i);

        return worldserver.getEntities((EntityTypeTest) EntityTypes.SHEEP, axisalignedbb, EntityLiving::isAlive).stream().map(Entity::getUUID).toList();
    };

    List<UUID> detect(WorldServer worldserver, BlockPosition blockposition, int i);
}
