package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class ForcedChunkToTicketFix extends DataFix {

    public ForcedChunkToTicketFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ForcedChunkToTicketFix", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_TICKETS), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("data", (dynamic1) -> {
                    return dynamic1.renameAndFixField("Forced", "tickets", (dynamic2) -> {
                        return dynamic2.createList(dynamic2.asLongStream().mapToObj((i) -> {
                            return dynamic.emptyMap().set("type", dynamic.createString("minecraft:forced")).set("level", dynamic.createInt(31)).set("ticks_left", dynamic.createLong(0L)).set("chunk_pos", dynamic.createLong(i));
                        }));
                    });
                });
            });
        });
    }
}
