package net.minecraft.world.entity.variant;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VariantUtils {

    public static final String TAG_VARIANT = "variant";

    public VariantUtils() {}

    public static <T> Holder<T> getDefaultOrAny(IRegistryCustom iregistrycustom, ResourceKey<T> resourcekey) {
        IRegistry<T> iregistry = iregistrycustom.lookupOrThrow(resourcekey.registryKey());
        Optional optional = iregistry.get(resourcekey);

        Objects.requireNonNull(iregistry);
        return (Holder) optional.or(iregistry::getAny).orElseThrow();
    }

    public static <T> Holder<T> getAny(IRegistryCustom iregistrycustom, ResourceKey<? extends IRegistry<T>> resourcekey) {
        return (Holder) iregistrycustom.lookupOrThrow(resourcekey).getAny().orElseThrow();
    }

    public static <T> void writeVariant(ValueOutput valueoutput, Holder<T> holder) {
        holder.unwrapKey().ifPresent((resourcekey) -> {
            valueoutput.store("variant", MinecraftKey.CODEC, resourcekey.location());
        });
    }

    public static <T> Optional<Holder<T>> readVariant(ValueInput valueinput, ResourceKey<? extends IRegistry<T>> resourcekey) {
        Optional optional = valueinput.read("variant", MinecraftKey.CODEC).map((minecraftkey) -> {
            return ResourceKey.create(resourcekey, minecraftkey);
        });
        HolderLookup.a holderlookup_a = valueinput.lookup();

        Objects.requireNonNull(holderlookup_a);
        return optional.flatMap(holderlookup_a::get);
    }

    public static <T extends PriorityProvider<SpawnContext, ?>> Optional<Holder.c<T>> selectVariantToSpawn(SpawnContext spawncontext, ResourceKey<IRegistry<T>> resourcekey) {
        WorldAccess worldaccess = spawncontext.level();
        Stream<Holder.c<T>> stream = worldaccess.registryAccess().lookupOrThrow(resourcekey).listElements();

        return PriorityProvider.pick(stream, Holder::value, worldaccess.getRandom(), spawncontext);
    }
}
