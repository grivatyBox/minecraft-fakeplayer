package net.minecraft.world.item.equipment;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.SystemUtils;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.EnumColor;

public interface EquipmentModels {

    MinecraftKey LEATHER = MinecraftKey.withDefaultNamespace("leather");
    MinecraftKey CHAINMAIL = MinecraftKey.withDefaultNamespace("chainmail");
    MinecraftKey IRON = MinecraftKey.withDefaultNamespace("iron");
    MinecraftKey GOLD = MinecraftKey.withDefaultNamespace("gold");
    MinecraftKey DIAMOND = MinecraftKey.withDefaultNamespace("diamond");
    MinecraftKey TURTLE_SCUTE = MinecraftKey.withDefaultNamespace("turtle_scute");
    MinecraftKey NETHERITE = MinecraftKey.withDefaultNamespace("netherite");
    MinecraftKey ARMADILLO_SCUTE = MinecraftKey.withDefaultNamespace("armadillo_scute");
    MinecraftKey ELYTRA = MinecraftKey.withDefaultNamespace("elytra");
    Map<EnumColor, MinecraftKey> CARPETS = SystemUtils.makeEnumMap(EnumColor.class, (enumcolor) -> {
        return MinecraftKey.withDefaultNamespace(enumcolor.getSerializedName() + "_carpet");
    });
    MinecraftKey TRADER_LLAMA = MinecraftKey.withDefaultNamespace("trader_llama");

    static void bootstrap(BiConsumer<MinecraftKey, EquipmentModel> biconsumer) {
        biconsumer.accept(EquipmentModels.LEATHER, EquipmentModel.builder().addHumanoidLayers(MinecraftKey.withDefaultNamespace("leather"), true).addHumanoidLayers(MinecraftKey.withDefaultNamespace("leather_overlay"), false).addLayers(EquipmentModel.d.HORSE_BODY, EquipmentModel.c.leatherDyeable(MinecraftKey.withDefaultNamespace("leather"), true)).build());
        biconsumer.accept(EquipmentModels.CHAINMAIL, onlyHumanoid("chainmail"));
        biconsumer.accept(EquipmentModels.IRON, humanoidAndHorse("iron"));
        biconsumer.accept(EquipmentModels.GOLD, humanoidAndHorse("gold"));
        biconsumer.accept(EquipmentModels.DIAMOND, humanoidAndHorse("diamond"));
        biconsumer.accept(EquipmentModels.TURTLE_SCUTE, EquipmentModel.builder().addMainHumanoidLayer(MinecraftKey.withDefaultNamespace("turtle_scute"), false).build());
        biconsumer.accept(EquipmentModels.NETHERITE, onlyHumanoid("netherite"));
        biconsumer.accept(EquipmentModels.ARMADILLO_SCUTE, EquipmentModel.builder().addLayers(EquipmentModel.d.WOLF_BODY, EquipmentModel.c.onlyIfDyed(MinecraftKey.withDefaultNamespace("armadillo_scute"), false)).addLayers(EquipmentModel.d.WOLF_BODY, EquipmentModel.c.onlyIfDyed(MinecraftKey.withDefaultNamespace("armadillo_scute_overlay"), true)).build());
        biconsumer.accept(EquipmentModels.ELYTRA, EquipmentModel.builder().addLayers(EquipmentModel.d.WINGS, new EquipmentModel.c(MinecraftKey.withDefaultNamespace("elytra"), Optional.empty(), true)).build());
        Iterator iterator = EquipmentModels.CARPETS.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<EnumColor, MinecraftKey> entry = (Entry) iterator.next();
            EnumColor enumcolor = (EnumColor) entry.getKey();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getValue();

            biconsumer.accept(minecraftkey, EquipmentModel.builder().addLayers(EquipmentModel.d.LLAMA_BODY, new EquipmentModel.c(MinecraftKey.withDefaultNamespace(enumcolor.getSerializedName()))).build());
        }

        biconsumer.accept(EquipmentModels.TRADER_LLAMA, EquipmentModel.builder().addLayers(EquipmentModel.d.LLAMA_BODY, new EquipmentModel.c(MinecraftKey.withDefaultNamespace("trader_llama"))).build());
    }

    private static EquipmentModel onlyHumanoid(String s) {
        return EquipmentModel.builder().addHumanoidLayers(MinecraftKey.withDefaultNamespace(s)).build();
    }

    private static EquipmentModel humanoidAndHorse(String s) {
        return EquipmentModel.builder().addHumanoidLayers(MinecraftKey.withDefaultNamespace(s)).addLayers(EquipmentModel.d.HORSE_BODY, EquipmentModel.c.leatherDyeable(MinecraftKey.withDefaultNamespace(s), false)).build();
    }
}
