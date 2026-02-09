package net.minecraft.world.item.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.TileEntitySkull;

public record ResolvableProfile(Optional<String> name, Optional<UUID> id, PropertyMap properties, GameProfile gameProfile) {

    private static final Codec<ResolvableProfile> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ResolvableProfile::name), UUIDUtil.CODEC.optionalFieldOf("id").forGetter(ResolvableProfile::id), ExtraCodecs.PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(ResolvableProfile::properties)).apply(instance, ResolvableProfile::new);
    });
    public static final Codec<ResolvableProfile> CODEC = Codec.withAlternative(ResolvableProfile.FULL_CODEC, ExtraCodecs.PLAYER_NAME, (s) -> {
        return new ResolvableProfile(Optional.of(s), Optional.empty(), new PropertyMap());
    });
    public static final StreamCodec<ByteBuf, ResolvableProfile> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(16).apply(ByteBufCodecs::optional), ResolvableProfile::name, UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), ResolvableProfile::id, ByteBufCodecs.GAME_PROFILE_PROPERTIES, ResolvableProfile::properties, ResolvableProfile::new);

    public ResolvableProfile(Optional<String> optional, Optional<UUID> optional1, PropertyMap propertymap) {
        this(optional, optional1, propertymap, createGameProfile(optional1, optional, propertymap));
    }

    public ResolvableProfile(GameProfile gameprofile) {
        this(Optional.of(gameprofile.getName()), Optional.of(gameprofile.getId()), gameprofile.getProperties(), gameprofile);
    }

    @Nullable
    public ResolvableProfile pollResolve() {
        if (this.isResolved()) {
            return this;
        } else {
            Optional<GameProfile> optional;

            if (this.id.isPresent()) {
                optional = (Optional) TileEntitySkull.fetchGameProfile((UUID) this.id.get()).getNow((Object) null);
            } else {
                optional = (Optional) TileEntitySkull.fetchGameProfile((String) this.name.orElseThrow()).getNow((Object) null);
            }

            return optional != null ? this.createProfile(optional) : null;
        }
    }

    public CompletableFuture<ResolvableProfile> resolve() {
        return this.isResolved() ? CompletableFuture.completedFuture(this) : (this.id.isPresent() ? TileEntitySkull.fetchGameProfile((UUID) this.id.get()).thenApply(this::createProfile) : TileEntitySkull.fetchGameProfile((String) this.name.orElseThrow()).thenApply(this::createProfile));
    }

    private ResolvableProfile createProfile(Optional<GameProfile> optional) {
        return new ResolvableProfile((GameProfile) optional.orElseGet(() -> {
            return createGameProfile(this.id, this.name);
        }));
    }

    private static GameProfile createGameProfile(Optional<UUID> optional, Optional<String> optional1) {
        return new GameProfile((UUID) optional.orElse(SystemUtils.NIL_UUID), (String) optional1.orElse(""));
    }

    private static GameProfile createGameProfile(Optional<UUID> optional, Optional<String> optional1, PropertyMap propertymap) {
        GameProfile gameprofile = createGameProfile(optional, optional1);

        gameprofile.getProperties().putAll(propertymap);
        return gameprofile;
    }

    public boolean isResolved() {
        return !this.properties.isEmpty() ? true : this.id.isPresent() == this.name.isPresent();
    }
}
