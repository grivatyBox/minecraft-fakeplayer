package net.minecraft.util.datafix;

import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ChatDeserializer;

public class ComponentDataFixUtils {

    private static final String EMPTY_CONTENTS = createTextComponentJson("");

    public ComponentDataFixUtils() {}

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> dynamicops, String s) {
        String s1 = createTextComponentJson(s);

        return new Dynamic(dynamicops, dynamicops.createString(s1));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> dynamicops) {
        return new Dynamic(dynamicops, dynamicops.createString(ComponentDataFixUtils.EMPTY_CONTENTS));
    }

    private static String createTextComponentJson(String s) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("text", s);
        return ChatDeserializer.toStableString(jsonobject);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> dynamicops, String s) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("translate", s);
        return new Dynamic(dynamicops, dynamicops.createString(ChatDeserializer.toStableString(jsonobject)));
    }

    public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> dynamic) {
        return (Dynamic) DataFixUtils.orElse(dynamic.asString().map((s) -> {
            return createPlainTextComponent(dynamic.getOps(), s);
        }).result(), dynamic);
    }
}
