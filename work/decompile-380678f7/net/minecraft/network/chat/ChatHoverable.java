package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;

public interface ChatHoverable {

    Codec<ChatHoverable> CODEC = ChatHoverable.EnumHoverAction.CODEC.dispatch("action", ChatHoverable::action, (chathoverable_enumhoveraction) -> {
        return chathoverable_enumhoveraction.codec;
    });

    ChatHoverable.EnumHoverAction action();

    public static record e(IChatBaseComponent value) implements ChatHoverable {

        public static final MapCodec<ChatHoverable.e> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ComponentSerialization.CODEC.fieldOf("value").forGetter(ChatHoverable.e::value)).apply(instance, ChatHoverable.e::new);
        });

        @Override
        public ChatHoverable.EnumHoverAction action() {
            return ChatHoverable.EnumHoverAction.SHOW_TEXT;
        }
    }

    public static record d(ItemStack item) implements ChatHoverable {

        public static final MapCodec<ChatHoverable.d> CODEC = ItemStack.MAP_CODEC.xmap(ChatHoverable.d::new, ChatHoverable.d::item);

        public d(ItemStack itemstack) {
            itemstack = itemstack.copy();
            this.item = itemstack;
        }

        @Override
        public ChatHoverable.EnumHoverAction action() {
            return ChatHoverable.EnumHoverAction.SHOW_ITEM;
        }

        public boolean equals(Object object) {
            boolean flag;

            if (object instanceof ChatHoverable.d chathoverable_d) {
                if (ItemStack.matches(this.item, chathoverable_d.item)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }

        public int hashCode() {
            return ItemStack.hashItemAndComponents(this.item);
        }
    }

    public static record c(ChatHoverable.b entity) implements ChatHoverable {

        public static final MapCodec<ChatHoverable.c> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ChatHoverable.b.CODEC.forGetter(ChatHoverable.c::entity)).apply(instance, ChatHoverable.c::new);
        });

        @Override
        public ChatHoverable.EnumHoverAction action() {
            return ChatHoverable.EnumHoverAction.SHOW_ENTITY;
        }
    }

    public static class b {

        public static final MapCodec<ChatHoverable.b> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id").forGetter((chathoverable_b) -> {
                return chathoverable_b.type;
            }), UUIDUtil.LENIENT_CODEC.fieldOf("uuid").forGetter((chathoverable_b) -> {
                return chathoverable_b.uuid;
            }), ComponentSerialization.CODEC.optionalFieldOf("name").forGetter((chathoverable_b) -> {
                return chathoverable_b.name;
            })).apply(instance, ChatHoverable.b::new);
        });
        public final EntityTypes<?> type;
        public final UUID uuid;
        public final Optional<IChatBaseComponent> name;
        @Nullable
        private List<IChatBaseComponent> linesCache;

        public b(EntityTypes<?> entitytypes, UUID uuid, @Nullable IChatBaseComponent ichatbasecomponent) {
            this(entitytypes, uuid, Optional.ofNullable(ichatbasecomponent));
        }

        public b(EntityTypes<?> entitytypes, UUID uuid, Optional<IChatBaseComponent> optional) {
            this.type = entitytypes;
            this.uuid = uuid;
            this.name = optional;
        }

        public List<IChatBaseComponent> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = new ArrayList();
                Optional optional = this.name;
                List list = this.linesCache;

                Objects.requireNonNull(this.linesCache);
                optional.ifPresent(list::add);
                this.linesCache.add(IChatBaseComponent.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(IChatBaseComponent.literal(this.uuid.toString()));
            }

            return this.linesCache;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                ChatHoverable.b chathoverable_b = (ChatHoverable.b) object;

                return this.type.equals(chathoverable_b.type) && this.uuid.equals(chathoverable_b.uuid) && this.name.equals(chathoverable_b.name);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int i = this.type.hashCode();

            i = 31 * i + this.uuid.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }

    public static enum EnumHoverAction implements INamable {

        SHOW_TEXT("show_text", true, ChatHoverable.e.CODEC), SHOW_ITEM("show_item", true, ChatHoverable.d.CODEC), SHOW_ENTITY("show_entity", true, ChatHoverable.c.CODEC);

        public static final Codec<ChatHoverable.EnumHoverAction> UNSAFE_CODEC = INamable.<ChatHoverable.EnumHoverAction>fromValues(ChatHoverable.EnumHoverAction::values);
        public static final Codec<ChatHoverable.EnumHoverAction> CODEC = ChatHoverable.EnumHoverAction.UNSAFE_CODEC.validate(ChatHoverable.EnumHoverAction::filterForSerialization);
        private final String name;
        private final boolean allowFromServer;
        final MapCodec<? extends ChatHoverable> codec;

        private EnumHoverAction(final String s, final boolean flag, final MapCodec mapcodec) {
            this.name = s;
            this.allowFromServer = flag;
            this.codec = mapcodec;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<ChatHoverable.EnumHoverAction> filterForSerialization(ChatHoverable.EnumHoverAction chathoverable_enumhoveraction) {
            return !chathoverable_enumhoveraction.isAllowedFromServer() ? DataResult.error(() -> {
                return "Action not allowed: " + String.valueOf(chathoverable_enumhoveraction);
            }) : DataResult.success(chathoverable_enumhoveraction, Lifecycle.stable());
        }
    }
}
