package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;

public class TooltipDisplayComponentFix extends DataFix {

    private static final List<String> CONVERTED_ADDITIONAL_TOOLTIP_TYPES = List.of("minecraft:banner_patterns", "minecraft:bees", "minecraft:block_entity_data", "minecraft:block_state", "minecraft:bundle_contents", "minecraft:charged_projectiles", "minecraft:container", "minecraft:container_loot", "minecraft:firework_explosion", "minecraft:fireworks", "minecraft:instrument", "minecraft:map_id", "minecraft:painting/variant", "minecraft:pot_decorations", "minecraft:potion_contents", "minecraft:tropical_fish/pattern", "minecraft:written_book_content");

    public TooltipDisplayComponentFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.DATA_COMPONENTS);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.DATA_COMPONENTS);
        OpticFinder<?> opticfinder = type.findField("minecraft:can_place_on");
        OpticFinder<?> opticfinder1 = type.findField("minecraft:can_break");
        Type<?> type2 = type1.findFieldType("minecraft:can_place_on");
        Type<?> type3 = type1.findFieldType("minecraft:can_break");

        return this.fixTypeEverywhereTyped("TooltipDisplayComponentFix", type, type1, (typed) -> {
            return fix(typed, opticfinder, opticfinder1, type2, type3);
        });
    }

    private static Typed<?> fix(Typed<?> typed, OpticFinder<?> opticfinder, OpticFinder<?> opticfinder1, Type<?> type, Type<?> type1) {
        Set<String> set = new HashSet();

        typed = fixAdventureModePredicate(typed, opticfinder, type, "minecraft:can_place_on", set);
        typed = fixAdventureModePredicate(typed, opticfinder1, type1, "minecraft:can_break", set);
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            dynamic = fixSimpleComponent(dynamic, "minecraft:trim", set);
            dynamic = fixSimpleComponent(dynamic, "minecraft:unbreakable", set);
            dynamic = fixComponentAndUnwrap(dynamic, "minecraft:dyed_color", "rgb", set);
            dynamic = fixComponentAndUnwrap(dynamic, "minecraft:attribute_modifiers", "modifiers", set);
            dynamic = fixComponentAndUnwrap(dynamic, "minecraft:enchantments", "levels", set);
            dynamic = fixComponentAndUnwrap(dynamic, "minecraft:stored_enchantments", "levels", set);
            boolean flag = dynamic.get("minecraft:hide_tooltip").result().isPresent();

            dynamic = dynamic.remove("minecraft:hide_tooltip");
            boolean flag1 = dynamic.get("minecraft:hide_additional_tooltip").result().isPresent();

            dynamic = dynamic.remove("minecraft:hide_additional_tooltip");
            if (flag1) {
                for (String s : TooltipDisplayComponentFix.CONVERTED_ADDITIONAL_TOOLTIP_TYPES) {
                    if (dynamic.get(s).result().isPresent()) {
                        set.add(s);
                    }
                }
            }

            if (set.isEmpty() && !flag) {
                return dynamic;
            } else {
                Dynamic dynamic1 = dynamic.createString("hide_tooltip");
                Dynamic dynamic2 = dynamic.createBoolean(flag);
                Dynamic dynamic3 = dynamic.createString("hidden_components");
                Stream stream = set.stream();

                Objects.requireNonNull(dynamic);
                return dynamic.set("minecraft:tooltip_display", dynamic.createMap(Map.of(dynamic1, dynamic2, dynamic3, dynamic.createList(stream.map(dynamic::createString)))));
            }
        });
    }

    private static Dynamic<?> fixSimpleComponent(Dynamic<?> dynamic, String s, Set<String> set) {
        return fixRemainderComponent(dynamic, s, set, UnaryOperator.identity());
    }

    private static Dynamic<?> fixComponentAndUnwrap(Dynamic<?> dynamic, String s, String s1, Set<String> set) {
        return fixRemainderComponent(dynamic, s, set, (dynamic1) -> {
            return (Dynamic) DataFixUtils.orElse(dynamic1.get(s1).result(), dynamic1);
        });
    }

    private static Dynamic<?> fixRemainderComponent(Dynamic<?> dynamic, String s, Set<String> set, UnaryOperator<Dynamic<?>> unaryoperator) {
        return dynamic.update(s, (dynamic1) -> {
            boolean flag = dynamic1.get("show_in_tooltip").asBoolean(true);

            if (!flag) {
                set.add(s);
            }

            return (Dynamic) unaryoperator.apply(dynamic1.remove("show_in_tooltip"));
        });
    }

    private static Typed<?> fixAdventureModePredicate(Typed<?> typed, OpticFinder<?> opticfinder, Type<?> type, String s, Set<String> set) {
        return typed.updateTyped(opticfinder, type, (typed1) -> {
            return SystemUtils.writeAndReadTypedOrThrow(typed1, type, (dynamic) -> {
                OptionalDynamic<?> optionaldynamic = dynamic.get("predicates");

                if (optionaldynamic.result().isEmpty()) {
                    return dynamic;
                } else {
                    boolean flag = dynamic.get("show_in_tooltip").asBoolean(true);

                    if (!flag) {
                        set.add(s);
                    }

                    return (Dynamic) optionaldynamic.result().get();
                }
            });
        });
    }
}
