package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class DataConverterItemLoreComponentize extends DataFix {

    public DataConverterItemLoreComponentize(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        Type<Pair<String, String>> type1 = this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT);
        OpticFinder<?> opticfinder = type.findField("tag");
        OpticFinder<?> opticfinder1 = opticfinder.type().findField("display");
        OpticFinder<?> opticfinder2 = opticfinder1.type().findField("Lore");
        OpticFinder<Pair<String, String>> opticfinder3 = DSL.typeFinder(type1);

        return this.fixTypeEverywhereTyped("Item Lore componentize", type, (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.updateTyped(opticfinder1, (typed2) -> {
                    return typed2.updateTyped(opticfinder2, (typed3) -> {
                        return typed3.update(opticfinder3, (pair) -> {
                            return pair.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson);
                        });
                    });
                });
            });
        });
    }
}
