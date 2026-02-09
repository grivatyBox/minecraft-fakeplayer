package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public abstract class NamedEntityWriteReadFix extends DataFix {

    private final String name;
    private final String entityName;
    private final TypeReference type;

    public NamedEntityWriteReadFix(Schema schema, boolean flag, String s, TypeReference typereference, String s1) {
        super(schema, flag);
        this.name = s;
        this.type = typereference;
        this.entityName = s1;
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.type);
        Type<?> type1 = this.getInputSchema().getChoiceType(this.type, this.entityName);
        Type<?> type2 = this.getOutputSchema().getType(this.type);
        OpticFinder<?> opticfinder = DSL.namedChoice(this.entityName, type1);
        Type<?> type3 = ExtraDataFixUtils.patchSubType(type, type, type2);

        return this.fix(type, type2, type3, opticfinder);
    }

    private <S, T, A> TypeRewriteRule fix(Type<S> type, Type<T> type1, Type<?> type2, OpticFinder<A> opticfinder) {
        return this.fixTypeEverywhereTyped(this.name, type, type1, (typed) -> {
            if (typed.getOptional(opticfinder).isEmpty()) {
                return ExtraDataFixUtils.cast(type1, typed);
            } else {
                Typed<?> typed1 = ExtraDataFixUtils.cast(type2, typed);

                return SystemUtils.writeAndReadTypedOrThrow(typed1, type1, this::fix);
            }
        });
    }

    protected abstract <T> Dynamic<T> fix(Dynamic<T> dynamic);
}
