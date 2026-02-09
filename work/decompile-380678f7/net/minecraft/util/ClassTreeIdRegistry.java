package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.SystemUtils;

public class ClassTreeIdRegistry {

    public static final int NO_ID_VALUE = -1;
    private final Object2IntMap<Class<?>> classToLastIdCache = (Object2IntMap) SystemUtils.make(new Object2IntOpenHashMap(), (object2intopenhashmap) -> {
        object2intopenhashmap.defaultReturnValue(-1);
    });

    public ClassTreeIdRegistry() {}

    public int getLastIdFor(Class<?> oclass) {
        int i = this.classToLastIdCache.getInt(oclass);

        if (i != -1) {
            return i;
        } else {
            Class<?> oclass1 = oclass;

            while ((oclass1 = oclass1.getSuperclass()) != Object.class) {
                int j = this.classToLastIdCache.getInt(oclass1);

                if (j != -1) {
                    return j;
                }
            }

            return -1;
        }
    }

    public int getCount(Class<?> oclass) {
        return this.getLastIdFor(oclass) + 1;
    }

    public int define(Class<?> oclass) {
        int i = this.getLastIdFor(oclass);
        int j = i == -1 ? 0 : i + 1;

        this.classToLastIdCache.put(oclass, j);
        return j;
    }
}
