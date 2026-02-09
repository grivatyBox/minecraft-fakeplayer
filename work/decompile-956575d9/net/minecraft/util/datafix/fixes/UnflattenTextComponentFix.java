package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.SystemUtils;
import org.slf4j.Logger;

public class UnflattenTextComponentFix extends DataFix {

    private static final Logger LOGGER = LogUtils.getLogger();

    public UnflattenTextComponentFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type = this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.TEXT_COMPONENT);

        return this.createFixer(type, type1);
    }

    private <T> TypeRewriteRule createFixer(Type<Pair<String, String>> type, Type<T> type1) {
        return this.fixTypeEverywhere("UnflattenTextComponentFix", type, type1, (dynamicops) -> {
            return (pair) -> {
                return SystemUtils.readTypedOrThrow(type1, unflattenJson(dynamicops, (String) pair.getSecond()), true).getValue();
            };
        });
    }

    private static <T> Dynamic<T> unflattenJson(DynamicOps<T> dynamicops, String s) {
        try {
            JsonElement jsonelement = JsonParser.parseString(s);

            if (!jsonelement.isJsonNull()) {
                return new Dynamic(dynamicops, JsonOps.INSTANCE.convertTo(dynamicops, jsonelement));
            }
        } catch (Exception exception) {
            UnflattenTextComponentFix.LOGGER.error("Failed to unflatten text component json: {}", s, exception);
        }

        return new Dynamic(dynamicops, dynamicops.createString(s));
    }
}
