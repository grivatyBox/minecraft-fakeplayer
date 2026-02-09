package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class ScoreboardDisplayNameFix extends DataFix {

    private final String name;
    private final TypeReference type;

    public ScoreboardDisplayNameFix(Schema schema, String s, TypeReference typereference) {
        super(schema, false);
        this.name = s;
        this.type = typereference;
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.type);
        OpticFinder<?> opticfinder = type.findField("DisplayName");
        OpticFinder<Pair<String, String>> opticfinder1 = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT));

        return this.fixTypeEverywhereTyped(this.name, type, (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.update(opticfinder1, (pair) -> {
                    return pair.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson);
                });
            });
        });
    }
}
