package net.minecraft.world.entity.variant;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

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

    public static <T> void writeVariant(NBTTagCompound nbttagcompound, Holder<T> holder) {
        holder.unwrapKey().ifPresent((resourcekey) -> {
            nbttagcompound.store("variant", MinecraftKey.CODEC, resourcekey.location());
        });
    }

    public static <T> Optional<Holder<T>> readVariant(NBTTagCompound nbttagcompound, IRegistryCustom iregistrycustom, ResourceKey<? extends IRegistry<T>> resourcekey) {
        Optional optional = nbttagcompound.read("variant", MinecraftKey.CODEC).map((minecraftkey) -> {
            return ResourceKey.create(resourcekey, minecraftkey);
        });

        Objects.requireNonNull(iregistrycustom);
        return optional.flatMap(iregistrycustom::get);
    }
}
