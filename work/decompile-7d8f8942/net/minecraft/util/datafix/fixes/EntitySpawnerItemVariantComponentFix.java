package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class EntitySpawnerItemVariantComponentFix extends DataFix {

    public EntitySpawnerItemVariantComponentFix(Schema schema) {
        super(schema, false);
    }

    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");

        return this.fixTypeEverywhereTyped("ItemStack bucket_entity_data variants to separate components", type, (typed) -> {
            Typed typed1;

            switch ((String) typed.getOptional(opticfinder).map(Pair::getSecond).orElse("")) {
                case "minecraft:salmon_bucket":
                    typed1 = typed.updateTyped(opticfinder1, EntitySpawnerItemVariantComponentFix::fixSalmonBucket);
                    break;
                case "minecraft:axolotl_bucket":
                    typed1 = typed.updateTyped(opticfinder1, EntitySpawnerItemVariantComponentFix::fixAxolotlBucket);
                    break;
                case "minecraft:tropical_fish_bucket":
                    typed1 = typed.updateTyped(opticfinder1, EntitySpawnerItemVariantComponentFix::fixTropicalFishBucket);
                    break;
                case "minecraft:painting":
                    typed1 = typed.updateTyped(opticfinder1, (typed2) -> {
                        return SystemUtils.writeAndReadTypedOrThrow(typed2, typed2.getType(), EntitySpawnerItemVariantComponentFix::fixPainting);
                    });
                    break;
                default:
                    typed1 = typed;
            }

            return typed1;
        });
    }

    private static String getBaseColor(int i) {
        return ExtraDataFixUtils.dyeColorIdToName(i >> 16 & 255);
    }

    private static String getPatternColor(int i) {
        return ExtraDataFixUtils.dyeColorIdToName(i >> 24 & 255);
    }

    private static String getPattern(int i) {
        String s;

        switch (i & 65535) {
            case 1:
                s = "flopper";
                break;
            case 256:
                s = "sunstreak";
                break;
            case 257:
                s = "stripey";
                break;
            case 512:
                s = "snooper";
                break;
            case 513:
                s = "glitter";
                break;
            case 768:
                s = "dasher";
                break;
            case 769:
                s = "blockfish";
                break;
            case 1024:
                s = "brinely";
                break;
            case 1025:
                s = "betty";
                break;
            case 1280:
                s = "spotty";
                break;
            case 1281:
                s = "clayfish";
                break;
            default:
                s = "kob";
        }

        return s;
    }

    private static <T> Dynamic<T> fixTropicalFishBucket(Dynamic<T> dynamic, Dynamic<T> dynamic1) {
        Optional<Number> optional = dynamic1.get("BucketVariantTag").asNumber().result();

        if (optional.isEmpty()) {
            return dynamic;
        } else {
            int i = ((Number) optional.get()).intValue();
            String s = getPattern(i);
            String s1 = getBaseColor(i);
            String s2 = getPatternColor(i);

            return dynamic.update("minecraft:bucket_entity_data", (dynamic2) -> {
                return dynamic2.remove("BucketVariantTag");
            }).set("minecraft:tropical_fish/pattern", dynamic.createString(s)).set("minecraft:tropical_fish/base_color", dynamic.createString(s1)).set("minecraft:tropical_fish/pattern_color", dynamic.createString(s2));
        }
    }

    private static <T> Dynamic<T> fixAxolotlBucket(Dynamic<T> dynamic, Dynamic<T> dynamic1) {
        Optional<Number> optional = dynamic1.get("Variant").asNumber().result();

        if (optional.isEmpty()) {
            return dynamic;
        } else {
            String s;

            switch (((Number) optional.get()).intValue()) {
                case 1:
                    s = "wild";
                    break;
                case 2:
                    s = "gold";
                    break;
                case 3:
                    s = "cyan";
                    break;
                case 4:
                    s = "blue";
                    break;
                default:
                    s = "lucy";
            }

            String s1 = s;

            return dynamic.update("minecraft:bucket_entity_data", (dynamic2) -> {
                return dynamic2.remove("Variant");
            }).set("minecraft:axolotl/variant", dynamic.createString(s1));
        }
    }

    private static <T> Dynamic<T> fixSalmonBucket(Dynamic<T> dynamic, Dynamic<T> dynamic1) {
        Optional<Dynamic<T>> optional = dynamic1.get("type").result();

        return optional.isEmpty() ? dynamic : dynamic.update("minecraft:bucket_entity_data", (dynamic2) -> {
            return dynamic2.remove("type");
        }).set("minecraft:salmon/size", (Dynamic) optional.get());
    }

    private static <T> Dynamic<T> fixPainting(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("minecraft:entity_data").result();

        if (optional.isEmpty()) {
            return dynamic;
        } else if (((Dynamic) optional.get()).get("id").asString().result().filter((s) -> {
            return s.equals("minecraft:painting");
        }).isEmpty()) {
            return dynamic;
        } else {
            Optional<Dynamic<T>> optional1 = ((Dynamic) optional.get()).get("variant").result();
            Dynamic<T> dynamic1 = ((Dynamic) optional.get()).remove("variant");

            if (dynamic1.remove("id").equals(dynamic1.emptyMap())) {
                dynamic = dynamic.remove("minecraft:entity_data");
            } else {
                dynamic = dynamic.set("minecraft:entity_data", dynamic1);
            }

            if (optional1.isPresent()) {
                dynamic = dynamic.set("minecraft:painting/variant", (Dynamic) optional1.get());
            }

            return dynamic;
        }
    }

    @FunctionalInterface
    private interface a extends Function<Typed<?>, Typed<?>> {

        default Typed<?> apply(Typed<?> typed) {
            return typed.update(DSL.remainderFinder(), this::fixRemainder);
        }

        default <T> Dynamic<T> fixRemainder(Dynamic<T> dynamic) {
            return (Dynamic) dynamic.get("minecraft:bucket_entity_data").result().map((dynamic1) -> {
                return this.fixRemainder(dynamic, dynamic1);
            }).orElse(dynamic);
        }

        <T> Dynamic<T> fixRemainder(Dynamic<T> dynamic, Dynamic<T> dynamic1);
    }
}
