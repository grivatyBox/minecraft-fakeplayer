package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class SignTextStrictJsonFix extends DataConverterNamedEntity {

    private static final List<String> LINE_FIELDS = List.of("Text1", "Text2", "Text3", "Text4");

    public SignTextStrictJsonFix(Schema schema) {
        super(schema, false, "SignTextStrictJsonFix", DataConverterTypes.BLOCK_ENTITY, "Sign");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        for (String s : SignTextStrictJsonFix.LINE_FIELDS) {
            OpticFinder<?> opticfinder = typed.getType().findField(s);
            OpticFinder<Pair<String, String>> opticfinder1 = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT));

            typed = typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.update(opticfinder1, (pair) -> {
                    return pair.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient);
                });
            });
        }

        return typed;
    }
}
