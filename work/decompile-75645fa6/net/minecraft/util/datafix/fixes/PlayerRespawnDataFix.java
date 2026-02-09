package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class PlayerRespawnDataFix extends DataFix {

    public PlayerRespawnDataFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerRespawnDataFix", this.getInputSchema().getType(DataConverterTypes.PLAYER), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("respawn", (dynamic1) -> {
                    return dynamic1.set("dimension", dynamic1.createString(dynamic1.get("dimension").asString("minecraft:overworld"))).set("yaw", dynamic1.createFloat(dynamic1.get("angle").asFloat(0.0F))).set("pitch", dynamic1.createFloat(0.0F)).remove("angle");
                });
            });
        });
    }
}
