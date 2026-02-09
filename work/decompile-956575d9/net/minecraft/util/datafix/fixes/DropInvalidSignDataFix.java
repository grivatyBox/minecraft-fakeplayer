package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class DropInvalidSignDataFix extends DataFix {

    private final String entityName;

    public DropInvalidSignDataFix(Schema schema, String s) {
        super(schema, false);
        this.entityName = s;
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        dynamic = dynamic.update("front_text", DropInvalidSignDataFix::fixText);
        dynamic = dynamic.update("back_text", DropInvalidSignDataFix::fixText);

        for (String s : BlockEntitySignDoubleSidedEditableTextFix.FIELDS_TO_DROP) {
            dynamic = dynamic.remove(s);
        }

        return dynamic;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> dynamic) {
        Optional<Stream<Dynamic<T>>> optional = dynamic.get("filtered_messages").asStreamOpt().result();

        if (optional.isEmpty()) {
            return dynamic;
        } else {
            Dynamic<T> dynamic1 = LegacyComponentDataFixUtils.<T>createEmptyComponent(dynamic.getOps());
            List<Dynamic<T>> list = ((Stream) dynamic.get("messages").asStreamOpt().result().orElse(Stream.of())).toList();
            List<Dynamic<T>> list1 = Streams.mapWithIndex((Stream) optional.get(), (dynamic2, i) -> {
                Dynamic<T> dynamic3 = i < (long) list.size() ? (Dynamic) list.get((int) i) : dynamic1;

                return dynamic2.equals(dynamic1) ? dynamic3 : dynamic2;
            }).toList();

            return list1.equals(list) ? dynamic.remove("filtered_messages") : dynamic.set("filtered_messages", dynamic.createList(list1.stream()));
        }
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        Type<?> type1 = this.getInputSchema().getChoiceType(DataConverterTypes.BLOCK_ENTITY, this.entityName);
        OpticFinder<?> opticfinder = DSL.namedChoice(this.entityName, type1);

        return this.fixTypeEverywhereTyped("DropInvalidSignDataFix for " + this.entityName, type, (typed) -> {
            return typed.updateTyped(opticfinder, type1, (typed1) -> {
                boolean flag = ((Dynamic) typed1.get(DSL.remainderFinder())).get("_filtered_correct").asBoolean(false);

                return flag ? typed1.update(DSL.remainderFinder(), (dynamic) -> {
                    return dynamic.remove("_filtered_correct");
                }) : SystemUtils.writeAndReadTypedOrThrow(typed1, type1, this::fix);
            });
        });
    }
}
