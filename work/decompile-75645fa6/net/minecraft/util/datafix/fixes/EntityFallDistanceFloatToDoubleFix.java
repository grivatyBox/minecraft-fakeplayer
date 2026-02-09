package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityFallDistanceFloatToDoubleFix extends DataFix {

    private TypeReference type;

    public EntityFallDistanceFloatToDoubleFix(Schema schema, TypeReference typereference) {
        super(schema, false);
        this.type = typereference;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityFallDistanceFloatToDoubleFixFor" + this.type.typeName(), this.getOutputSchema().getType(this.type), EntityFallDistanceFloatToDoubleFix::fixEntity);
    }

    private static Typed<?> fixEntity(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.renameAndFixField("FallDistance", "fall_distance", (dynamic1) -> {
                return dynamic1.createDouble((double) dynamic1.asFloat(0.0F));
            });
        });
    }
}
