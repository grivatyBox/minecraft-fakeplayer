package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;

public class DataConverterOminousBannerBlockEntityRename extends DataConverterNamedEntity {

    public DataConverterOminousBannerBlockEntityRename(Schema schema, boolean flag) {
        super(schema, flag, "OminousBannerBlockEntityRenameFix", DataConverterTypes.BLOCK_ENTITY, "minecraft:banner");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        OpticFinder<?> opticfinder = typed.getType().findField("CustomName");
        OpticFinder<Pair<String, String>> opticfinder1 = DSL.typeFinder(this.getInputSchema().getType(DataConverterTypes.TEXT_COMPONENT));

        return typed.updateTyped(opticfinder, (typed1) -> {
            return typed1.update(opticfinder1, (pair) -> {
                return pair.mapSecond((s) -> {
                    return s.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
                });
            });
        });
    }
}
