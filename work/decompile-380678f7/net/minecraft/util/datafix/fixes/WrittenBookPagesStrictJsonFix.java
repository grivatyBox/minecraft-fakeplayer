package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class WrittenBookPagesStrictJsonFix extends ItemStackTagFix {

    public WrittenBookPagesStrictJsonFix(Schema schema) {
        super(schema, "WrittenBookPagesStrictJsonFix", (s) -> {
            return s.equals("minecraft:written_book");
        });
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> typed) {
        Type<Pair<String, String>> type = this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT);
        Type<?> type1 = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<?> opticfinder = type1.findField("tag");
        OpticFinder<?> opticfinder1 = opticfinder.type().findField("pages");
        OpticFinder<Pair<String, String>> opticfinder2 = DSL.typeFinder(type);

        return typed.updateTyped(opticfinder1, (typed1) -> {
            return typed1.update(opticfinder2, (pair) -> {
                return pair.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient);
            });
        });
    }
}
