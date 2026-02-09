package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class DataConverterEquipment extends DataFix {

    public DataConverterEquipment(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        return this.cap(this.getInputSchema().getTypeRaw(DataConverterTypes.ITEM_STACK), this.getOutputSchema().getTypeRaw(DataConverterTypes.ITEM_STACK));
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule cap(Type<ItemStackOld> type, Type<ItemStackNew> type1) {
        Type<Pair<String, Either<List<ItemStackOld>, Unit>>> type2 = DSL.named(DataConverterTypes.ENTITY_EQUIPMENT.typeName(), DSL.optional(DSL.field("Equipment", DSL.list(type))));
        Type<Pair<String, Pair<Either<List<ItemStackNew>, Unit>, Pair<Either<List<ItemStackNew>, Unit>, Pair<Either<ItemStackNew, Unit>, Either<ItemStackNew, Unit>>>>>> type3 = DSL.named(DataConverterTypes.ENTITY_EQUIPMENT.typeName(), DSL.and(DSL.optional(DSL.field("ArmorItems", DSL.list(type1))), DSL.optional(DSL.field("HandItems", DSL.list(type1))), DSL.optional(DSL.field("body_armor_item", type1)), DSL.optional(DSL.field("saddle", type1))));

        if (!type2.equals(this.getInputSchema().getType(DataConverterTypes.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        } else if (!type3.equals(this.getOutputSchema().getType(DataConverterTypes.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        } else {
            return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix - drop chances", this.getInputSchema().getType(DataConverterTypes.ENTITY), (typed) -> {
                return typed.update(DSL.remainderFinder(), DataConverterEquipment::fixDropChances);
            }), this.fixTypeEverywhere("EntityEquipmentToArmorAndHandFix - equipment", type2, type3, (dynamicops) -> {
                ItemStackNew itemstacknew = (ItemStackNew) ((Pair) type1.read((new Dynamic(dynamicops)).emptyMap()).result().orElseThrow(() -> {
                    return new IllegalStateException("Could not parse newly created empty itemstack.");
                })).getFirst();
                Either<ItemStackNew, Unit> either = Either.right(DSL.unit());

                return (pair) -> {
                    return pair.mapSecond((either1) -> {
                        List<ItemStackOld> list = (List) either1.map(Function.identity(), (unit) -> {
                            return List.of();
                        });
                        Either<List<ItemStackNew>, Unit> either2 = Either.right(DSL.unit());
                        Either<List<ItemStackNew>, Unit> either3 = Either.right(DSL.unit());

                        if (!list.isEmpty()) {
                            either2 = Either.left(Lists.newArrayList(new Object[]{list.getFirst(), itemstacknew}));
                        }

                        if (list.size() > 1) {
                            List<ItemStackNew> list1 = Lists.newArrayList(new Object[]{itemstacknew, itemstacknew, itemstacknew, itemstacknew});

                            for (int i = 1; i < Math.min(list.size(), 5); ++i) {
                                list1.set(i - 1, list.get(i));
                            }

                            either3 = Either.left(list1);
                        }

                        return Pair.of(either3, Pair.of(either2, Pair.of(either, either)));
                    });
                };
            }));
        }
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> dynamic) {
        Optional<? extends Stream<? extends Dynamic<?>>> optional = dynamic.get("DropChances").asStreamOpt().result();

        dynamic = dynamic.remove("DropChances");
        if (optional.isPresent()) {
            Iterator<Float> iterator = Stream.concat(((Stream) optional.get()).map((dynamic1) -> {
                return dynamic1.asFloat(0.0F);
            }), Stream.generate(() -> {
                return 0.0F;
            })).iterator();
            float f = (Float) iterator.next();

            if (dynamic.get("HandDropChances").result().isEmpty()) {
                Stream stream = Stream.of(f, 0.0F);

                Objects.requireNonNull(dynamic);
                dynamic = dynamic.set("HandDropChances", dynamic.createList(stream.map(dynamic::createFloat)));
            }

            if (dynamic.get("ArmorDropChances").result().isEmpty()) {
                Stream stream1 = Stream.of((Float) iterator.next(), (Float) iterator.next(), (Float) iterator.next(), (Float) iterator.next());

                Objects.requireNonNull(dynamic);
                dynamic = dynamic.set("ArmorDropChances", dynamic.createList(stream1.map(dynamic::createFloat)));
            }
        }

        return dynamic;
    }
}
