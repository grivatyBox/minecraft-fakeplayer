package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public abstract class ScoreboardTeamBase {

    public ScoreboardTeamBase() {}

    public boolean isAlliedTo(@Nullable ScoreboardTeamBase scoreboardteambase) {
        return scoreboardteambase == null ? false : this == scoreboardteambase;
    }

    public abstract String getName();

    public abstract IChatMutableComponent getFormattedName(IChatBaseComponent ichatbasecomponent);

    public abstract boolean canSeeFriendlyInvisibles();

    public abstract boolean isAllowFriendlyFire();

    public abstract ScoreboardTeamBase.EnumNameTagVisibility getNameTagVisibility();

    public abstract EnumChatFormat getColor();

    public abstract Collection<String> getPlayers();

    public abstract ScoreboardTeamBase.EnumNameTagVisibility getDeathMessageVisibility();

    public abstract ScoreboardTeamBase.EnumTeamPush getCollisionRule();

    public static enum EnumNameTagVisibility implements INamable {

        ALWAYS("always", 0), NEVER("never", 1), HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2), HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        public static final Codec<ScoreboardTeamBase.EnumNameTagVisibility> CODEC = INamable.<ScoreboardTeamBase.EnumNameTagVisibility>fromEnum(ScoreboardTeamBase.EnumNameTagVisibility::values);
        private static final IntFunction<ScoreboardTeamBase.EnumNameTagVisibility> BY_ID = ByIdMap.<ScoreboardTeamBase.EnumNameTagVisibility>continuous((scoreboardteambase_enumnametagvisibility) -> {
            return scoreboardteambase_enumnametagvisibility.id;
        }, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, ScoreboardTeamBase.EnumNameTagVisibility> STREAM_CODEC = ByteBufCodecs.idMapper(ScoreboardTeamBase.EnumNameTagVisibility.BY_ID, (scoreboardteambase_enumnametagvisibility) -> {
            return scoreboardteambase_enumnametagvisibility.id;
        });
        public final String name;
        public final int id;

        private EnumNameTagVisibility(final String s, final int i) {
            this.name = s;
            this.id = i;
        }

        public IChatBaseComponent getDisplayName() {
            return IChatBaseComponent.translatable("team.visibility." + this.name);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static enum EnumTeamPush implements INamable {

        ALWAYS("always", 0), NEVER("never", 1), PUSH_OTHER_TEAMS("pushOtherTeams", 2), PUSH_OWN_TEAM("pushOwnTeam", 3);

        public static final Codec<ScoreboardTeamBase.EnumTeamPush> CODEC = INamable.<ScoreboardTeamBase.EnumTeamPush>fromEnum(ScoreboardTeamBase.EnumTeamPush::values);
        private static final IntFunction<ScoreboardTeamBase.EnumTeamPush> BY_ID = ByIdMap.<ScoreboardTeamBase.EnumTeamPush>continuous((scoreboardteambase_enumteampush) -> {
            return scoreboardteambase_enumteampush.id;
        }, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, ScoreboardTeamBase.EnumTeamPush> STREAM_CODEC = ByteBufCodecs.idMapper(ScoreboardTeamBase.EnumTeamPush.BY_ID, (scoreboardteambase_enumteampush) -> {
            return scoreboardteambase_enumteampush.id;
        });
        public final String name;
        public final int id;

        private EnumTeamPush(final String s, final int i) {
            this.name = s;
            this.id = i;
        }

        public IChatBaseComponent getDisplayName() {
            return IChatBaseComponent.translatable("team.collision." + this.name);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
