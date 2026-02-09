package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class ScoreboardScore implements ReadOnlyScoreInfo {

    public static final MapCodec<ScoreboardScore> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.INT.optionalFieldOf("Score", 0).forGetter(ScoreboardScore::value), Codec.BOOL.optionalFieldOf("Locked", false).forGetter(ScoreboardScore::isLocked), ComponentSerialization.CODEC.optionalFieldOf("display").forGetter((scoreboardscore) -> {
            return Optional.ofNullable(scoreboardscore.display);
        }), NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter((scoreboardscore) -> {
            return Optional.ofNullable(scoreboardscore.numberFormat);
        })).apply(instance, ScoreboardScore::new);
    });
    private int value;
    private boolean locked = true;
    @Nullable
    private IChatBaseComponent display;
    @Nullable
    private NumberFormat numberFormat;

    public ScoreboardScore() {}

    private ScoreboardScore(int i, boolean flag, Optional<IChatBaseComponent> optional, Optional<NumberFormat> optional1) {
        this.value = i;
        this.locked = flag;
        this.display = (IChatBaseComponent) optional.orElse((Object) null);
        this.numberFormat = (NumberFormat) optional1.orElse((Object) null);
    }

    @Override
    public int value() {
        return this.value;
    }

    public void value(int i) {
        this.value = i;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean flag) {
        this.locked = flag;
    }

    @Nullable
    public IChatBaseComponent display() {
        return this.display;
    }

    public void display(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.display = ichatbasecomponent;
    }

    @Nullable
    @Override
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat numberformat) {
        this.numberFormat = numberformat;
    }
}
