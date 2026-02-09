package net.minecraft.util.debug;

import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;

public interface DebugValueSource {

    void registerDebugValues(WorldServer worldserver, DebugValueSource.a debugvaluesource_a);

    public interface a {

        <T> void register(DebugSubscription<T> debugsubscription, DebugValueSource.b<T> debugvaluesource_b);
    }

    public interface b<T> {

        @Nullable
        T get();
    }
}
