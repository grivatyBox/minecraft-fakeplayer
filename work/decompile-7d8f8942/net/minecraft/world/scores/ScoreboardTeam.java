package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;

public class ScoreboardTeam extends ScoreboardTeamBase {

    private static final int BIT_FRIENDLY_FIRE = 0;
    private static final int BIT_SEE_INVISIBLES = 1;
    private final Scoreboard scoreboard;
    private final String name;
    private final Set<String> players = Sets.newHashSet();
    private IChatBaseComponent displayName;
    private IChatBaseComponent playerPrefix;
    private IChatBaseComponent playerSuffix;
    private boolean allowFriendlyFire;
    private boolean seeFriendlyInvisibles;
    private ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility;
    private ScoreboardTeamBase.EnumNameTagVisibility deathMessageVisibility;
    private EnumChatFormat color;
    private ScoreboardTeamBase.EnumTeamPush collisionRule;
    private final ChatModifier displayNameStyle;

    public ScoreboardTeam(Scoreboard scoreboard, String s) {
        this.playerPrefix = CommonComponents.EMPTY;
        this.playerSuffix = CommonComponents.EMPTY;
        this.allowFriendlyFire = true;
        this.seeFriendlyInvisibles = true;
        this.nameTagVisibility = ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS;
        this.deathMessageVisibility = ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS;
        this.color = EnumChatFormat.RESET;
        this.collisionRule = ScoreboardTeamBase.EnumTeamPush.ALWAYS;
        this.scoreboard = scoreboard;
        this.name = s;
        this.displayName = IChatBaseComponent.literal(s);
        this.displayNameStyle = ChatModifier.EMPTY.withInsertion(s).withHoverEvent(new ChatHoverable.e(IChatBaseComponent.literal(s)));
    }

    public ScoreboardTeam.a pack() {
        return new ScoreboardTeam.a(this.name, Optional.of(this.displayName), this.color != EnumChatFormat.RESET ? Optional.of(this.color) : Optional.empty(), this.allowFriendlyFire, this.seeFriendlyInvisibles, this.playerPrefix, this.playerSuffix, this.nameTagVisibility, this.deathMessageVisibility, this.collisionRule, List.copyOf(this.players));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public IChatMutableComponent getFormattedDisplayName() {
        IChatMutableComponent ichatmutablecomponent = ChatComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(this.displayNameStyle));
        EnumChatFormat enumchatformat = this.getColor();

        if (enumchatformat != EnumChatFormat.RESET) {
            ichatmutablecomponent.withStyle(enumchatformat);
        }

        return ichatmutablecomponent;
    }

    public void setDisplayName(IChatBaseComponent ichatbasecomponent) {
        if (ichatbasecomponent == null) {
            throw new IllegalArgumentException("Name cannot be null");
        } else {
            this.displayName = ichatbasecomponent;
            this.scoreboard.onTeamChanged(this);
        }
    }

    public void setPlayerPrefix(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.playerPrefix = ichatbasecomponent == null ? CommonComponents.EMPTY : ichatbasecomponent;
        this.scoreboard.onTeamChanged(this);
    }

    public IChatBaseComponent getPlayerPrefix() {
        return this.playerPrefix;
    }

    public void setPlayerSuffix(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.playerSuffix = ichatbasecomponent == null ? CommonComponents.EMPTY : ichatbasecomponent;
        this.scoreboard.onTeamChanged(this);
    }

    public IChatBaseComponent getPlayerSuffix() {
        return this.playerSuffix;
    }

    @Override
    public Collection<String> getPlayers() {
        return this.players;
    }

    @Override
    public IChatMutableComponent getFormattedName(IChatBaseComponent ichatbasecomponent) {
        IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.empty().append(this.playerPrefix).append(ichatbasecomponent).append(this.playerSuffix);
        EnumChatFormat enumchatformat = this.getColor();

        if (enumchatformat != EnumChatFormat.RESET) {
            ichatmutablecomponent.withStyle(enumchatformat);
        }

        return ichatmutablecomponent;
    }

    public static IChatMutableComponent formatNameForTeam(@Nullable ScoreboardTeamBase scoreboardteambase, IChatBaseComponent ichatbasecomponent) {
        return scoreboardteambase == null ? ichatbasecomponent.copy() : scoreboardteambase.getFormattedName(ichatbasecomponent);
    }

