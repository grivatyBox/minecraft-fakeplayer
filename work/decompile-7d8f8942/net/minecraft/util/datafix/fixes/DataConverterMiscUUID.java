package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class DataConverterMiscUUID extends DataConverterUUIDBase {

    private static final Logger LOGGER = LogUtils.getLogger();

    public DataConverterMiscUUID(Schema schema) {
        super(schema, DataConverterTypes.LEVEL);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.typeReference);
        OpticFinder<?> opticfinder = type.findField("CustomBossEvents");
        OpticFinder<?> opticfinder1 = DSL.typeFinder(DSL.and(DSL.optional(DSL.field("Name", this.getInputSchema().getTypeRaw(DataConverterTypes.TEXT_COMPONENT))), DSL.remainderType()));

        return this.fixTypeEverywhereTyped("LevelUUIDFix", type, (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                dynamic = this.updateDragonFight(dynamic);
                dynamic = this.updateWanderingTrader(dynamic);
                return dynamic;
            }).updateTyped(opticfinder, (typed1) -> {
                return typed1.updateTyped(opticfinder1, (typed2) -> {
                    return typed2.update(DSL.remainderFinder(), this::updateCustomBossEvent);
                });
            });
        });
    }

    private Dynamic<?> updateWanderingTrader(Dynamic<?> dynamic) {
        return (Dynamic) replaceUUIDString(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
    }

    private Dynamic<?> updateDragonFight(Dynamic<?> dynamic) {
        return dynamic.update("DimensionData", (dynamic1) -> {
            return dynamic1.updateMapValues((pair) -> {
                return pair.mapSecond((dynamic2) -> {
                    return dynamic2.update("DragonFight", (dynamic3) -> {
                        return (Dynamic) replaceUUIDLeastMost(dynamic3, "DragonUUID", "Dragon").orElse(dynamic3);
                    });
                });
            });
        });
    }

    private Dynamic<?> updateCustomBossEvent(Dynamic<?> dynamic) {
        return dynamic.update("Players", (dynamic1) -> {
            return dynamic.createList(dynamic1.asStream().map((dynamic2) -> {
                return (Dynamic) createUUIDFromML(dynamic2).orElseGet(() -> {
                    DataConverterMiscUUID.LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
                    return dynamic2;
                });
            }));
        });
    }
}
