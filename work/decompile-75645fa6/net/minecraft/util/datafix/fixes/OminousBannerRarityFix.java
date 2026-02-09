package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class OminousBannerRarityFix extends DataFix {

    public OminousBannerRarityFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        Type<?> type1 = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");
        OpticFinder<?> opticfinder2 = type1.findField("components");
        OpticFinder<?> opticfinder3 = opticfinder1.type().findField("minecraft:item_name");
        OpticFinder<Pair<String, String>> opticfinder4 = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT));

        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", type, (typed) -> {
            Object object = ((Pair) typed.get(taggedchoice_taggedchoicetype.finder())).getFirst();

            return object.equals("minecraft:banner") ? this.fix(typed, opticfinder1, opticfinder3, opticfinder4) : typed;
        }), this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", type1, (typed) -> {
            String s = (String) typed.getOptional(opticfinder).map(Pair::getSecond).orElse("");

            return s.equals("minecraft:white_banner") ? this.fix(typed, opticfinder2, opticfinder3, opticfinder4) : typed;
        }));
    }

    private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticfinder, OpticFinder<?> opticfinder1, OpticFinder<Pair<String, String>> opticfinder2) {
        return typed.updateTyped(opticfinder, (typed1) -> {
            boolean flag = typed1.getOptionalTyped(opticfinder1).flatMap((typed2) -> {
                return typed2.getOptional(opticfinder2);
            }).map(Pair::getSecond).flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter((s) -> {
                return s.equals("block.minecraft.ominous_banner");
            }).isPresent();

            return flag ? typed1.updateTyped(opticfinder1, (typed2) -> {
                return typed2.set(opticfinder2, Pair.of(DataConverterTypes.TEXT_COMPONENT.typeName(), LegacyComponentDataFixUtils.createTranslatableComponentJson("block.minecraft.ominous_banner")));
            }).update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.set("minecraft:rarity", dynamic.createString("uncommon"));
            }) : typed1;
        });
    }
}