    @Override
    public boolean isAllowFriendlyFire() {
        return this.allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean flag) {
        this.allowFriendlyFire = flag;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public boolean canSeeFriendlyInvisibles() {
        return this.seeFriendlyInvisibles;
    }

    public void setSeeFriendlyInvisibles(boolean flag) {
        this.seeFriendlyInvisibles = flag;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public ScoreboardTeamBase.EnumNameTagVisibility getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    @Override
    public ScoreboardTeamBase.EnumNameTagVisibility getDeathMessageVisibility() {
        return this.deathMessageVisibility;
    }

    public void setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility scoreboardteambase_enumnametagvisibility) {
        this.nameTagVisibility = scoreboardteambase_enumnametagvisibility;
        this.scoreboard.onTeamChanged(this);
    }

    public void setDeathMessageVisibility(ScoreboardTeamBase.EnumNameTagVisibility scoreboardteambase_enumnametagvisibility) {
        this.deathMessageVisibility = scoreboardteambase_enumnametagvisibility;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public ScoreboardTeamBase.EnumTeamPush getCollisionRule() {
        return this.collisionRule;
    }

    public void setCollisionRule(ScoreboardTeamBase.EnumTeamPush scoreboardteambase_enumteampush) {
        this.collisionRule = scoreboardteambase_enumteampush;
        this.scoreboard.onTeamChanged(this);
    }

    public int packOptions() {
        int i = 0;

        if (this.isAllowFriendlyFire()) {
            i |= 1;
        }

        if (this.canSeeFriendlyInvisibles()) {
            i |= 2;
        }

        return i;
    }

    public void unpackOptions(int i) {
        this.setAllowFriendlyFire((i & 1) > 0);
        this.setSeeFriendlyInvisibles((i & 2) > 0);
    }

    public void setColor(EnumChatFormat enumchatformat) {
        this.color = enumchatformat;
        this.scoreboard.onTeamChanged(this);
    }

    @Override
    public EnumChatFormat getColor() {
        return this.color;
    }

    public static record a(String name, Optional<IChatBaseComponent> displayName, Optional<EnumChatFormat> color, boolean allowFriendlyFire, boolean seeFriendlyInvisibles, IChatBaseComponent memberNamePrefix, IChatBaseComponent memberNameSuffix, ScoreboardTeamBase.EnumNameTagVisibility nameTagVisibility, ScoreboardTeamBase.EnumNameTagVisibility deathMessageVisibility, ScoreboardTeamBase.EnumTeamPush collisionRule, List<String> players) {

        public static final Codec<ScoreboardTeam.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.STRING.fieldOf("Name").forGetter(ScoreboardTeam.a::name), ComponentSerialization.CODEC.optionalFieldOf("DisplayName").forGetter(ScoreboardTeam.a::displayName), EnumChatFormat.COLOR_CODEC.optionalFieldOf("TeamColor").forGetter(ScoreboardTeam.a::color), Codec.BOOL.optionalFieldOf("AllowFriendlyFire", true).forGetter(ScoreboardTeam.a::allowFriendlyFire), Codec.BOOL.optionalFieldOf("SeeFriendlyInvisibles", true).forGetter(ScoreboardTeam.a::seeFriendlyInvisibles), ComponentSerialization.CODEC.optionalFieldOf("MemberNamePrefix", CommonComponents.EMPTY).forGetter(ScoreboardTeam.a::memberNamePrefix), ComponentSerialization.CODEC.optionalFieldOf("MemberNameSuffix", CommonComponents.EMPTY).forGetter(ScoreboardTeam.a::memberNameSuffix), ScoreboardTeamBase.EnumNameTagVisibility.CODEC.optionalFieldOf("NameTagVisibility", ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS).forGetter(ScoreboardTeam.a::nameTagVisibility), ScoreboardTeamBase.EnumNameTagVisibility.CODEC.optionalFieldOf("DeathMessageVisibility", ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS).forGetter(ScoreboardTeam.a::deathMessageVisibility), ScoreboardTeamBase.EnumTeamPush.CODEC.optionalFieldOf("CollisionRule", ScoreboardTeamBase.EnumTeamPush.ALWAYS).forGetter(ScoreboardTeam.a::collisionRule), Codec.STRING.listOf().optionalFieldOf("Players", List.of()).forGetter(ScoreboardTeam.a::players)).apply(instance, ScoreboardTeam.a::new);
        });
    }
}
