package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.OptionalDynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class ItemStackCustomNameToOverrideComponentFix extends DataFix {

    private static final Set<String> MAP_NAMES = Set.of("filled_map.buried_treasure", "filled_map.explorer_jungle", "filled_map.explorer_swamp", "filled_map.mansion", "filled_map.monument", "filled_map.trial_chambers", "filled_map.village_desert", "filled_map.village_plains", "filled_map.village_savanna", "filled_map.village_snowy", "filled_map.village_taiga");

    public ItemStackCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");

        return this.fixTypeEverywhereTyped("ItemStack custom_name to item_name component fix", type, (typed) -> {
            Optional<Pair<String, String>> optional = typed.getOptional(opticfinder);
            Optional<String> optional1 = optional.map(Pair::getSecond);

            return optional1.filter((s) -> {
                return s.equals("minecraft:white_banner");
            }).isPresent() ? typed.updateTyped(opticfinder1, ItemStackCustomNameToOverrideComponentFix::fixBanner) : (optional1.filter((s) -> {
                return s.equals("minecraft:filled_map");
            }).isPresent() ? typed.updateTyped(opticfinder1, ItemStackCustomNameToOverrideComponentFix::fixMap) : typed);
        });
    }

    private static <T> Typed<T> fixMap(Typed<T> typed) {
        Set set = ItemStackCustomNameToOverrideComponentFix.MAP_NAMES;

        Objects.requireNonNull(set);
        return fixCustomName(typed, set::contains);
    }

    private static <T> Typed<T> fixBanner(Typed<T> typed) {
        return fixCustomName(typed, (s) -> {
            return s.equals("block.minecraft.ominous_banner");
        });
    }

    private static <T> Typed<T> fixCustomName(Typed<T> typed, Predicate<String> predicate) {
        return SystemUtils.writeAndReadTypedOrThrow(typed, typed.getType(), (dynamic) -> {
            OptionalDynamic<?> optionaldynamic = dynamic.get("minecraft:custom_name");
            Optional<String> optional = optionaldynamic.asString().result().flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter(predicate);

            return optional.isPresent() ? dynamic.renameField("minecraft:custom_name", "minecraft:item_name") : dynamic;
        });
    }
}
