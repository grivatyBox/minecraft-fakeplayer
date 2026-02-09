package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class ScoreboardObjective {

    private final Scoreboard scoreboard;
    private final String name;
    private final IScoreboardCriteria criteria;
    public IChatBaseComponent displayName;
    private IChatBaseComponent formattedDisplayName;
    private IScoreboardCriteria.EnumScoreboardHealthDisplay renderType;
    private boolean displayAutoUpdate;
    @Nullable
    private NumberFormat numberFormat;

    public ScoreboardObjective(Scoreboard scoreboard, String s, IScoreboardCriteria iscoreboardcriteria, IChatBaseComponent ichatbasecomponent, IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay, boolean flag, @Nullable NumberFormat numberformat) {
        this.scoreboard = scoreboard;
        this.name = s;
        this.criteria = iscoreboardcriteria;
        this.displayName = ichatbasecomponent;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.renderType = iscoreboardcriteria_enumscoreboardhealthdisplay;
        this.displayAutoUpdate = flag;
        this.numberFormat = numberformat;
    }

    public ScoreboardObjective.a pack() {
        return new ScoreboardObjective.a(this.name, this.criteria, this.displayName, this.renderType, this.displayAutoUpdate, Optional.ofNullable(this.numberFormat));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public String getName() {
        return this.name;
    }

    public IScoreboardCriteria getCriteria() {
        return this.criteria;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public boolean displayAutoUpdate() {
        return this.displayAutoUpdate;
    }

    @Nullable
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public NumberFormat numberFormatOrDefault(NumberFormat numberformat) {
        return (NumberFormat) Objects.requireNonNullElse(this.numberFormat, numberformat);
    }

    private IChatBaseComponent createFormattedDisplayName() {
        return ChatComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle((chatmodifier) -> {
            return chatmodifier.withHoverEvent(new ChatHoverable.e(IChatBaseComponent.literal(this.name)));
        }));
    }

    public IChatBaseComponent getFormattedDisplayName() {
        return this.formattedDisplayName;
    }

    public void setDisplayName(IChatBaseComponent ichatbasecomponent) {
        this.displayName = ichatbasecomponent;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.scoreboard.onObjectiveChanged(this);
    }

    public IScoreboardCriteria.EnumScoreboardHealthDisplay getRenderType() {
        return this.renderType;
    }

    public void setRenderType(IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay) {
        this.renderType = iscoreboardcriteria_enumscoreboardhealthdisplay;
        this.scoreboard.onObjectiveChanged(this);
    }

    public void setDisplayAutoUpdate(boolean flag) {
        this.displayAutoUpdate = flag;
        this.scoreboard.onObjectiveChanged(this);
    }

    public void setNumberFormat(@Nullable NumberFormat numberformat) {
        this.numberFormat = numberformat;
        this.scoreboard.onObjectiveChanged(this);
    }

    public static record a(String name, IScoreboardCriteria criteria, IChatBaseComponent displayName, IScoreboardCriteria.EnumScoreboardHealthDisplay renderType, boolean displayAutoUpdate, Optional<NumberFormat> numberFormat) {

        public static final Codec<ScoreboardObjective.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.STRING.fieldOf("Name").forGetter(ScoreboardObjective.a::name), IScoreboardCriteria.CODEC.optionalFieldOf("CriteriaName", IScoreboardCriteria.DUMMY).forGetter(ScoreboardObjective.a::criteria), ComponentSerialization.CODEC.fieldOf("DisplayName").forGetter(ScoreboardObjective.a::displayName), IScoreboardCriteria.EnumScoreboardHealthDisplay.CODEC.optionalFieldOf("RenderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER).forGetter(ScoreboardObjective.a::renderType), Codec.BOOL.optionalFieldOf("display_auto_update", false).forGetter(ScoreboardObjective.a::displayAutoUpdate), NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(ScoreboardObjective.a::numberFormat)).apply(instance, ScoreboardObjective.a::new);
        });
    }
}
