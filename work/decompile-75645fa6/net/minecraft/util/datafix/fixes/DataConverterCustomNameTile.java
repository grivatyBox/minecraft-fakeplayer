package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class DataConverterCustomNameTile extends DataFix {

    private static final Set<String> NAMEABLE_BLOCK_ENTITIES = Set.of("minecraft:beacon", "minecraft:banner", "minecraft:brewing_stand", "minecraft:chest", "minecraft:trapped_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:enchanting_table", "minecraft:furnace", "minecraft:hopper", "minecraft:shulker_box");

    public DataConverterCustomNameTile(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder<String> opticfinder = DSL.fieldFinder("id", DataConverterSchemaNamed.namespacedString());
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.BLOCK_ENTITY);
        Type<?> type2 = ExtraDataFixUtils.patchSubType(type, type, type1);

        return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", type, type1, (typed) -> {
            Optional<String> optional = typed.getOptional(opticfinder);

            return optional.isPresent() && !DataConverterCustomNameTile.NAMEABLE_BLOCK_ENTITIES.contains(optional.get()) ? ExtraDataFixUtils.cast(type1, typed) : SystemUtils.writeAndReadTypedOrThrow(ExtraDataFixUtils.cast(type2, typed), type1, DataConverterCustomNameTile::fixTagCustomName);
        });
    }

    public static <T> Dynamic<T> fixTagCustomName(Dynamic<T> dynamic) {
        String s = dynamic.get("CustomName").asString("");

        return s.isEmpty() ? dynamic.remove("CustomName") : dynamic.set("CustomName", LegacyComponentDataFixUtils.createPlainTextComponent(dynamic.getOps(), s));
    }
}
