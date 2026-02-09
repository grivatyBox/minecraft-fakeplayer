package net.minecraft.commands;

import net.minecraft.server.commands.PermissionCheck;

public interface PermissionSource {

    boolean hasPermission(int i);

    default boolean allowsSelectors() {
        return this.hasPermission(2);
    }

    public static record a<T extends PermissionSource>(int requiredLevel) implements PermissionCheck<T> {

        public boolean test(T t0) {
            return t0.hasPermission(this.requiredLevel);
        }
    }
}
