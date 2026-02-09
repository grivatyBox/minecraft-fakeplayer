package net.minecraft.world.level.saveddata;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;

public abstract class PersistentBase {

    private boolean dirty;

    public PersistentBase() {}

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean flag) {
        this.dirty = flag;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public static record a(@Nullable WorldServer level, long worldSeed) {

        public a(WorldServer worldserver) {
            this(worldserver, worldserver.getSeed());
        }

        public WorldServer levelOrThrow() {
            return (WorldServer) Objects.requireNonNull(this.level);
        }
    }
}
