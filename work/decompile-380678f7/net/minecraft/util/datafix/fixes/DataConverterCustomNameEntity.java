package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class DataConverterCustomNameEntity extends DataFix {

    public DataConverterCustomNameEntity(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.ENTITY);
        OpticFinder<String> opticfinder = DSL.fieldFinder("id", DataConverterSchemaNamed.namespacedString());
        OpticFinder<String> opticfinder1 = type.findField("CustomName");
        Type<?> type2 = type1.findFieldType("CustomName");

        return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", type, type1, (typed) -> {
            return fixEntity(typed, type1, opticfinder, opticfinder1, type2);
        });
    }

    private static <T> Typed<?> fixEntity(Typed<?> typed, Type<?> type, OpticFinder<String> opticfinder, OpticFinder<String> opticfinder1, Type<T> type1) {
        Optional<String> optional = typed.getOptional(opticfinder1);

        if (optional.isEmpty()) {
            return ExtraDataFixUtils.cast(type, typed);
        } else if (((String) optional.get()).isEmpty()) {
            return SystemUtils.writeAndReadTypedOrThrow(typed, type, (dynamic) -> {
                return dynamic.remove("CustomName");
            });
        } else {
            String s = (String) typed.getOptional(opticfinder).orElse("");
            Dynamic<?> dynamic = fixCustomName(typed.getOps(), (String) optional.get(), s);

            return typed.set(opticfinder1, SystemUtils.readTypedOrThrow(type1, dynamic));
        }
    }

    private static <T> Dynamic<T> fixCustomName(DynamicOps<T> dynamicops, String s, String s1) {
        return "minecraft:commandblock_minecart".equals(s1) ? new Dynamic(dynamicops, dynamicops.createString(s)) : LegacyComponentDataFixUtils.createPlainTextComponent(dynamicops, s);
    }
}
