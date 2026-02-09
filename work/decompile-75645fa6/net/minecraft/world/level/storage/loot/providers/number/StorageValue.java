package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public record StorageValue(MinecraftKey storage, ArgumentNBTKey.g path) implements NumberProvider {

    public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("storage").forGetter(StorageValue::storage), ArgumentNBTKey.g.CODEC.fieldOf("path").forGetter(StorageValue::path)).apply(instance, StorageValue::new);
    });

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.STORAGE;
    }

    private Number getNumericTag(LootTableInfo loottableinfo, Number number) {
        NBTTagCompound nbttagcompound = loottableinfo.getLevel().getServer().getCommandStorage().get(this.storage);

        try {
            List<NBTBase> list = this.path.get(nbttagcompound);

            if (list.size() == 1) {
                Object object = list.getFirst();

                if (object instanceof NBTNumber) {
                    NBTNumber nbtnumber = (NBTNumber) object;

                    return nbtnumber.box();
                }
            }
        } catch (CommandSyntaxException commandsyntaxexception) {
            ;
        }

        return number;
    }

    @Override
    public float getFloat(LootTableInfo loottableinfo) {
        return this.getNumericTag(loottableinfo, 0.0F).floatValue();
    }

    @Override
    public int getInt(LootTableInfo loottableinfo) {
        return this.getNumericTag(loottableinfo, 0).intValue();
    }
}
