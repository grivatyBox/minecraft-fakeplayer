package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class EquipmentFormatFix extends DataFix {

    public EquipmentFormatFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getTypeRaw(DataConverterTypes.ITEM_STACK);
        Type<?> type1 = this.getOutputSchema().getTypeRaw(DataConverterTypes.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("id");

        return this.fix(type, type1, opticfinder);
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule fix(Type<ItemStackOld> type, Type<ItemStackNew> type1, OpticFinder<?> opticfinder) {
        Type<Pair<String, Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<ItemStackOld, Unit>, Either<ItemStackOld, Unit>>>>>> type2 = DSL.named(DataConverterTypes.ENTITY_EQUIPMENT.typeName(), DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(type))), DSL.optional(DSL.field("HandItems", DSL.list(type))), DSL.optional(DSL.field("body_armor_item", type)), DSL.optional(DSL.field("saddle", type))));
        Type<Pair<String, Either<Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Dynamic<?>>>>>>>>>, Unit>>> type3 = DSL.named(DataConverterTypes.ENTITY_EQUIPMENT.typeName(), DSL.optional(DSL.field("equipment", DSL.and(DSL.optional(DSL.field("mainhand", type1)), DSL.optional(DSL.field("offhand", type1)), DSL.optional(DSL.field("feet", type1)), DSL.and(DSL.optional(DSL.field("legs", type1)), DSL.optional(DSL.field("chest", type1)), DSL.optional(DSL.field("head", type1)), DSL.and(DSL.optional(DSL.field("body", type1)), DSL.optional(DSL.field("saddle", type1)), DSL.remainderType()))))));

        if (!type2.equals(this.getInputSchema().getType(DataConverterTypes.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        } else if (!type3.equals(this.getOutputSchema().getType(DataConverterTypes.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        } else {
            return this.fixTypeEverywhere("EquipmentFormatFix", type2, type3, (dynamicops) -> {
                Predicate<ItemStackOld> predicate = (object) -> {
                    Typed<ItemStackOld> typed = new Typed(type, dynamicops, object);

                    return typed.getOptional(opticfinder).isEmpty();
                };

                return (pair) -> {
                    String s = (String) pair.getFirst();
                    Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<ItemStackOld, Unit>, Either<ItemStackOld, Unit>>>> pair1 = (Pair) pair.getSecond();
                    List<ItemStackOld> list = (List) ((Either) pair1.getFirst()).map(Function.identity(), (unit) -> {
                        return List.of();
                    });
                    List<ItemStackOld> list1 = (List) ((Either) ((Pair) pair1.getSecond()).getFirst()).map(Function.identity(), (unit) -> {
                        return List.of();
                    });
                    Either<ItemStackOld, Unit> either = (Either) ((Pair) ((Pair) pair1.getSecond()).getSecond()).getFirst();
                    Either<ItemStackOld, Unit> either1 = (Either) ((Pair) ((Pair) pair1.getSecond()).getSecond()).getSecond();
                    Either<ItemStackOld, Unit> either2 = getItemFromList(0, list, predicate);
                    Either<ItemStackOld, Unit> either3 = getItemFromList(1, list, predicate);
                    Either<ItemStackOld, Unit> either4 = getItemFromList(2, list, predicate);
                    Either<ItemStackOld, Unit> either5 = getItemFromList(3, list, predicate);
                    Either<ItemStackOld, Unit> either6 = getItemFromList(0, list1, predicate);
                    Either<ItemStackOld, Unit> either7 = getItemFromList(1, list1, predicate);

                    return areAllEmpty(either, either1, either2, either3, either4, either5, either6, either7) ? Pair.of(s, Either.right(Unit.INSTANCE)) : Pair.of(s, Either.left(Pair.of(either6, Pair.of(either7, Pair.of(either2, Pair.of(either3, Pair.of(either4, Pair.of(either5, Pair.of(either, Pair.of(either1, new Dynamic(dynamicops)))))))))));
                };
            });
        }
    }

    @SafeVarargs
    private static boolean areAllEmpty(Either<?, Unit>... aeither) {
        for (Either<?, Unit> either : aeither) {
            if (either.right().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static <ItemStack> Either<ItemStack, Unit> getItemFromList(int i, List<ItemStack> list, Predicate<ItemStack> predicate) {
        if (i >= list.size()) {
            return Either.right(Unit.INSTANCE);
        } else {
            ItemStack itemstack = (ItemStack) list.get(i);

            return predicate.test(itemstack) ? Either.right(Unit.INSTANCE) : Either.left(itemstack);
        }
    }
}
