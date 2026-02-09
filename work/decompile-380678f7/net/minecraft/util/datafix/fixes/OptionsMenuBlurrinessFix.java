package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsMenuBlurrinessFix extends DataFix {

    public OptionsMenuBlurrinessFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsMenuBlurrinessFix", this.getInputSchema().getType(DataConverterTypes.OPTIONS), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("menuBackgroundBlurriness", (dynamic1) -> {
                    int i = this.convertToIntRange(dynamic1.asString("0.5"));

                    return dynamic1.createString(String.valueOf(i));
                });
            });
        });
    }

    private int convertToIntRange(String s) {
        try {
            return Math.round(Float.parseFloat(s) * 10.0F);
        } catch (NumberFormatException numberformatexception) {
            return 5;
        }
    }
}
