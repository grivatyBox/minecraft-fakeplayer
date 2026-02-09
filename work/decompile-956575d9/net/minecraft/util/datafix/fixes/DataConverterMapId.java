package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class DataConverterMapId extends DataFix {

    public DataConverterMapId(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("Map id fix", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_MAP_DATA), this.getOutputSchema().getType(DataConverterTypes.SAVED_DATA_MAP_DATA), (dynamic) -> {
            return dynamic.createMap(Map.of(dynamic.createString("data"), dynamic));
        });
    }
}
