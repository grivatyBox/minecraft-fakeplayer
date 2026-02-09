package net.minecraft.network.syncher;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vector3f;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RegistryID;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.ChickenVariant;
import net.minecraft.world.entity.animal.CowVariant;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import org.joml.Quaternionf;

public class DataWatcherRegistry {

    private static final RegistryID<DataWatcherSerializer<?>> SERIALIZERS = RegistryID.<DataWatcherSerializer<?>>create(16);
    public static final DataWatcherSerializer<Byte> BYTE = DataWatcherSerializer.<Byte>forValueType(ByteBufCodecs.BYTE);
    public static final DataWatcherSerializer<Integer> INT = DataWatcherSerializer.<Integer>forValueType(ByteBufCodecs.VAR_INT);
    public static final DataWatcherSerializer<Long> LONG = DataWatcherSerializer.<Long>forValueType(ByteBufCodecs.VAR_LONG);
    public static final DataWatcherSerializer<Float> FLOAT = DataWatcherSerializer.<Float>forValueType(ByteBufCodecs.FLOAT);
    public static final DataWatcherSerializer<String> STRING = DataWatcherSerializer.<String>forValueType(ByteBufCodecs.STRING_UTF8);
    public static final DataWatcherSerializer<IChatBaseComponent> COMPONENT = DataWatcherSerializer.<IChatBaseComponent>forValueType(ComponentSerialization.TRUSTED_STREAM_CODEC);
    public static final DataWatcherSerializer<Optional<IChatBaseComponent>> OPTIONAL_COMPONENT = DataWatcherSerializer.<Optional<IChatBaseComponent>>forValueType(ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC);
    public static final DataWatcherSerializer<ItemStack> ITEM_STACK = new DataWatcherSerializer<ItemStack>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ItemStack> codec() {
            return ItemStack.OPTIONAL_STREAM_CODEC;
        }

