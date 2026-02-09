package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.util.ChatDeserializer;

public class LegacyHoverEventFix extends DataFix {

    public LegacyHoverEventFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<? extends Pair<String, ?>> type = this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT).findFieldType("hoverEvent");

        return this.createFixer(this.getInputSchema().getTypeRaw(DataConverterTypes.TEXT_COMPONENT), type);
    }

    private <C, H extends Pair<String, ?>> TypeRewriteRule createFixer(Type<C> type, Type<H> type1) {
        Type<Pair<String, Either<Either<String, List<C>>, Pair<Either<List<C>, Unit>, Pair<Either<C, Unit>, Pair<Either<H, Unit>, Dynamic<?>>>>>>> type2 = DSL.named(DataConverterTypes.TEXT_COMPONENT.typeName(), DSL.or(DSL.or(DSL.string(), DSL.list(type)), DSL.and(DSL.optional(DSL.field("extra", DSL.list(type))), DSL.optional(DSL.field("separator", type)), DSL.optional(DSL.field("hoverEvent", type1)), DSL.remainderType())));

        if (!type2.equals(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT))) {
            String s = String.valueOf(type2);

            throw new IllegalStateException("Text component type did not match, expected " + s + " but got " + String.valueOf(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT)));
        } else {
            return this.fixTypeEverywhere("LegacyHoverEventFix", type2, (dynamicops) -> {
                return (pair) -> {
                    return pair.mapSecond((either) -> {
                        return either.mapRight((pair1) -> {
                            return pair1.mapSecond((pair2) -> {
                                return pair2.mapSecond((pair3) -> {
                                    Dynamic<?> dynamic = (Dynamic) pair3.getSecond();
                                    Optional<? extends Dynamic<?>> optional = dynamic.get("hoverEvent").result();

                                    if (optional.isEmpty()) {
                                        return pair3;
                                    } else {
                                        Optional<? extends Dynamic<?>> optional1 = ((Dynamic) optional.get()).get("value").result();

                                        if (optional1.isEmpty()) {
                                            return pair3;
                                        } else {
                                            String s1 = (String) ((Either) pair3.getFirst()).left().map(Pair::getFirst).orElse("");
                                            H h0 = (H) ((Pair) this.fixHoverEvent(type1, s1, (Dynamic) optional.get()));

                                            return pair3.mapFirst((either1) -> {
                                                return Either.left(h0);
                                            });
                                        }
                                    }
                                });
                            });
                        });
                    });
                };
            });
        }
    }

    private <H> H fixHoverEvent(Type<H> type, String s, Dynamic<?> dynamic) {
        return (H) ("show_text".equals(s) ? fixShowTextHover(type, dynamic) : createPlaceholderHover(type, dynamic));
    }

    private static <H> H fixShowTextHover(Type<H> type, Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = dynamic.renameField("value", "contents");

        return (H) SystemUtils.readTypedOrThrow(type, dynamic1).getValue();
    }

    private static <H> H createPlaceholderHover(Type<H> type, Dynamic<?> dynamic) {
        JsonElement jsonelement = (JsonElement) dynamic.convert(JsonOps.INSTANCE).getValue();
        Dynamic<?> dynamic1 = new Dynamic(JavaOps.INSTANCE, Map.of("action", "show_text", "contents", Map.of("text", "Legacy hoverEvent: " + ChatDeserializer.toStableString(jsonelement))));

        return (H) SystemUtils.readTypedOrThrow(type, dynamic1).getValue();
    }
}
