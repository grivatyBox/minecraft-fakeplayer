package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class TextComponentStrictJsonFix extends DataFix {

    public TextComponentStrictJsonFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type = this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT);

        return this.fixTypeEverywhere("TextComponentStrictJsonFix", type, (dynamicops) -> {
            return (pair) -> {
                return pair.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient);
            };
        });
    }
}
