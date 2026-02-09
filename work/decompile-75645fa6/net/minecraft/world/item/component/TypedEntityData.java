package net.minecraft.world.item.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public final class TypedEntityData<IdType> implements TooltipProvider {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TYPE_TAG = "id";
    final IdType type;
    final NBTTagCompound tag;

    public static <T> Codec<TypedEntityData<T>> codec(final Codec<T> codec) {
        return new Codec<TypedEntityData<T>>() {
            public <V> DataResult<Pair<TypedEntityData<T>, V>> decode(DynamicOps<V> dynamicops, V v0) {
                DataResult<T> dataresult = dynamicops.get(v0, "id").flatMap((object) -> {
                    return codec.parse(dynamicops, object).mapError((s) -> {
                        return "Failed to parse 'id': " + s;
                    });
                });
                DataResult<Pair<NBTTagCompound, V>> dataresult1 = CustomData.COMPOUND_TAG_CODEC.decode(dynamicops, dynamicops.remove(v0, "id"));

                return dataresult.apply2stable((object, pair) -> {
                    return new Pair(new TypedEntityData(object, (NBTTagCompound) pair.getFirst()), pair.getSecond());
                }, dataresult1);
            }

            public <V> DataResult<V> encode(TypedEntityData<T> typedentitydata, DynamicOps<V> dynamicops, V v0) {
                return codec.encodeStart(asNbtOps(dynamicops), typedentitydata.type).flatMap((nbtbase) -> {
                    NBTTagCompound nbttagcompound = typedentitydata.tag.copy();

                    nbttagcompound.put("id", nbtbase);
                    return CustomData.COMPOUND_TAG_CODEC.encode(nbttagcompound, dynamicops, v0);
                });
            }

            private static <T> DynamicOps<NBTBase> asNbtOps(DynamicOps<T> dynamicops) {
                if (dynamicops instanceof RegistryOps<T> registryops) {
                    return registryops.<NBTBase>withParent(DynamicOpsNBT.INSTANCE);
                } else {
                    return DynamicOpsNBT.INSTANCE;
                }
            }
        };
    }

    public static <B extends ByteBuf, T> StreamCodec<B, TypedEntityData<T>> streamCodec(StreamCodec<B, T> streamcodec) {
        return StreamCodec.composite(streamcodec, TypedEntityData::type, ByteBufCodecs.COMPOUND_TAG, TypedEntityData::tag, TypedEntityData::new);
    }

    TypedEntityData(IdType idtype, NBTTagCompound nbttagcompound) {
        this.type = idtype;
        this.tag = stripId(nbttagcompound);
    }

    public static <T> TypedEntityData<T> of(T t0, NBTTagCompound nbttagcompound) {
        return new TypedEntityData<T>(t0, nbttagcompound);
    }

    private static NBTTagCompound stripId(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.contains("id")) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.copy();

            nbttagcompound1.remove("id");
            return nbttagcompound1;
        } else {
            return nbttagcompound;
        }
    }

    public IdType type() {
        return this.type;
    }

    public boolean contains(String s) {
        return this.tag.contains(s);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (!(object instanceof TypedEntityData)) {
            return false;
        } else {
            TypedEntityData<?> typedentitydata = (TypedEntityData) object;

            return this.type == typedentitydata.type && this.tag.equals(typedentitydata.tag);
        }
    }

    public int hashCode() {
        return 31 * this.type.hashCode() + this.tag.hashCode();
    }

    public String toString() {
        String s = String.valueOf(this.type);

        return s + " " + String.valueOf(this.tag);
    }

    public void loadInto(Entity entity) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(entity.problemPath(), TypedEntityData.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, entity.registryAccess());

            entity.saveWithoutId(tagvalueoutput);
            NBTTagCompound nbttagcompound = tagvalueoutput.buildResult();
            UUID uuid = entity.getUUID();

            nbttagcompound.merge(this.getUnsafe());
            entity.load(TagValueInput.create(problemreporter_j, entity.registryAccess(), nbttagcompound));
            entity.setUUID(uuid);
        }

    }

    public boolean loadInto(TileEntity tileentity, HolderLookup.a holderlookup_a) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(tileentity.problemPath(), TypedEntityData.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, holderlookup_a);

            tileentity.saveCustomOnly((ValueOutput) tagvalueoutput);
            NBTTagCompound nbttagcompound = tagvalueoutput.buildResult();
            NBTTagCompound nbttagcompound1 = nbttagcompound.copy();

            nbttagcompound.merge(this.getUnsafe());
            if (!nbttagcompound.equals(nbttagcompound1)) {
                try {
                    tileentity.loadCustomOnly(TagValueInput.create(problemreporter_j, holderlookup_a, nbttagcompound));
                    tileentity.setChanged();
                    return true;
                } catch (Exception exception) {
                    TypedEntityData.LOGGER.warn("Failed to apply custom data to block entity at {}", tileentity.getBlockPos(), exception);

                    try {
                        tileentity.loadCustomOnly(TagValueInput.create(problemreporter_j.forChild(() -> {
                            return "(rollback)";
                        }), holderlookup_a, nbttagcompound1));
                    } catch (Exception exception1) {
                        TypedEntityData.LOGGER.warn("Failed to rollback block entity at {} after failure", tileentity.getBlockPos(), exception1);
                    }
                }
            }

            return false;
        }
    }

    private NBTTagCompound tag() {
        return this.tag;
    }

    /** @deprecated */
    @Deprecated
    public NBTTagCompound getUnsafe() {
        return this.tag;
    }

    public NBTTagCompound copyTagWithoutId() {
        return this.tag.copy();
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        if (this.type.getClass() == EntityTypes.class) {
            EntityTypes<?> entitytypes = (EntityTypes) this.type;

            if (item_b.isPeaceful() && !entitytypes.isAllowedInPeaceful()) {
                consumer.accept(IChatBaseComponent.translatable("item.spawn_egg.peaceful").withStyle(EnumChatFormat.RED));
            }
        }

    }
}
