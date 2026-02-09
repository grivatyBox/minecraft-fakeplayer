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
import net.minecraft.SystemUtils;
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
            return fixEntity(typed, opticfinder, opticfinder1, type2);
        });
    }

    private static <T> Typed<?> fixEntity(Typed<?> typed, OpticFinder<String> opticfinder, OpticFinder<String> opticfinder1, Type<T> type) {
        return typed.update(opticfinder1, type, (s) -> {
            String s1 = (String) typed.getOptional(opticfinder).orElse("");
            Dynamic<?> dynamic = fixCustomName(typed.getOps(), s, s1);

            return SystemUtils.readTypedOrThrow(type, dynamic).getValue();
        });
    }

    private static <T> Dynamic<T> fixCustomName(DynamicOps<T> dynamicops, String s, String s1) {
        return "minecraft:commandblock_minecart".equals(s1) ? new Dynamic(dynamicops, dynamicops.createString(s)) : LegacyComponentDataFixUtils.createPlainTextComponent(dynamicops, s);
    }
}
