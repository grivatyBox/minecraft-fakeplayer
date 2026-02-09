package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class CopperGolemWeatherStateFix extends DataConverterNamedEntity {

    public CopperGolemWeatherStateFix(Schema schema) {
        super(schema, false, "CopperGolemWeatherStateFix", DataConverterTypes.ENTITY, "minecraft:copper_golem");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.update("weather_state", CopperGolemWeatherStateFix::fixWeatherState);
        });
    }

    private static Dynamic<?> fixWeatherState(Dynamic<?> dynamic) {
        Dynamic dynamic1;

        switch (dynamic.asInt(0)) {
            case 1:
                dynamic1 = dynamic.createString("exposed");
                break;
            case 2:
                dynamic1 = dynamic.createString("weathered");
                break;
            case 3:
                dynamic1 = dynamic.createString("oxidized");
                break;
            default:
                dynamic1 = dynamic.createString("unaffected");
        }

        return dynamic1;
    }
}
