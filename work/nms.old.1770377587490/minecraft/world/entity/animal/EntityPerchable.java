package net.minecraft.world.entity.animal;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTameableAnimal;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

// CraftBukkit start
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public abstract class EntityPerchable extends EntityTameableAnimal {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int RIDE_COOLDOWN = 100;
    private int rideCooldownCounter;

    protected EntityPerchable(EntityTypes<? extends EntityPerchable> entitytypes, World world) {
        super(entitytypes, world);
    }

    public boolean setEntityOnShoulder(EntityPlayer entityplayer) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(this.problemPath(), EntityPerchable.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, this.registryAccess());

            this.saveWithoutId(tagvalueoutput);
            tagvalueoutput.putString("id", this.getEncodeId());
            if (entityplayer.setEntityOnShoulder(tagvalueoutput.buildResult())) {
                this.discard(EntityRemoveEvent.Cause.PICKUP); // CraftBukkit - add Bukkit remove cause
                return true;
            }
        }

        return false;
    }

    @Override
    public void tick() {
        ++this.rideCooldownCounter;
        super.tick();
    }

    public boolean canSitOnShoulder() {
        return this.rideCooldownCounter > 100;
    }
}
