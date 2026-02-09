package net.minecraft.world.item.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public abstract sealed class ResolvableProfile implements TooltipProvider {

    private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.mapEither(ExtraCodecs.STORED_GAME_PROFILE, ResolvableProfile.Partial.MAP_CODEC).forGetter(ResolvableProfile::unpack), PlayerSkin.Patch.MAP_CODEC.forGetter(ResolvableProfile::skinPatch)).apply(instance, ResolvableProfile::create);
    });
    public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(ResolvableProfile.FULL_CODEC, ExtraCodecs.PLAYER_NAME, ResolvableProfile::createUnresolved);
    public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.either(ByteBufCodecs.GAME_PROFILE, ResolvableProfile.Partial.STREAM_CODEC), ResolvableProfile::unpack, PlayerSkin.Patch.STREAM_CODEC, ResolvableProfile::skinPatch, ResolvableProfile::create);
    protected final GameProfile partialProfile;
    protected final PlayerSkin.Patch skinPatch;

    private static ResolvableProfile create(Either<GameProfile, ResolvableProfile.Partial> either, PlayerSkin.Patch playerskin_patch) {
        return (ResolvableProfile) either.map((gameprofile) -> {
            return new ResolvableProfile.Static(Either.left(gameprofile), playerskin_patch);
        }, (resolvableprofile_partial) -> {
            return (ResolvableProfile) (resolvableprofile_partial.properties.isEmpty() && resolvableprofile_partial.id.isPresent() != resolvableprofile_partial.name.isPresent() ? (ResolvableProfile) resolvableprofile_partial.name.map((s) -> {
                return new ResolvableProfile.Dynamic(Either.left(s), playerskin_patch);
            }).orElseGet(() -> {
                return new ResolvableProfile.Dynamic(Either.right((UUID) resolvableprofile_partial.id.get()), playerskin_patch);
            }) : new ResolvableProfile.Static(Either.right(resolvableprofile_partial), playerskin_patch));
        });
    }

    public static ResolvableProfile createResolved(GameProfile gameprofile) {
        return new ResolvableProfile.Static(Either.left(gameprofile), PlayerSkin.Patch.EMPTY);
    }

    public static ResolvableProfile createUnresolved(String s) {
        return new ResolvableProfile.Dynamic(Either.left(s), PlayerSkin.Patch.EMPTY);
    }

    public static ResolvableProfile createUnresolved(UUID uuid) {
        return new ResolvableProfile.Dynamic(Either.right(uuid), PlayerSkin.Patch.EMPTY);
    }

    protected abstract Either<GameProfile, ResolvableProfile.Partial> unpack();

    protected ResolvableProfile(GameProfile gameprofile, PlayerSkin.Patch playerskin_patch) {
        this.partialProfile = gameprofile;
        this.skinPatch = playerskin_patch;
    }

    public abstract CompletableFuture<GameProfile> resolveProfile(ProfileResolver profileresolver);

    public GameProfile partialProfile() {
        return this.partialProfile;
    }

    public PlayerSkin.Patch skinPatch() {
        return this.skinPatch;
    }

    static GameProfile createPartialProfile(Optional<String> optional, Optional<UUID> optional1, PropertyMap propertymap) {
        String s = (String) optional.orElse("");
        UUID uuid = (UUID) optional1.orElseGet(() -> {
            return (UUID) optional.map(UUIDUtil::createOfflinePlayerUUID).orElse(SystemUtils.NIL_UUID);
        });

        return new GameProfile(uuid, s, propertymap);
    }

    public abstract Optional<String> name();

    protected static record Partial(Optional<String> name, Optional<UUID> id, PropertyMap properties) {

        public static final ResolvableProfile.Partial EMPTY = new ResolvableProfile.Partial(Optional.empty(), Optional.empty(), PropertyMap.EMPTY);
        static final MapCodec<ResolvableProfile.Partial> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ResolvableProfile.Partial::name), UUIDUtil.CODEC.optionalFieldOf("id").forGetter(ResolvableProfile.Partial::id), ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", PropertyMap.EMPTY).forGetter(ResolvableProfile.Partial::properties)).apply(instance, ResolvableProfile.Partial::new);
        });
        public static final StreamCodec<ByteBuf, ResolvableProfile.Partial> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.PLAYER_NAME.apply(ByteBufCodecs::optional), ResolvableProfile.Partial::name, UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), ResolvableProfile.Partial::id, ByteBufCodecs.GAME_PROFILE_PROPERTIES, ResolvableProfile.Partial::properties, ResolvableProfile.Partial::new);

        private GameProfile createProfile() {
            return ResolvableProfile.createPartialProfile(this.name, this.id, this.properties);
        }
    }

    public static final class Static extends ResolvableProfile {

        public static final ResolvableProfile.Static EMPTY = new ResolvableProfile.Static(Either.right(ResolvableProfile.Partial.EMPTY), PlayerSkin.Patch.EMPTY);
        private final Either<GameProfile, ResolvableProfile.Partial> contents;

        public Static(Either<GameProfile, ResolvableProfile.Partial> either, PlayerSkin.Patch playerskin_patch) {
            super((GameProfile) either.map((gameprofile) -> {
                return gameprofile;
            }, ResolvableProfile.Partial::createProfile), playerskin_patch);
            this.contents = either;
        }

        @Override
        public CompletableFuture<GameProfile> resolveProfile(ProfileResolver profileresolver) {
            return CompletableFuture.completedFuture(this.partialProfile);
        }

        @Override
        protected Either<GameProfile, ResolvableProfile.Partial> unpack() {
            return this.contents;
        }

        @Override
        public Optional<String> name() {
            return (Optional) this.contents.map((gameprofile) -> {
                return Optional.of(gameprofile.name());
            }, (resolvableprofile_partial) -> {
                return resolvableprofile_partial.name;
            });
        }

        public boolean equals(Object object) {
            boolean flag;

            if (this != object) {
                label28:
                {
                    if (object instanceof ResolvableProfile.Static) {
                        ResolvableProfile.Static resolvableprofile_static = (ResolvableProfile.Static) object;

                        if (this.contents.equals(resolvableprofile_static.contents) && this.skinPatch.equals(resolvableprofile_static.skinPatch)) {
                            break label28;
                        }
                    }

                    flag = false;
                    return flag;
                }
            }

            flag = true;
            return flag;
        }

        public int hashCode() {
            int i = 31 + this.contents.hashCode();

            i = 31 * i + this.skinPatch.hashCode();
            return i;
        }

        @Override
        public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {}
    }

    public static final class Dynamic extends ResolvableProfile {

        private static final IChatBaseComponent DYNAMIC_TOOLTIP = IChatBaseComponent.translatable("component.profile.dynamic").withStyle(EnumChatFormat.GRAY);
        private final Either<String, UUID> nameOrId;

        public Dynamic(Either<String, UUID> either, PlayerSkin.Patch playerskin_patch) {
            super(ResolvableProfile.createPartialProfile(either.left(), either.right(), PropertyMap.EMPTY), playerskin_patch);
            this.nameOrId = either;
        }

        @Override
        public Optional<String> name() {
            return this.nameOrId.left();
        }

        public boolean equals(Object object) {
            boolean flag;

            if (this != object) {
                label28:
                {
                    if (object instanceof ResolvableProfile.Dynamic) {
                        ResolvableProfile.Dynamic resolvableprofile_dynamic = (ResolvableProfile.Dynamic) object;

                        if (this.nameOrId.equals(resolvableprofile_dynamic.nameOrId) && this.skinPatch.equals(resolvableprofile_dynamic.skinPatch)) {
                            break label28;
                        }
                    }

                    flag = false;
                    return flag;
                }
            }

            flag = true;
            return flag;
        }

        public int hashCode() {
            int i = 31 + this.nameOrId.hashCode();

            i = 31 * i + this.skinPatch.hashCode();
            return i;
        }

        @Override
        protected Either<GameProfile, ResolvableProfile.Partial> unpack() {
            return Either.right(new ResolvableProfile.Partial(this.nameOrId.left(), this.nameOrId.right(), PropertyMap.EMPTY));
        }

        @Override
        public CompletableFuture<GameProfile> resolveProfile(ProfileResolver profileresolver) {
            return CompletableFuture.supplyAsync(() -> {
                return (GameProfile) profileresolver.fetchByNameOrId(this.nameOrId).orElse(this.partialProfile);
            }, SystemUtils.nonCriticalIoPool());
        }

        @Override
        public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
            consumer.accept(ResolvableProfile.Dynamic.DYNAMIC_TOOLTIP);
        }
    }
}
