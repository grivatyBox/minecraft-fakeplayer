package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.stream.IntStream;

public class ChunkTicketUnpackPosFix extends DataFix {

    private static final long CHUNK_COORD_BITS = 32L;
    private static final long CHUNK_COORD_MASK = 4294967295L;

    public ChunkTicketUnpackPosFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ChunkTicketUnpackPosFix", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_TICKETS), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("data", (dynamic1) -> {
                    return dynamic1.update("tickets", (dynamic2) -> {
                        return dynamic2.createList(dynamic2.asStream().map((dynamic3) -> {
                            return dynamic3.update("chunk_pos", (dynamic4) -> {
                                long i = dynamic4.asLong(0L);
                                int j = (int) (i & 4294967295L);
                                int k = (int) (i >>> 32 & 4294967295L);

                                return dynamic4.createIntList(IntStream.of(new int[]{j, k}));
                            });
                        }));
                    });
                });
            });
        });
    }
}
