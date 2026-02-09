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
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class BannerEntityCustomNameToOverrideComponentFix extends DataFix {

    public BannerEntityCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY);
        OpticFinder<?> opticfinder = type.findField("CustomName");
        OpticFinder<Pair<String, String>> opticfinder1 = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT));

        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, (typed) -> {
            Object object = ((Pair) typed.get(taggedchoice_taggedchoicetype.finder())).getFirst();

            return object.equals("minecraft:banner") ? this.fix(typed, opticfinder1, opticfinder) : typed;
        });
    }

    private Typed<?> fix(Typed<?> typed, OpticFinder<Pair<String, String>> opticfinder, OpticFinder<?> opticfinder1) {
        Optional<String> optional = typed.getOptionalTyped(opticfinder1).flatMap((typed1) -> {
            return typed1.getOptional(opticfinder).map(Pair::getSecond);
        });
        boolean flag = optional.flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter((s) -> {
            return s.equals("block.minecraft.ominous_banner");
        }).isPresent();

        return flag ? SystemUtils.writeAndReadTypedOrThrow(typed, typed.getType(), (dynamic) -> {
            Dynamic<?> dynamic1 = dynamic.createMap(Map.of(dynamic.createString("minecraft:item_name"), dynamic.createString((String) optional.get()), dynamic.createString("minecraft:hide_additional_tooltip"), dynamic.emptyMap()));

            return dynamic.set("components", dynamic1).remove("CustomName");
        }) : typed;
    }
}
