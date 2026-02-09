package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class NamedEntityConvertUncheckedFix extends DataConverterNamedEntity {

    public NamedEntityConvertUncheckedFix(Schema schema, String s, TypeReference typereference, String s1) {
        super(schema, true, s, typereference, s1);
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        Type<?> type = this.getOutputSchema().getChoiceType(this.type, this.entityName);

        return ExtraDataFixUtils.cast(type, typed);
    }
}
