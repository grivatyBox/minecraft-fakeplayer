package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public record BlocksAttacks(float blockDelaySeconds, float disableCooldownScale, List<BlocksAttacks.a> damageReductions, BlocksAttacks.b itemDamage, Optional<TagKey<DamageType>> bypassedBy, Optional<Holder<SoundEffect>> blockSound, Optional<Holder<SoundEffect>> disableSound) {

    public static final Codec<BlocksAttacks> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("block_delay_seconds", 0.0F).forGetter(BlocksAttacks::blockDelaySeconds), ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_cooldown_scale", 1.0F).forGetter(BlocksAttacks::disableCooldownScale), BlocksAttacks.a.CODEC.listOf().optionalFieldOf("damage_reductions", List.of(new BlocksAttacks.a(90.0F, Optional.empty(), 0.0F, 1.0F))).forGetter(BlocksAttacks::damageReductions), BlocksAttacks.b.CODEC.optionalFieldOf("item_damage", BlocksAttacks.b.DEFAULT).forGetter(BlocksAttacks::itemDamage), TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacks::bypassedBy), SoundEffect.CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacks::blockSound), SoundEffect.CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacks::disableSound)).apply(instance, BlocksAttacks::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, BlocksAttacks> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, BlocksAttacks::blockDelaySeconds, ByteBufCodecs.FLOAT, BlocksAttacks::disableCooldownScale, BlocksAttacks.a.STREAM_CODEC.apply(ByteBufCodecs.list()), BlocksAttacks::damageReductions, BlocksAttacks.b.STREAM_CODEC, BlocksAttacks::itemDamage, TagKey.streamCodec(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional), BlocksAttacks::bypassedBy, SoundEffect.STREAM_CODEC.apply(ByteBufCodecs::optional), BlocksAttacks::blockSound, SoundEffect.STREAM_CODEC.apply(ByteBufCodecs::optional), BlocksAttacks::disableSound, BlocksAttacks::new);

    public void onBlocked(WorldServer worldserver, EntityLiving entityliving) {
        this.blockSound.ifPresent((holder) -> {
            worldserver.playSound((Entity) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), holder, entityliving.getSoundSource(), 1.0F, 0.8F + worldserver.random.nextFloat() * 0.4F);
        });
    }

    public void disable(WorldServer worldserver, EntityLiving entityliving, float f, ItemStack itemstack) {
        int i = this.disableBlockingForTicks(f);

        if (i > 0) {
            if (entityliving instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entityliving;

                entityhuman.getCooldowns().addCooldown(itemstack, i);
            }

            entityliving.stopUsingItem();
            this.disableSound.ifPresent((holder) -> {
                worldserver.playSound((Entity) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), holder, entityliving.getSoundSource(), 0.8F, 0.8F + worldserver.random.nextFloat() * 0.4F);
            });
        }

    }

    public void hurtBlockingItem(World world, ItemStack itemstack, EntityLiving entityliving, EnumHand enumhand, float f) {
        if (entityliving instanceof EntityHuman entityhuman) {
            if (!world.isClientSide) {
                entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
            }

            int i = this.itemDamage.apply(f);

            if (i > 0) {
                itemstack.hurtAndBreak(i, entityliving, EntityLiving.getSlotForHand(enumhand));
            }

        }
    }

    private int disableBlockingForTicks(float f) {
        float f1 = f * this.disableCooldownScale;

        return f1 > 0.0F ? Math.round(f1 * 20.0F) : 0;
    }

    public int blockDelayTicks() {
        return Math.round(this.blockDelaySeconds * 20.0F);
    }

    public float resolveBlockedDamage(DamageSource damagesource, float f, double d0) {
        float f1 = 0.0F;

        for (BlocksAttacks.a blocksattacks_a : this.damageReductions) {
            f1 += blocksattacks_a.resolve(damagesource, f, d0);
        }

        return MathHelper.clamp(f1, 0.0F, f);
    }

    public static record a(float horizontalBlockingAngle, Optional<HolderSet<DamageType>> type, float base, float factor) {

        public static final Codec<BlocksAttacks.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("horizontal_blocking_angle", 90.0F).forGetter(BlocksAttacks.a::horizontalBlockingAngle), RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(BlocksAttacks.a::type), Codec.FLOAT.fieldOf("base").forGetter(BlocksAttacks.a::base), Codec.FLOAT.fieldOf("factor").forGetter(BlocksAttacks.a::factor)).apply(instance, BlocksAttacks.a::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, BlocksAttacks.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, BlocksAttacks.a::horizontalBlockingAngle, ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional), BlocksAttacks.a::type, ByteBufCodecs.FLOAT, BlocksAttacks.a::base, ByteBufCodecs.FLOAT, BlocksAttacks.a::factor, BlocksAttacks.a::new);

        public float resolve(DamageSource damagesource, float f, double d0) {
            return d0 > (double) (((float) Math.PI / 180F) * this.horizontalBlockingAngle) ? 0.0F : (this.type.isPresent() && !((HolderSet) this.type.get()).contains(damagesource.typeHolder()) ? 0.0F : MathHelper.clamp(this.base + this.factor * f, 0.0F, f));
        }
    }

    public static record b(float threshold, float base, float factor) {

        public static final Codec<BlocksAttacks.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("threshold").forGetter(BlocksAttacks.b::threshold), Codec.FLOAT.fieldOf("base").forGetter(BlocksAttacks.b::base), Codec.FLOAT.fieldOf("factor").forGetter(BlocksAttacks.b::factor)).apply(instance, BlocksAttacks.b::new);
        });
        public static final StreamCodec<ByteBuf, BlocksAttacks.b> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, BlocksAttacks.b::threshold, ByteBufCodecs.FLOAT, BlocksAttacks.b::base, ByteBufCodecs.FLOAT, BlocksAttacks.b::factor, BlocksAttacks.b::new);
        public static final BlocksAttacks.b DEFAULT = new BlocksAttacks.b(1.0F, 0.0F, 1.0F);

        public int apply(float f) {
            return f < this.threshold ? 0 : MathHelper.floor(this.base + this.factor * f);
        }
    }
}
