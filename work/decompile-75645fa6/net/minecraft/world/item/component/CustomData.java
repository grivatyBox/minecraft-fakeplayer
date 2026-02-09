package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public final class CustomData {

    public static final CustomData EMPTY = new CustomData(new NBTTagCompound());
    public static final Codec<NBTTagCompound> COMPOUND_TAG_CODEC = Codec.withAlternative(NBTTagCompound.CODEC, MojangsonParser.FLATTENED_CODEC);
    public static final Codec<CustomData> CODEC = CustomData.COMPOUND_TAG_CODEC.xmap(CustomData::new, (customdata) -> {
        return customdata.tag;
    });
    /** @deprecated */
    @Deprecated
    public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, (customdata) -> {
        return customdata.tag;
    });
    private final NBTTagCompound tag;

    private CustomData(NBTTagCompound nbttagcompound) {
        this.tag = nbttagcompound;
    }

    public static CustomData of(NBTTagCompound nbttagcompound) {
        return new CustomData(nbttagcompound.copy());
    }

    public boolean matchedBy(NBTTagCompound nbttagcompound) {
        return GameProfileSerializer.compareNbt(nbttagcompound, this.tag, true);
    }

    public static void update(DataComponentType<CustomData> datacomponenttype, ItemStack itemstack, Consumer<NBTTagCompound> consumer) {
        CustomData customdata = ((CustomData) itemstack.getOrDefault(datacomponenttype, CustomData.EMPTY)).update(consumer);

        if (customdata.tag.isEmpty()) {
            itemstack.remove(datacomponenttype);
        } else {
            itemstack.set(datacomponenttype, customdata);
        }

    }

    public static void set(DataComponentType<CustomData> datacomponenttype, ItemStack itemstack, NBTTagCompound nbttagcompound) {
        if (!nbttagcompound.isEmpty()) {
            itemstack.set(datacomponenttype, of(nbttagcompound));
        } else {
            itemstack.remove(datacomponenttype);
        }

    }

    public CustomData update(Consumer<NBTTagCompound> consumer) {
        NBTTagCompound nbttagcompound = this.tag.copy();

        consumer.accept(nbttagcompound);
        return new CustomData(nbttagcompound);
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public NBTTagCompound copyTag() {
        return this.tag.copy();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof CustomData) {
            CustomData customdata = (CustomData) object;

            return this.tag.equals(customdata.tag);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.tag.hashCode();
    }

    public String toString() {
        return this.tag.toString();
    }
}
