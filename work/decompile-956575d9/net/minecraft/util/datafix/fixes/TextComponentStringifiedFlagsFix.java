package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class TextComponentStringifiedFlagsFix extends DataFix {

    public TextComponentStringifiedFlagsFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Either<?, Pair<?, Pair<?, Pair<?, Dynamic<?>>>>>>> type = this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT);

        return this.fixTypeEverywhere("TextComponentStringyFlagsFix", type, (dynamicops) -> {
            return (pair) -> {
                return pair.mapSecond((either) -> {
                    return either.mapRight((pair1) -> {
                        return pair1.mapSecond((pair2) -> {
                            return pair2.mapSecond((pair3) -> {
                                return pair3.mapSecond((dynamic) -> {
                                    return dynamic.update("bold", TextComponentStringifiedFlagsFix::stringToBool).update("italic", TextComponentStringifiedFlagsFix::stringToBool).update("underlined", TextComponentStringifiedFlagsFix::stringToBool).update("strikethrough", TextComponentStringifiedFlagsFix::stringToBool).update("obfuscated", TextComponentStringifiedFlagsFix::stringToBool);
                                });
                            });
                        });
                    });
                });
            };
        });
    }

    private static <T> Dynamic<T> stringToBool(Dynamic<T> dynamic) {
        Optional<String> optional = dynamic.asString().result();

        return optional.isPresent() ? dynamic.createBoolean(Boolean.parseBoolean((String) optional.get())) : dynamic;
    }
}
