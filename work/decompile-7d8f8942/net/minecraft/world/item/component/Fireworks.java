package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record Fireworks(int flightDuration, List<FireworkExplosion> explosions) implements TooltipProvider {

    public static final int MAX_EXPLOSIONS = 256;
    public static final Codec<Fireworks> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration", 0).forGetter(Fireworks::flightDuration), FireworkExplosion.CODEC.sizeLimitedListOf(256).optionalFieldOf("explosions", List.of()).forGetter(Fireworks::explosions)).apply(instance, Fireworks::new);
    });
    public static final StreamCodec<ByteBuf, Fireworks> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Fireworks::flightDuration, FireworkExplosion.STREAM_CODEC.apply(ByteBufCodecs.list(256)), Fireworks::explosions, Fireworks::new);

    public Fireworks(int i, List<FireworkExplosion> list) {
        if (list.size() > 256) {
            throw new IllegalArgumentException("Got " + list.size() + " explosions, but maximum is 256");
        } else {
            this.flightDuration = i;
            this.explosions = list;
        }
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        if (this.flightDuration > 0) {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.firework_rocket.flight").append(CommonComponents.SPACE).append(String.valueOf(this.flightDuration)).withStyle(EnumChatFormat.GRAY));
        }

        FireworkExplosion fireworkexplosion = null;
        int i = 0;

        for (FireworkExplosion fireworkexplosion1 : this.explosions) {
            if (fireworkexplosion == null) {
                fireworkexplosion = fireworkexplosion1;
                i = 1;
            } else if (fireworkexplosion.equals(fireworkexplosion1)) {
                ++i;
            } else {
                addExplosionTooltip(consumer, fireworkexplosion, i);
                fireworkexplosion = fireworkexplosion1;
                i = 1;
            }
        }

        if (fireworkexplosion != null) {
            addExplosionTooltip(consumer, fireworkexplosion, i);
        }

    }

    private static void addExplosionTooltip(Consumer<IChatBaseComponent> consumer, FireworkExplosion fireworkexplosion, int i) {
        IChatBaseComponent ichatbasecomponent = fireworkexplosion.shape().getName();

        if (i == 1) {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.firework_rocket.single_star", ichatbasecomponent).withStyle(EnumChatFormat.GRAY));
        } else {
            consumer.accept(IChatBaseComponent.translatable("item.minecraft.firework_rocket.multiple_stars", i, ichatbasecomponent).withStyle(EnumChatFormat.GRAY));
        }

        fireworkexplosion.addAdditionalTooltip((ichatbasecomponent1) -> {
            consumer.accept(IChatBaseComponent.literal("  ").append(ichatbasecomponent1));
        });
    }
}
