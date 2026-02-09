package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;

public record LootItemConditionEntityScore(Map<String, IntRange> scores, LootTableInfo.EntityTarget entityTarget) implements LootItemCondition {

    public static final MapCodec<LootItemConditionEntityScore> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.unboundedMap(Codec.STRING, IntRange.CODEC).fieldOf("scores").forGetter(LootItemConditionEntityScore::scores), LootTableInfo.EntityTarget.CODEC.fieldOf("entity").forGetter(LootItemConditionEntityScore::entityTarget)).apply(instance, LootItemConditionEntityScore::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_SCORES;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return (Set) Stream.concat(Stream.of(this.entityTarget.getParam()), this.scores.values().stream().flatMap((intrange) -> {
            return intrange.getReferencedContextParams().stream();
        })).collect(ImmutableSet.toImmutableSet());
    }

    public boolean test(LootTableInfo loottableinfo) {
        Entity entity = (Entity) loottableinfo.getOptionalParameter(this.entityTarget.getParam());

        if (entity == null) {
            return false;
        } else {
            Scoreboard scoreboard = loottableinfo.getLevel().getScoreboard();

            for (Map.Entry<String, IntRange> map_entry : this.scores.entrySet()) {
                if (!this.hasScore(loottableinfo, entity, scoreboard, (String) map_entry.getKey(), (IntRange) map_entry.getValue())) {
                    return false;
                }
            }

            return true;
        }
    }

    protected boolean hasScore(LootTableInfo loottableinfo, Entity entity, Scoreboard scoreboard, String s, IntRange intrange) {
        ScoreboardObjective scoreboardobjective = scoreboard.getObjective(s);

        if (scoreboardobjective == null) {
            return false;
        } else {
            ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(entity, scoreboardobjective);

            return readonlyscoreinfo == null ? false : intrange.test(loottableinfo, readonlyscoreinfo.value());
        }
    }

    public static LootItemConditionEntityScore.a hasScores(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new LootItemConditionEntityScore.a(loottableinfo_entitytarget);
    }

    public static class a implements LootItemCondition.a {

        private final ImmutableMap.Builder<String, IntRange> scores = ImmutableMap.builder();
        private final LootTableInfo.EntityTarget entityTarget;

        public a(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
            this.entityTarget = loottableinfo_entitytarget;
        }

        public LootItemConditionEntityScore.a withScore(String s, IntRange intrange) {
            this.scores.put(s, intrange);
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new LootItemConditionEntityScore(this.scores.build(), this.entityTarget);
        }
    }
}
