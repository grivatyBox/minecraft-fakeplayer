package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public abstract class AbstractBlockPropertyFix extends DataFix {

    private final String name;

    public AbstractBlockPropertyFix(Schema schema, String s) {
        super(schema, false);
        this.name = s;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(DataConverterTypes.BLOCK_STATE), (typed) -> {
            return typed.update(DSL.remainderFinder(), this::fixBlockState);
        });
    }

    private Dynamic<?> fixBlockState(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.get("Name").asString().result().map(DataConverterSchemaNamed::ensureNamespaced);

        return optional.isPresent() && this.shouldFix((String) optional.get()) ? dynamic.update("Properties", (dynamic1) -> {
            return this.fixProperties((String) optional.get(), dynamic1);
        }) : dynamic;
    }

    protected abstract boolean shouldFix(String s);

    protected abstract <T> Dynamic<T> fixProperties(String s, Dynamic<T> dynamic);
}
