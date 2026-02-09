package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;

public class LootCollector {

    private final ProblemReporter reporter;
    private final ContextKeySet contextKeySet;
    private final Optional<HolderGetter.a> resolver;
    private final Set<ResourceKey<?>> visitedElements;

    public LootCollector(ProblemReporter problemreporter, ContextKeySet contextkeyset, HolderGetter.a holdergetter_a) {
        this(problemreporter, contextkeyset, Optional.of(holdergetter_a), Set.of());
    }

    public LootCollector(ProblemReporter problemreporter, ContextKeySet contextkeyset) {
        this(problemreporter, contextkeyset, Optional.empty(), Set.of());
    }

    private LootCollector(ProblemReporter problemreporter, ContextKeySet contextkeyset, Optional<HolderGetter.a> optional, Set<ResourceKey<?>> set) {
        this.reporter = problemreporter;
        this.contextKeySet = contextkeyset;
        this.resolver = optional;
        this.visitedElements = set;
    }

    public LootCollector forChild(ProblemReporter.f problemreporter_f) {
        return new LootCollector(this.reporter.forChild(problemreporter_f), this.contextKeySet, this.resolver, this.visitedElements);
    }

    public LootCollector enterElement(ProblemReporter.f problemreporter_f, ResourceKey<?> resourcekey) {
        Set<ResourceKey<?>> set = ImmutableSet.builder().addAll(this.visitedElements).add(resourcekey).build();

        return new LootCollector(this.reporter.forChild(problemreporter_f), this.contextKeySet, this.resolver, set);
    }

    public boolean hasVisitedElement(ResourceKey<?> resourcekey) {
        return this.visitedElements.contains(resourcekey);
    }

    public void reportProblem(ProblemReporter.g problemreporter_g) {
        this.reporter.report(problemreporter_g);
    }

    public void validateContextUsage(LootItemUser lootitemuser) {
        Set<ContextKey<?>> set = lootitemuser.getReferencedContextParams();
        Set<ContextKey<?>> set1 = Sets.difference(set, this.contextKeySet.allowed());

        if (!set1.isEmpty()) {
            this.reporter.report(new LootCollector.b(set1));
        }

    }

    public HolderGetter.a resolver() {
        return (HolderGetter.a) this.resolver.orElseThrow(() -> {
            return new UnsupportedOperationException("References not allowed");
        });
    }

    public boolean allowsReferences() {
        return this.resolver.isPresent();
    }

    public LootCollector setContextKeySet(ContextKeySet contextkeyset) {
        return new LootCollector(this.reporter, contextkeyset, this.resolver, this.visitedElements);
    }

    public ProblemReporter reporter() {
        return this.reporter;
    }

    public static record b(Set<ContextKey<?>> notProvided) implements ProblemReporter.g {

        @Override
        public String description() {
            return "Parameters " + String.valueOf(this.notProvided) + " are not provided in this context";
        }
    }

    public static record d(ResourceKey<?> referenced) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.referenced.location());

            return "Reference to " + s + " of type " + String.valueOf(this.referenced.registry()) + " was used, but references are not allowed";
        }
    }

    public static record c(ResourceKey<?> referenced) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.referenced.location());

            return s + " of type " + String.valueOf(this.referenced.registry()) + " is recursively called";
        }
    }

    public static record a(ResourceKey<?> referenced) implements ProblemReporter.g {

        @Override
        public String description() {
            String s = String.valueOf(this.referenced.location());

            return "Missing element " + s + " of type " + String.valueOf(this.referenced.registry());
        }
    }
}
