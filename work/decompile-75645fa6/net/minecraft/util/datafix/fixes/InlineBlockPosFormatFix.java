package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class InlineBlockPosFormatFix extends DataFix {

    public InlineBlockPosFormatFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder<?> opticfinder = this.entityFinder("minecraft:vex");
        OpticFinder<?> opticfinder1 = this.entityFinder("minecraft:phantom");
        OpticFinder<?> opticfinder2 = this.entityFinder("minecraft:turtle");
        List<OpticFinder<?>> list = List.of(this.entityFinder("minecraft:item_frame"), this.entityFinder("minecraft:glow_item_frame"), this.entityFinder("minecraft:painting"), this.entityFinder("minecraft:leash_knot"));

        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("InlineBlockPosFormatFix - player", this.getInputSchema().getType(DataConverterTypes.PLAYER), (typed) -> {
            return typed.update(DSL.remainderFinder(), this::fixPlayer);
        }), this.fixTypeEverywhereTyped("InlineBlockPosFormatFix - entity", this.getInputSchema().getType(DataConverterTypes.ENTITY), (typed) -> {
            typed = typed.update(DSL.remainderFinder(), this::fixLivingEntity).updateTyped(opticfinder, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), this::fixVex);
            }).updateTyped(opticfinder1, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), this::fixPhantom);
            }).updateTyped(opticfinder2, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), this::fixTurtle);
            });

            for (OpticFinder<?> opticfinder3 : list) {
                typed = typed.updateTyped(opticfinder3, (typed1) -> {
                    return typed1.update(DSL.remainderFinder(), this::fixBlockAttached);
                });
            }

            return typed;
        }));
    }

    private OpticFinder<?> entityFinder(String s) {
        return DSL.namedChoice(s, this.getInputSchema().getChoiceType(DataConverterTypes.ENTITY, s));
    }

    private Dynamic<?> fixPlayer(Dynamic<?> dynamic) {
        dynamic = this.fixLivingEntity(dynamic);
        Optional<Number> optional = dynamic.get("SpawnX").asNumber().result();
        Optional<Number> optional1 = dynamic.get("SpawnY").asNumber().result();
        Optional<Number> optional2 = dynamic.get("SpawnZ").asNumber().result();

        if (optional.isPresent() && optional1.isPresent() && optional2.isPresent()) {
            Dynamic<?> dynamic1 = dynamic.createMap(Map.of(dynamic.createString("pos"), ExtraDataFixUtils.createBlockPos(dynamic, ((Number) optional.get()).intValue(), ((Number) optional1.get()).intValue(), ((Number) optional2.get()).intValue())));

            dynamic1 = Dynamic.copyField(dynamic, "SpawnAngle", dynamic1, "angle");
            dynamic1 = Dynamic.copyField(dynamic, "SpawnDimension", dynamic1, "dimension");
            dynamic1 = Dynamic.copyField(dynamic, "SpawnForced", dynamic1, "forced");
            dynamic = dynamic.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle").remove("SpawnDimension").remove("SpawnForced");
            dynamic = dynamic.set("respawn", dynamic1);
        }

        Optional<? extends Dynamic<?>> optional3 = dynamic.get("enteredNetherPosition").result();

        if (optional3.isPresent()) {
            dynamic = dynamic.remove("enteredNetherPosition").set("entered_nether_pos", dynamic.createList(Stream.of(dynamic.createDouble(((Dynamic) optional3.get()).get("x").asDouble(0.0D)), dynamic.createDouble(((Dynamic) optional3.get()).get("y").asDouble(0.0D)), dynamic.createDouble(((Dynamic) optional3.get()).get("z").asDouble(0.0D)))));
        }

        return dynamic;
    }

    private Dynamic<?> fixLivingEntity(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic, "SleepingX", "SleepingY", "SleepingZ", "sleeping_pos");
    }

    private Dynamic<?> fixVex(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic.renameField("LifeTicks", "life_ticks"), "BoundX", "BoundY", "BoundZ", "bound_pos");
    }

    private Dynamic<?> fixPhantom(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic.renameField("Size", "size"), "AX", "AY", "AZ", "anchor_pos");
    }

    private Dynamic<?> fixTurtle(Dynamic<?> dynamic) {
        dynamic = dynamic.remove("TravelPosX").remove("TravelPosY").remove("TravelPosZ");
        dynamic = ExtraDataFixUtils.fixInlineBlockPos(dynamic, "HomePosX", "HomePosY", "HomePosZ", "home_pos");
        return dynamic.renameField("HasEgg", "has_egg");
    }

    private Dynamic<?> fixBlockAttached(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic, "TileX", "TileY", "TileZ", "block_pos");
    }
}
