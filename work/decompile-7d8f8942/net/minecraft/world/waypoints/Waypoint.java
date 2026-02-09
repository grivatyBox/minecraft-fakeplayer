package net.minecraft.world.waypoints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface Waypoint {

    int MAX_RANGE = 60000000;
    AttributeModifier WAYPOINT_TRANSMIT_RANGE_HIDE_MODIFIER = new AttributeModifier(MinecraftKey.withDefaultNamespace("waypoint_transmit_range_hide"), -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    static Item.Info addHideAttribute(Item.Info item_info) {
        return item_info.component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder().add(GenericAttributes.WAYPOINT_TRANSMIT_RANGE, Waypoint.WAYPOINT_TRANSMIT_RANGE_HIDE_MODIFIER, EquipmentSlotGroup.HEAD, ItemAttributeModifiers.b.hidden()).build());
    }

    public static class a {

        public static final Codec<Waypoint.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ResourceKey.codec(WaypointStyleAssets.ROOT_ID).fieldOf("style").forGetter((waypoint_a) -> {
                return waypoint_a.style;
            }), ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color").forGetter((waypoint_a) -> {
                return waypoint_a.color;
            })).apply(instance, Waypoint.a::new);
        });
        public static final StreamCodec<ByteBuf, Waypoint.a> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(WaypointStyleAssets.ROOT_ID), (waypoint_a) -> {
            return waypoint_a.style;
        }, ByteBufCodecs.optional(ByteBufCodecs.RGB_COLOR), (waypoint_a) -> {
            return waypoint_a.color;
        }, Waypoint.a::new);
        public static final Waypoint.a NULL = new Waypoint.a();
        public ResourceKey<WaypointStyleAsset> style;
        public Optional<Integer> color;

        public a() {
            this.style = WaypointStyleAssets.DEFAULT;
            this.color = Optional.empty();
        }

        private a(ResourceKey<WaypointStyleAsset> resourcekey, Optional<Integer> optional) {
            this.style = WaypointStyleAssets.DEFAULT;
            this.color = Optional.empty();
            this.style = resourcekey;
            this.color = optional;
        }

        public boolean hasData() {
            return this.style != WaypointStyleAssets.DEFAULT || this.color.isPresent();
        }

        public Waypoint.a cloneAndAssignStyle(EntityLiving entityliving) {
            ResourceKey<WaypointStyleAsset> resourcekey = this.getOverrideStyle();
            Optional<Integer> optional = this.color.or(() -> {
                return Optional.ofNullable(entityliving.getTeam()).map((scoreboardteam) -> {
                    return scoreboardteam.getColor().getColor();
                }).map((integer) -> {
                    return integer == 0 ? -13619152 : integer;
                });
            });

            return resourcekey == this.style && optional.isEmpty() ? this : new Waypoint.a(resourcekey, optional);
        }

        private ResourceKey<WaypointStyleAsset> getOverrideStyle() {
            return this.style != WaypointStyleAssets.DEFAULT ? this.style : WaypointStyleAssets.DEFAULT;
        }
    }
}
