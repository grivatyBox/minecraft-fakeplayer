package net.minecraft.util.datafix.fixes;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class LockComponentPredicateFix extends ItemStackComponentRemainderFix {

    public static final Escaper ESCAPER = Escapers.builder().addEscape('"', "\\\"").addEscape('\\', "\\\\").build();

    public LockComponentPredicateFix(Schema schema) {
        super(schema, "LockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return fixLock(dynamic);
    }

    public static <T> Dynamic<T> fixLock(Dynamic<T> dynamic) {
        Optional<String> optional = dynamic.asString().result();

        if (optional.isPresent()) {
            Escaper escaper = LockComponentPredicateFix.ESCAPER;
            Dynamic<T> dynamic1 = dynamic.createString("\"" + escaper.escape((String) optional.get()) + "\"");
            Dynamic<T> dynamic2 = dynamic.emptyMap().set("minecraft:custom_name", dynamic1);

            return dynamic.emptyMap().set("components", dynamic2);
        } else {
            return dynamic.emptyMap();
        }
    }
}
