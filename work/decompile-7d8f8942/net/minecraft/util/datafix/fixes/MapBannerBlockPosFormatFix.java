package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class MapBannerBlockPosFormatFix extends DataFix {

    public MapBannerBlockPosFormatFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_MAP_DATA);
        OpticFinder<?> opticfinder = type.findField("data");
        OpticFinder<?> opticfinder1 = opticfinder.type().findField("banners");
        OpticFinder<?> opticfinder2 = DSL.typeFinder(((ListType) opticfinder1.type()).getElement());

        return this.fixTypeEverywhereTyped("MapBannerBlockPosFormatFix", type, (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return typed1.updateTyped(opticfinder1, (typed2) -> {
                    return typed2.updateTyped(opticfinder2, (typed3) -> {
                        return typed3.update(DSL.remainderFinder(), (dynamic) -> {
                            return dynamic.update("Pos", ExtraDataFixUtils::fixBlockPos);
                        });
                    });
                });
            });
        });
    }
}
