package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChatHoverable {

    public static final Codec<ChatHoverable> CODEC = Codec.either(ChatHoverable.d.CODEC.codec(), ChatHoverable.d.LEGACY_CODEC.codec()).xmap((either) -> {
        return new ChatHoverable((ChatHoverable.d) either.map((chathoverable_d) -> {
            return chathoverable_d;
        }, (chathoverable_d) -> {
            return chathoverable_d;
        }));
    }, (chathoverable) -> {
        return Either.left(chathoverable.event);
    });
    private final ChatHoverable.d<?> event;

    public <T> ChatHoverable(ChatHoverable.EnumHoverAction<T> chathoverable_enumhoveraction, T t0) {
        this(new ChatHoverable.d<>(chathoverable_enumhoveraction, t0));
    }

    private ChatHoverable(ChatHoverable.d<?> chathoverable_d) {
        this.event = chathoverable_d;
    }

    public ChatHoverable.EnumHoverAction<?> getAction() {
        return this.event.action;
    }

    @Nullable
    public <T> T getValue(ChatHoverable.EnumHoverAction<T> chathoverable_enumhoveraction) {
        return this.event.action == chathoverable_enumhoveraction ? chathoverable_enumhoveraction.cast(this.event.value) : null;
    }

    public boolean equals(Object object) {
        return this == object ? true : (object != null && this.getClass() == object.getClass() ? ((ChatHoverable) object).event.equals(this.event) : false);
    }

    public String toString() {
        return this.event.toString();
    }

    public int hashCode() {
        return this.event.hashCode();
    }

    private static record d<T> (ChatHoverable.EnumHoverAction<T> action, T value) {

        public static final MapCodec<ChatHoverable.d<?>> CODEC = ChatHoverable.EnumHoverAction.CODEC.dispatchMap("action", ChatHoverable.d::action, (chathoverable_enumhoveraction) -> {
            return chathoverable_enumhoveraction.codec;
        });
        public static final MapCodec<ChatHoverable.d<?>> LEGACY_CODEC = ChatHoverable.EnumHoverAction.CODEC.dispatchMap("action", ChatHoverable.d::action, (chathoverable_enumhoveraction) -> {
            return chathoverable_enumhoveraction.legacyCodec;
        });
    }

    public static class EnumHoverAction<T> implements INamable {

        public static final ChatHoverable.EnumHoverAction<IChatBaseComponent> SHOW_TEXT = new ChatHoverable.EnumHoverAction<>("show_text", true, ComponentSerialization.CODEC, DataResult::success);
        public static final ChatHoverable.EnumHoverAction<ChatHoverable.c> SHOW_ITEM = new ChatHoverable.EnumHoverAction<>("show_item", true, ChatHoverable.c.CODEC, ChatHoverable.c::legacyCreate);
        public static final ChatHoverable.EnumHoverAction<ChatHoverable.b> SHOW_ENTITY = new ChatHoverable.EnumHoverAction<>("show_entity", true, ChatHoverable.b.CODEC, ChatHoverable.b::legacyCreate);
        public static final Codec<ChatHoverable.EnumHoverAction<?>> UNSAFE_CODEC = INamable.fromValues(() -> {
            return new ChatHoverable.EnumHoverAction[]{ChatHoverable.EnumHoverAction.SHOW_TEXT, ChatHoverable.EnumHoverAction.SHOW_ITEM, ChatHoverable.EnumHoverAction.SHOW_ENTITY};
        });
        public static final Codec<ChatHoverable.EnumHoverAction<?>> CODEC = ExtraCodecs.validate(ChatHoverable.EnumHoverAction.UNSAFE_CODEC, ChatHoverable.EnumHoverAction::filterForSerialization);
        private final String name;
        private final boolean allowFromServer;
        final Codec<ChatHoverable.d<T>> codec;
        final Codec<ChatHoverable.d<T>> legacyCodec;

        public EnumHoverAction(String s, boolean flag, Codec<T> codec, Function<IChatBaseComponent, DataResult<T>> function) {
            this.name = s;
            this.allowFromServer = flag;
            this.codec = codec.xmap((object) -> {
                return new ChatHoverable.d<>(this, object);
            }, (chathoverable_d) -> {
                return chathoverable_d.value;
            }).fieldOf("contents").codec();
            this.legacyCodec = Codec.of(Encoder.error("Can't encode in legacy format"), ComponentSerialization.CODEC.flatMap(function).map((object) -> {
                return new ChatHoverable.d<>(this, object);
            }));
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        T cast(Object object) {
            return object;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<ChatHoverable.EnumHoverAction<?>> filterForSerialization(@Nullable ChatHoverable.EnumHoverAction<?> chathoverable_enumhoveraction) {
            return chathoverable_enumhoveraction == null ? DataResult.error(() -> {
                return "Unknown action";
            }) : (!chathoverable_enumhoveraction.isAllowedFromServer() ? DataResult.error(() -> {
                return "Action not allowed: " + chathoverable_enumhoveraction;
            }) : DataResult.success(chathoverable_enumhoveraction, Lifecycle.stable()));
        }
    }

    public static class c {

        public static final Codec<ChatHoverable.c> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter((chathoverable_c) -> {
                return chathoverable_c.item;
            }), ExtraCodecs.strictOptionalField(Codec.INT, "count", 1).forGetter((chathoverable_c) -> {
                return chathoverable_c.count;
            }), ExtraCodecs.strictOptionalField(MojangsonParser.AS_CODEC, "tag").forGetter((chathoverable_c) -> {
                return chathoverable_c.tag;
            })).apply(instance, ChatHoverable.c::new);
        });
        public static final Codec<ChatHoverable.c> CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), ChatHoverable.c.FULL_CODEC).xmap((either) -> {
            return (ChatHoverable.c) either.map((item) -> {
                return new ChatHoverable.c(item, 1, Optional.empty());
            }, (chathoverable_c) -> {
                return chathoverable_c;
            });
        }, Either::right);
        private final Item item;
        private final int count;
        private final Optional<NBTTagCompound> tag;
        @Nullable
        private ItemStack itemStack;

        c(Item item, int i, @Nullable NBTTagCompound nbttagcompound) {
            this(item, i, Optional.ofNullable(nbttagcompound));
        }

        c(Item item, int i, Optional<NBTTagCompound> optional) {
            this.item = item;
            this.count = i;
            this.tag = optional;
        }

        public c(ItemStack itemstack) {
            this(itemstack.getItem(), itemstack.getCount(), itemstack.getTag() != null ? Optional.of(itemstack.getTag().copy()) : Optional.empty());
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                ChatHoverable.c chathoverable_c = (ChatHoverable.c) object;

                return this.count == chathoverable_c.count && this.item.equals(chathoverable_c.item) && this.tag.equals(chathoverable_c.tag);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int i = this.item.hashCode();

            i = 31 * i + this.count;
            i = 31 * i + this.tag.hashCode();
            return i;
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null) {
                this.itemStack = new ItemStack(this.item, this.count);
                Optional optional = this.tag;
                ItemStack itemstack = this.itemStack;

                Objects.requireNonNull(this.itemStack);
                optional.ifPresent(itemstack::setTag);
            }

            return this.itemStack;
        }

        private static DataResult<ChatHoverable.c> legacyCreate(IChatBaseComponent ichatbasecomponent) {
            try {
                NBTTagCompound nbttagcompound = MojangsonParser.parseTag(ichatbasecomponent.getString());

                return DataResult.success(new ChatHoverable.c(ItemStack.of(nbttagcompound)));
            } catch (CommandSyntaxException commandsyntaxexception) {
                return DataResult.error(() -> {
                    return "Failed to parse item tag: " + commandsyntaxexception.getMessage();
                });
            }
        }
    }

    public static class b {

        public static final Codec<ChatHoverable.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter((chathoverable_b) -> {
                return chathoverable_b.type;
            }), UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter((chathoverable_b) -> {
                return chathoverable_b.id;
            }), ExtraCodecs.strictOptionalField(ComponentSerialization.CODEC, "name").forGetter((chathoverable_b) -> {
                return chathoverable_b.name;
            })).apply(instance, ChatHoverable.b::new);
        });
        public final EntityTypes<?> type;
        public final UUID id;
        public final Optional<IChatBaseComponent> name;
        @Nullable
        private List<IChatBaseComponent> linesCache;

        public b(EntityTypes<?> entitytypes, UUID uuid, @Nullable IChatBaseComponent ichatbasecomponent) {
            this(entitytypes, uuid, Optional.ofNullable(ichatbasecomponent));
        }

        public b(EntityTypes<?> entitytypes, UUID uuid, Optional<IChatBaseComponent> optional) {
            this.type = entitytypes;
            this.id = uuid;
            this.name = optional;
        }

        public static DataResult<ChatHoverable.b> legacyCreate(IChatBaseComponent ichatbasecomponent) {
            try {
                NBTTagCompound nbttagcompound = MojangsonParser.parseTag(ichatbasecomponent.getString());
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("name"));
                EntityTypes<?> entitytypes = (EntityTypes) BuiltInRegistries.ENTITY_TYPE.get(new MinecraftKey(nbttagcompound.getString("type")));
                UUID uuid = UUID.fromString(nbttagcompound.getString("id"));

                return DataResult.success(new ChatHoverable.b(entitytypes, uuid, ichatmutablecomponent));
            } catch (Exception exception) {
                return DataResult.error(() -> {
                    return "Failed to parse tooltip: " + exception.getMessage();
                });
            }
        }

        public List<IChatBaseComponent> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = new ArrayList();
                Optional optional = this.name;
                List list = this.linesCache;

                Objects.requireNonNull(this.linesCache);
                optional.ifPresent(list::add);
                this.linesCache.add(IChatBaseComponent.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(IChatBaseComponent.literal(this.id.toString()));
            }

            return this.linesCache;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                ChatHoverable.b chathoverable_b = (ChatHoverable.b) object;

                return this.type.equals(chathoverable_b.type) && this.id.equals(chathoverable_b.id) && this.name.equals(chathoverable_b.name);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int i = this.type.hashCode();

            i = 31 * i + this.id.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }
}
