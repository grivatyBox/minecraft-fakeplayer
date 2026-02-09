package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.function.Supplier;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class ThrownPotionSplitFix extends DataConverterEntityName {

    private final Supplier<ThrownPotionSplitFix.a> itemIdFinder = Suppliers.memoize(() -> {
        Type<?> type = this.getInputSchema().getChoiceType(DataConverterTypes.ENTITY, "minecraft:potion");
        Type<?> type1 = ExtraDataFixUtils.patchSubType(type, this.getInputSchema().getType(DataConverterTypes.ENTITY), this.getOutputSchema().getType(DataConverterTypes.ENTITY));
        OpticFinder<?> opticfinder = type1.findField("Item");
        OpticFinder<Pair<String, String>> opticfinder1 = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));

        return new ThrownPotionSplitFix.a(opticfinder, opticfinder1);
    });

    public ThrownPotionSplitFix(Schema schema) {
        super("ThrownPotionSplitFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String s, Typed<?> typed) {
        if (!s.equals("minecraft:potion")) {
            return Pair.of(s, typed);
        } else {
            String s1 = ((ThrownPotionSplitFix.a) this.itemIdFinder.get()).getItemId(typed);

            return "minecraft:lingering_potion".equals(s1) ? Pair.of("minecraft:lingering_potion", typed) : Pair.of("minecraft:splash_potion", typed);
        }
    }

    private static record a(OpticFinder<?> itemFinder, OpticFinder<Pair<String, String>> itemIdFinder) {

        public String getItemId(Typed<?> typed) {
            return (String) typed.getOptionalTyped(this.itemFinder).flatMap((typed1) -> {
                return typed1.getOptional(this.itemIdFinder);
            }).map(Pair::getSecond).map(DataConverterSchemaNamed::ensureNamespaced).orElse("");
        }
    }
}
