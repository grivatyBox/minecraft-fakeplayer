package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.SystemUtils;

public class DataConverterOminousBannerRename extends ItemStackTagFix {

    public DataConverterOminousBannerRename(Schema schema) {
        super(schema, "OminousBannerRenameFix", (s) -> {
            return s.equals("minecraft:white_banner");
        });
    }

    private <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic) {
        return dynamic.update("display", (dynamic1) -> {
            return dynamic1.update("Name", (dynamic2) -> {
                Optional<String> optional = dynamic2.asString().result();

                return optional.isPresent() ? dynamic2.createString(((String) optional.get()).replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"")) : dynamic2;
            });
        });
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> typed) {
        return SystemUtils.writeAndReadTypedOrThrow(typed, typed.getType(), this::fixItemStackTag);
    }
}
