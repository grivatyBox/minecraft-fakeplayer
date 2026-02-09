package net.minecraft.world.scores;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.level.saveddata.PersistentBase;

public class PersistentScoreboard extends PersistentBase {

    public static final String FILE_ID = "scoreboard";
    private final Scoreboard scoreboard;

    public PersistentScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void loadFrom(PersistentScoreboard.a persistentscoreboard_a) {
        List list = persistentscoreboard_a.objectives();
        Scoreboard scoreboard = this.scoreboard;

        Objects.requireNonNull(this.scoreboard);
        list.forEach(scoreboard::loadObjective);
        list = persistentscoreboard_a.scores();
        scoreboard = this.scoreboard;
        Objects.requireNonNull(this.scoreboard);
        list.forEach(scoreboard::loadPlayerScore);
        persistentscoreboard_a.displaySlots().forEach((displayslot, s) -> {
            ScoreboardObjective scoreboardobjective = this.scoreboard.getObjective(s);

            this.scoreboard.setDisplayObjective(displayslot, scoreboardobjective);
        });
        list = persistentscoreboard_a.teams();
        scoreboard = this.scoreboard;
        Objects.requireNonNull(this.scoreboard);
        list.forEach(scoreboard::loadPlayerTeam);
    }

    public PersistentScoreboard.a pack() {
        Map<DisplaySlot, String> map = new EnumMap(DisplaySlot.class);

        for (DisplaySlot displayslot : DisplaySlot.values()) {
            ScoreboardObjective scoreboardobjective = this.scoreboard.getDisplayObjective(displayslot);

            if (scoreboardobjective != null) {
                map.put(displayslot, scoreboardobjective.getName());
            }
        }

        return new PersistentScoreboard.a(this.scoreboard.getObjectives().stream().map(ScoreboardObjective::pack).toList(), this.scoreboard.packPlayerScores(), map, this.scoreboard.getPlayerTeams().stream().map(ScoreboardTeam::pack).toList());
    }

    public static record a(List<ScoreboardObjective.a> objectives, List<Scoreboard.a> scores, Map<DisplaySlot, String> displaySlots, List<ScoreboardTeam.a> teams) {

        public static final Codec<PersistentScoreboard.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ScoreboardObjective.a.CODEC.listOf().optionalFieldOf("Objectives", List.of()).forGetter(PersistentScoreboard.a::objectives), Scoreboard.a.CODEC.listOf().optionalFieldOf("PlayerScores", List.of()).forGetter(PersistentScoreboard.a::scores), Codec.unboundedMap(DisplaySlot.CODEC, Codec.STRING).optionalFieldOf("DisplaySlots", Map.of()).forGetter(PersistentScoreboard.a::displaySlots), ScoreboardTeam.a.CODEC.listOf().optionalFieldOf("Teams", List.of()).forGetter(PersistentScoreboard.a::teams)).apply(instance, PersistentScoreboard.a::new);
        });
    }
}
