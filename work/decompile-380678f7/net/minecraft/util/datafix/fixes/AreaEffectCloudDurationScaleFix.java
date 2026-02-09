package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class AreaEffectCloudDurationScaleFix extends DataConverterNamedEntity {

    public AreaEffectCloudDurationScaleFix(Schema schema) {
        super(schema, false, "AreaEffectCloudDurationScaleFix", DataConverterTypes.ENTITY, "minecraft:area_effect_cloud");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.set("potion_duration_scale", dynamic.createFloat(0.25F));
        });
    }
}
