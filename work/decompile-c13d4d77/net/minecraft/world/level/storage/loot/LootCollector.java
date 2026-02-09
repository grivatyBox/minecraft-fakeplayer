package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;

public class LootCollector {

    private final ProblemReporter reporter;
    private final LootContextParameterSet params;
    private final LootDataResolver resolver;
    private final Set<LootDataId<?>> visitedElements;

    public LootCollector(ProblemReporter problemreporter, LootContextParameterSet lootcontextparameterset, LootDataResolver lootdataresolver) {
        this(problemreporter, lootcontextparameterset, lootdataresolver, Set.of());
    }

    private LootCollector(ProblemReporter problemreporter, LootContextParameterSet lootcontextparameterset, LootDataResolver lootdataresolver, Set<LootDataId<?>> set) {
        this.reporter = problemreporter;
        this.params = lootcontextparameterset;
        this.resolver = lootdataresolver;
        this.visitedElements = set;
    }

    public LootCollector forChild(String s) {
        return new LootCollector(this.reporter.forChild(s), this.params, this.resolver, this.visitedElements);
    }

    public LootCollector enterElement(String s, LootDataId<?> lootdataid) {
        ImmutableSet<LootDataId<?>> immutableset = ImmutableSet.builder().addAll(this.visitedElements).add(lootdataid).build();

        return new LootCollector(this.reporter.forChild(s), this.params, this.resolver, immutableset);
    }

    public boolean hasVisitedElement(LootDataId<?> lootdataid) {
        return this.visitedElements.contains(lootdataid);
    }

    public void reportProblem(String s) {
        this.reporter.report(s);
    }

    public void validateUser(LootItemUser lootitemuser) {
        this.params.validateUser(this, lootitemuser);
    }

    public LootDataResolver resolver() {
        return this.resolver;
    }

    public LootCollector setParams(LootContextParameterSet lootcontextparameterset) {
        return new LootCollector(this.reporter, lootcontextparameterset, this.resolver, this.visitedElements);
    }
}
