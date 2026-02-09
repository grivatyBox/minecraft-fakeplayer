package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;

public class EntityFieldsRenameFix extends DataConverterNamedEntity {

    private final Map<String, String> renames;

    public EntityFieldsRenameFix(Schema schema, String s, String s1, Map<String, String> map) {
        super(schema, false, s, DataConverterTypes.ENTITY, s1);
        this.renames = map;
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        for (Map.Entry<String, String> map_entry : this.renames.entrySet()) {
            dynamic = dynamic.renameField((String) map_entry.getKey(), (String) map_entry.getValue());
        }

        return dynamic;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}
