package net.minecraft.world.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlayerSkin(ClientAsset.c body, @Nullable ClientAsset.c cape, @Nullable ClientAsset.c elytra, PlayerModelType model, boolean secure) {

    public static PlayerSkin insecure(ClientAsset.c clientasset_c, @Nullable ClientAsset.c clientasset_c1, @Nullable ClientAsset.c clientasset_c2, PlayerModelType playermodeltype) {
        return new PlayerSkin(clientasset_c, clientasset_c1, clientasset_c2, playermodeltype, false);
    }

    public PlayerSkin with(PlayerSkin.Patch playerskin_patch) {
        return playerskin_patch.equals(PlayerSkin.Patch.EMPTY) ? this : insecure((ClientAsset.c) DataFixUtils.orElse(playerskin_patch.body, this.body), (ClientAsset.c) DataFixUtils.orElse(playerskin_patch.cape, this.cape), (ClientAsset.c) DataFixUtils.orElse(playerskin_patch.elytra, this.elytra), (PlayerModelType) playerskin_patch.model.orElse(this.model));
    }

    public static record Patch(Optional<ClientAsset.b> body, Optional<ClientAsset.b> cape, Optional<ClientAsset.b> elytra, Optional<PlayerModelType> model) {

        public static final PlayerSkin.Patch EMPTY = new PlayerSkin.Patch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        public static final MapCodec<PlayerSkin.Patch> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ClientAsset.b.CODEC.optionalFieldOf("texture").forGetter(PlayerSkin.Patch::body), ClientAsset.b.CODEC.optionalFieldOf("cape").forGetter(PlayerSkin.Patch::cape), ClientAsset.b.CODEC.optionalFieldOf("elytra").forGetter(PlayerSkin.Patch::elytra), PlayerModelType.CODEC.optionalFieldOf("model").forGetter(PlayerSkin.Patch::model)).apply(instance, PlayerSkin.Patch::create);
        });
        public static final StreamCodec<ByteBuf, PlayerSkin.Patch> STREAM_CODEC = StreamCodec.composite(ClientAsset.b.STREAM_CODEC.apply(ByteBufCodecs::optional), PlayerSkin.Patch::body, ClientAsset.b.STREAM_CODEC.apply(ByteBufCodecs::optional), PlayerSkin.Patch::cape, ClientAsset.b.STREAM_CODEC.apply(ByteBufCodecs::optional), PlayerSkin.Patch::elytra, PlayerModelType.STREAM_CODEC.apply(ByteBufCodecs::optional), PlayerSkin.Patch::model, PlayerSkin.Patch::create);

        public static PlayerSkin.Patch create(Optional<ClientAsset.b> optional, Optional<ClientAsset.b> optional1, Optional<ClientAsset.b> optional2, Optional<PlayerModelType> optional3) {
            return optional.isEmpty() && optional1.isEmpty() && optional2.isEmpty() && optional3.isEmpty() ? PlayerSkin.Patch.EMPTY : new PlayerSkin.Patch(optional, optional1, optional2, optional3);
        }
    }
}
