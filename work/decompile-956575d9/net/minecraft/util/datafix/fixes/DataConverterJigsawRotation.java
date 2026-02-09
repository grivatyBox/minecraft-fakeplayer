package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;

public class DataConverterJigsawRotation extends AbstractBlockPropertyFix {

    private static final Map<String, String> RENAMES = ImmutableMap.builder().put("down", "down_south").put("up", "up_north").put("north", "north_up").put("south", "south_up").put("west", "west_up").put("east", "east_up").build();

    public DataConverterJigsawRotation(Schema schema) {
        super(schema, "jigsaw_rotation_fix");
    }

    @Override
    protected boolean shouldFix(String s) {
        return s.equals("minecraft:jigsaw");
    }

    @Override
    protected <T> Dynamic<T> fixProperties(String s, Dynamic<T> dynamic) {
        String s1 = dynamic.get("facing").asString("north");

        return dynamic.remove("facing").set("orientation", dynamic.createString((String) DataConverterJigsawRotation.RENAMES.getOrDefault(s1, s1)));
    }
}