        public ItemStack copy(ItemStack itemstack) {
            return itemstack.copy();
        }
    };
    public static final DataWatcherSerializer<IBlockData> BLOCK_STATE = DataWatcherSerializer.<IBlockData>forValueType(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY));
    private static final StreamCodec<ByteBuf, Optional<IBlockData>> OPTIONAL_BLOCK_STATE_CODEC = new StreamCodec<ByteBuf, Optional<IBlockData>>() {
        public void encode(ByteBuf bytebuf, Optional<IBlockData> optional) {
            if (optional.isPresent()) {
                VarInt.write(bytebuf, Block.getId((IBlockData) optional.get()));
            } else {
                VarInt.write(bytebuf, 0);
            }

        }

        public Optional<IBlockData> decode(ByteBuf bytebuf) {
            int i = VarInt.read(bytebuf);

            return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
        }
    };
    public static final DataWatcherSerializer<Optional<IBlockData>> OPTIONAL_BLOCK_STATE = DataWatcherSerializer.<Optional<IBlockData>>forValueType(DataWatcherRegistry.OPTIONAL_BLOCK_STATE_CODEC);
    public static final DataWatcherSerializer<Boolean> BOOLEAN = DataWatcherSerializer.<Boolean>forValueType(ByteBufCodecs.BOOL);
    public static final DataWatcherSerializer<ParticleParam> PARTICLE = DataWatcherSerializer.<ParticleParam>forValueType(Particles.STREAM_CODEC);
    public static final DataWatcherSerializer<List<ParticleParam>> PARTICLES = DataWatcherSerializer.<List<ParticleParam>>forValueType(Particles.STREAM_CODEC.apply(ByteBufCodecs.list()));
    public static final DataWatcherSerializer<Vector3f> ROTATIONS = DataWatcherSerializer.<Vector3f>forValueType(Vector3f.STREAM_CODEC);
    public static final DataWatcherSerializer<BlockPosition> BLOCK_POS = DataWatcherSerializer.<BlockPosition>forValueType(BlockPosition.STREAM_CODEC);
    public static final DataWatcherSerializer<Optional<BlockPosition>> OPTIONAL_BLOCK_POS = DataWatcherSerializer.<Optional<BlockPosition>>forValueType(BlockPosition.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final DataWatcherSerializer<EnumDirection> DIRECTION = DataWatcherSerializer.<EnumDirection>forValueType(EnumDirection.STREAM_CODEC);
    public static final DataWatcherSerializer<Optional<EntityReference<EntityLiving>>> OPTIONAL_LIVING_ENTITY_REFERENCE = DataWatcherSerializer.<Optional<EntityReference<EntityLiving>>>forValueType(EntityReference.streamCodec().apply(ByteBufCodecs::optional));
    public static final DataWatcherSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = DataWatcherSerializer.<Optional<GlobalPos>>forValueType(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final DataWatcherSerializer<NBTTagCompound> COMPOUND_TAG = new DataWatcherSerializer<NBTTagCompound>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, NBTTagCompound> codec() {
            return ByteBufCodecs.TRUSTED_COMPOUND_TAG;
        }

        public NBTTagCompound copy(NBTTagCompound nbttagcompound) {
            return nbttagcompound.copy();
        }
    };
    public static final DataWatcherSerializer<VillagerData> VILLAGER_DATA = DataWatcherSerializer.<VillagerData>forValueType(VillagerData.STREAM_CODEC);
    private static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_UNSIGNED_INT_CODEC = new StreamCodec<ByteBuf, OptionalInt>() {
        public OptionalInt decode(ByteBuf bytebuf) {
            int i = VarInt.read(bytebuf);

            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }

        public void encode(ByteBuf bytebuf, OptionalInt optionalint) {
            VarInt.write(bytebuf, optionalint.orElse(-1) + 1);
        }
    };
    public static final DataWatcherSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = DataWatcherSerializer.<OptionalInt>forValueType(DataWatcherRegistry.OPTIONAL_UNSIGNED_INT_CODEC);
    public static final DataWatcherSerializer<EntityPose> POSE = DataWatcherSerializer.<EntityPose>forValueType(EntityPose.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<CatVariant>> CAT_VARIANT = DataWatcherSerializer.<Holder<CatVariant>>forValueType(CatVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<ChickenVariant>> CHICKEN_VARIANT = DataWatcherSerializer.<Holder<ChickenVariant>>forValueType(ChickenVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<CowVariant>> COW_VARIANT = DataWatcherSerializer.<Holder<CowVariant>>forValueType(CowVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<WolfVariant>> WOLF_VARIANT = DataWatcherSerializer.<Holder<WolfVariant>>forValueType(WolfVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<WolfSoundVariant>> WOLF_SOUND_VARIANT = DataWatcherSerializer.<Holder<WolfSoundVariant>>forValueType(WolfSoundVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<FrogVariant>> FROG_VARIANT = DataWatcherSerializer.<Holder<FrogVariant>>forValueType(FrogVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<PigVariant>> PIG_VARIANT = DataWatcherSerializer.<Holder<PigVariant>>forValueType(PigVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = DataWatcherSerializer.<Holder<PaintingVariant>>forValueType(PaintingVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Armadillo.a> ARMADILLO_STATE = DataWatcherSerializer.<Armadillo.a>forValueType(Armadillo.a.STREAM_CODEC);
    public static final DataWatcherSerializer<Sniffer.State> SNIFFER_STATE = DataWatcherSerializer.<Sniffer.State>forValueType(Sniffer.State.STREAM_CODEC);
    public static final DataWatcherSerializer<org.joml.Vector3f> VECTOR3 = DataWatcherSerializer.<org.joml.Vector3f>forValueType(ByteBufCodecs.VECTOR3F);
    public static final DataWatcherSerializer<Quaternionf> QUATERNION = DataWatcherSerializer.<Quaternionf>forValueType(ByteBufCodecs.QUATERNIONF);

    public static void registerSerializer(DataWatcherSerializer<?> datawatcherserializer) {
        DataWatcherRegistry.SERIALIZERS.add(datawatcherserializer);
    }

    @Nullable
    public static DataWatcherSerializer<?> getSerializer(int i) {
        return (DataWatcherSerializer) DataWatcherRegistry.SERIALIZERS.byId(i);
    }

    public static int getSerializedId(DataWatcherSerializer<?> datawatcherserializer) {
        return DataWatcherRegistry.SERIALIZERS.getId(datawatcherserializer);
    }

    private DataWatcherRegistry() {}

    static {
        registerSerializer(DataWatcherRegistry.BYTE);
        registerSerializer(DataWatcherRegistry.INT);
        registerSerializer(DataWatcherRegistry.LONG);
        registerSerializer(DataWatcherRegistry.FLOAT);
        registerSerializer(DataWatcherRegistry.STRING);
        registerSerializer(DataWatcherRegistry.COMPONENT);
        registerSerializer(DataWatcherRegistry.OPTIONAL_COMPONENT);
        registerSerializer(DataWatcherRegistry.ITEM_STACK);
        registerSerializer(DataWatcherRegistry.BOOLEAN);
        registerSerializer(DataWatcherRegistry.ROTATIONS);
        registerSerializer(DataWatcherRegistry.BLOCK_POS);
        registerSerializer(DataWatcherRegistry.OPTIONAL_BLOCK_POS);
        registerSerializer(DataWatcherRegistry.DIRECTION);
        registerSerializer(DataWatcherRegistry.OPTIONAL_LIVING_ENTITY_REFERENCE);
        registerSerializer(DataWatcherRegistry.BLOCK_STATE);
        registerSerializer(DataWatcherRegistry.OPTIONAL_BLOCK_STATE);
        registerSerializer(DataWatcherRegistry.COMPOUND_TAG);
        registerSerializer(DataWatcherRegistry.PARTICLE);
        registerSerializer(DataWatcherRegistry.PARTICLES);
        registerSerializer(DataWatcherRegistry.VILLAGER_DATA);
        registerSerializer(DataWatcherRegistry.OPTIONAL_UNSIGNED_INT);
        registerSerializer(DataWatcherRegistry.POSE);
        registerSerializer(DataWatcherRegistry.CAT_VARIANT);
        registerSerializer(DataWatcherRegistry.COW_VARIANT);
        registerSerializer(DataWatcherRegistry.WOLF_VARIANT);
        registerSerializer(DataWatcherRegistry.WOLF_SOUND_VARIANT);
        registerSerializer(DataWatcherRegistry.FROG_VARIANT);
        registerSerializer(DataWatcherRegistry.PIG_VARIANT);
        registerSerializer(DataWatcherRegistry.CHICKEN_VARIANT);
        registerSerializer(DataWatcherRegistry.OPTIONAL_GLOBAL_POS);
        registerSerializer(DataWatcherRegistry.PAINTING_VARIANT);
        registerSerializer(DataWatcherRegistry.SNIFFER_STATE);
        registerSerializer(DataWatcherRegistry.ARMADILLO_STATE);
        registerSerializer(DataWatcherRegistry.VECTOR3);
        registerSerializer(DataWatcherRegistry.QUATERNION);
    }
}
