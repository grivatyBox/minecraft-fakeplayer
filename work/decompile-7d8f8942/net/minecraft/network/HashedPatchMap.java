package net.minecraft.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HashedPatchMap(Map<DataComponentType<?>, Integer> addedComponents, Set<DataComponentType<?>> removedComponents) {

    public static final StreamCodec<RegistryFriendlyByteBuf, HashedPatchMap> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE), ByteBufCodecs.INT, 256), HashedPatchMap::addedComponents, ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE), 256), HashedPatchMap::removedComponents, HashedPatchMap::new);

    public static HashedPatchMap create(DataComponentPatch datacomponentpatch, HashedPatchMap.a hashedpatchmap_a) {
        DataComponentPatch.d datacomponentpatch_d = datacomponentpatch.split();
        Map<DataComponentType<?>, Integer> map = new IdentityHashMap(datacomponentpatch_d.added().size());

        datacomponentpatch_d.added().forEach((typeddatacomponent) -> {
            map.put(typeddatacomponent.type(), (Integer) hashedpatchmap_a.apply(typeddatacomponent));
        });
        return new HashedPatchMap(map, datacomponentpatch_d.removed());
    }

    public boolean matches(DataComponentPatch datacomponentpatch, HashedPatchMap.a hashedpatchmap_a) {
        DataComponentPatch.d datacomponentpatch_d = datacomponentpatch.split();

        if (!datacomponentpatch_d.removed().equals(this.removedComponents)) {
            return false;
        } else if (this.addedComponents.size() != datacomponentpatch_d.added().size()) {
            return false;
        } else {
            for (TypedDataComponent<?> typeddatacomponent : datacomponentpatch_d.added()) {
                Integer integer = (Integer) this.addedComponents.get(typeddatacomponent.type());

                if (integer == null) {
                    return false;
                }

                Integer integer1 = (Integer) hashedpatchmap_a.apply(typeddatacomponent);

                if (!integer1.equals(integer)) {
                    return false;
                }
            }

            return true;
        }
    }

    @FunctionalInterface
    public interface a extends Function<TypedDataComponent<?>, Integer> {}
}
