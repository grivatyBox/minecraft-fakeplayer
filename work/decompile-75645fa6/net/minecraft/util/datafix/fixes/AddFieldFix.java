package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class AddFieldFix extends DataFix {

    private final String name;
    private final TypeReference type;
    private final String fieldName;
    private final String[] path;
    private final Function<Dynamic<?>, Dynamic<?>> fieldGenerator;

    public AddFieldFix(Schema schema, TypeReference typereference, String s, Function<Dynamic<?>, Dynamic<?>> function, String... astring) {
        super(schema, false);
        this.name = String.format(Locale.ROOT, "Adding field `%s` to type `%s`", s, typereference.typeName().toLowerCase());
        this.type = typereference;
        this.fieldName = s;
        this.path = astring;
        this.fieldGenerator = function;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return this.addField(dynamic, 0);
            });
        });
    }

    private Dynamic<?> addField(Dynamic<?> dynamic, int i) {
        if (i >= this.path.length) {
            return dynamic.set(this.fieldName, (Dynamic) this.fieldGenerator.apply(dynamic));
        } else {
            Optional<? extends Dynamic<?>> optional = dynamic.get(this.path[i]).result();

            return optional.isEmpty() ? dynamic : this.addField((Dynamic) optional.get(), i + 1);
        }
    }
}
