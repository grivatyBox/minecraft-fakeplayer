package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.SystemUtils;

public class DataConverterHorse extends DataConverterEntityName {

    public DataConverterHorse(Schema schema, boolean flag) {
        super("EntityHorseSplitFix", schema, flag);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String s, Typed<?> typed) {
        Dynamic<?> dynamic = (Dynamic) typed.get(DSL.remainderFinder());

        if (Objects.equals("EntityHorse", s)) {
            int i = dynamic.get("Type").asInt(0);
            String s1;

            switch (i) {
                case 1:
                    s1 = "Donkey";
                    break;
                case 2:
                    s1 = "Mule";
                    break;
                case 3:
                    s1 = "ZombieHorse";
                    break;
                case 4:
                    s1 = "SkeletonHorse";
                    break;
                default:
                    s1 = "Horse";
            }

            String s2 = s1;

            dynamic.remove("Type");
            Type<?> type = (Type) this.getOutputSchema().findChoiceType(DataConverterTypes.ENTITY).types().get(s2);

            return Pair.of(s2, SystemUtils.writeAndReadTypedOrThrow(typed, type, (dynamic1) -> {
                return dynamic1;
            }));
        } else {
            return Pair.of(s, typed);
        }
    }
}
