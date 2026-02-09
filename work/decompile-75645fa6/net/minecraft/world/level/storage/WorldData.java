package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.World;

public interface WorldData {

    WorldData.a getRespawnData();

    long getGameTime();

    long getDayTime();

    boolean isThundering();

    boolean isRaining();

    void setRaining(boolean flag);

    boolean isHardcore();

    EnumDifficulty getDifficulty();

    boolean isDifficultyLocked();

    default void fillCrashReportCategory(CrashReportSystemDetails crashreportsystemdetails, LevelHeightAccessor levelheightaccessor) {
        crashreportsystemdetails.setDetail("Level spawn location", () -> {
            return CrashReportSystemDetails.formatLocation(levelheightaccessor, this.getRespawnData().pos());
        });
        crashreportsystemdetails.setDetail("Level time", () -> {
            return String.format(Locale.ROOT, "%d game time, %d day time", this.getGameTime(), this.getDayTime());
        });
    }

    public static record a(GlobalPos globalPos, float yaw, float pitch) {

        public static final WorldData.a DEFAULT = new WorldData.a(GlobalPos.of(World.OVERWORLD, BlockPosition.ZERO), 0.0F, 0.0F);
        public static final MapCodec<WorldData.a> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(GlobalPos.MAP_CODEC.forGetter(WorldData.a::globalPos), Codec.floatRange(-180.0F, 180.0F).fieldOf("yaw").forGetter(WorldData.a::yaw), Codec.floatRange(-90.0F, 90.0F).fieldOf("pitch").forGetter(WorldData.a::pitch)).apply(instance, WorldData.a::new);
        });
        public static final Codec<WorldData.a> CODEC = WorldData.a.MAP_CODEC.codec();
        public static final StreamCodec<ByteBuf, WorldData.a> STREAM_CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC, WorldData.a::globalPos, ByteBufCodecs.FLOAT, WorldData.a::yaw, ByteBufCodecs.FLOAT, WorldData.a::pitch, WorldData.a::new);

        public static WorldData.a of(ResourceKey<World> resourcekey, BlockPosition blockposition, float f, float f1) {
            return new WorldData.a(GlobalPos.of(resourcekey, blockposition.immutable()), f, f1);
        }

        public ResourceKey<World> dimension() {
            return this.globalPos.dimension();
        }

        public BlockPosition pos() {
            return this.globalPos.pos();
        }
    }
}
