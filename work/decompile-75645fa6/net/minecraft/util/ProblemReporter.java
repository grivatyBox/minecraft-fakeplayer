package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface ProblemReporter {

    ProblemReporter DISCARDING = new ProblemReporter() {
        @Override
        public ProblemReporter forChild(ProblemReporter.f problemreporter_f) {
            return this;
        }

        @Override
        public void report(ProblemReporter.g problemreporter_g) {}
    };

    ProblemReporter forChild(ProblemReporter.f problemreporter_f);

    void report(ProblemReporter.g problemreporter_g);

    public static record i(String name) implements ProblemReporter.f {

        @Override
        public String get() {
            return this.name;
        }
    }

    public static record h(ResourceKey<?> id) implements ProblemReporter.f {

        @Override
        public String get() {
            String s = String.valueOf(this.id.location());

            return "{" + s + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public static record c(String name) implements ProblemReporter.f {

        @Override
        public String get() {
            return "." + this.name;
        }
    }

    public static record d(String name, int index) implements ProblemReporter.f {

        @Override
        public String get() {
            return "." + this.name + "[" + this.index + "]";
        }
    }

    public static record e(int index) implements ProblemReporter.f {

        @Override
        public String get() {
            return "[" + this.index + "]";
        }
    }

    public static record b(ResourceKey<?> id) implements ProblemReporter.f {

        @Override
        public String get() {
            String s = String.valueOf(this.id.location());

            return "->{" + s + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public static class a implements ProblemReporter {

        public static final ProblemReporter.f EMPTY_ROOT = () -> {
            return "";
        };
        @Nullable
        private final ProblemReporter.a parent;
        private final ProblemReporter.f element;
        private final Set<ProblemReporter.a.a> problems;

        public a() {
            this(ProblemReporter.a.EMPTY_ROOT);
        }

        public a(ProblemReporter.f problemreporter_f) {
            this.parent = null;
            this.problems = new LinkedHashSet();
            this.element = problemreporter_f;
        }

        private a(ProblemReporter.a problemreporter_a, ProblemReporter.f problemreporter_f) {
            this.problems = problemreporter_a.problems;
            this.parent = problemreporter_a;
            this.element = problemreporter_f;
        }

        @Override
        public ProblemReporter forChild(ProblemReporter.f problemreporter_f) {
            return new ProblemReporter.a(this, problemreporter_f);
        }

        @Override
        public void report(ProblemReporter.g problemreporter_g) {
            this.problems.add(new ProblemReporter.a.a(this, problemreporter_g));
        }

        public boolean isEmpty() {
            return this.problems.isEmpty();
        }

        public void forEach(BiConsumer<String, ProblemReporter.g> biconsumer) {
            List<ProblemReporter.f> list = new ArrayList();
            StringBuilder stringbuilder = new StringBuilder();

            for (ProblemReporter.a.a problemreporter_a_a : this.problems) {
                for (ProblemReporter.a problemreporter_a = problemreporter_a_a.source; problemreporter_a != null; problemreporter_a = problemreporter_a.parent) {
                    list.add(problemreporter_a.element);
                }

                for (int i = list.size() - 1; i >= 0; --i) {
                    stringbuilder.append(((ProblemReporter.f) list.get(i)).get());
                }

                biconsumer.accept(stringbuilder.toString(), problemreporter_a_a.problem());
                stringbuilder.setLength(0);
                list.clear();
            }

        }

        public String getReport() {
            Multimap<String, ProblemReporter.g> multimap = HashMultimap.create();

            Objects.requireNonNull(multimap);
            this.forEach(multimap::put);
            return (String) multimap.asMap().entrySet().stream().map((entry) -> {
                String s = (String) entry.getKey();

                return " at " + s + ": " + (String) ((Collection) entry.getValue()).stream().map(ProblemReporter.g::description).collect(Collectors.joining("; "));
            }).collect(Collectors.joining("\n"));
        }

        public String getTreeReport() {
            List<ProblemReporter.f> list = new ArrayList();
            ProblemReporter.a.b problemreporter_a_b = new ProblemReporter.a.b(this.element);

            for (ProblemReporter.a.a problemreporter_a_a : this.problems) {
                for (ProblemReporter.a problemreporter_a = problemreporter_a_a.source; problemreporter_a != this; problemreporter_a = problemreporter_a.parent) {
                    list.add(problemreporter_a.element);
                }

                ProblemReporter.a.b problemreporter_a_b1 = problemreporter_a_b;

                for (int i = list.size() - 1; i >= 0; --i) {
                    problemreporter_a_b1 = problemreporter_a_b1.child((ProblemReporter.f) list.get(i));
                }

                list.clear();
                problemreporter_a_b1.problems.add(problemreporter_a_a.problem);
            }

            return String.join("\n", problemreporter_a_b.getLines());
        }

        private static record a(ProblemReporter.a source, ProblemReporter.g problem) {

        }

        private static record b(ProblemReporter.f element, List<ProblemReporter.g> problems, Map<ProblemReporter.f, ProblemReporter.a.b> children) {

            public b(ProblemReporter.f problemreporter_f) {
                this(problemreporter_f, new ArrayList(), new LinkedHashMap());
            }

            public ProblemReporter.a.b child(ProblemReporter.f problemreporter_f) {
                return (ProblemReporter.a.b) this.children.computeIfAbsent(problemreporter_f, ProblemReporter.a.b::new);
            }

            public List<String> getLines() {
                int i = this.problems.size();
                int j = this.children.size();

                if (i == 0 && j == 0) {
                    return List.of();
                } else if (i == 0 && j == 1) {
                    List<String> list = new ArrayList();

                    this.children.forEach((problemreporter_f, problemreporter_a_b) -> {
                        list.addAll(problemreporter_a_b.getLines());
                    });
                    String s = this.element.get();

                    list.set(0, s + (String) list.get(0));
                    return list;
                } else if (i == 1 && j == 0) {
                    String s1 = this.element.get();

                    return List.of(s1 + ": " + ((ProblemReporter.g) this.problems.getFirst()).description());
                } else {
                    List<String> list1 = new ArrayList();

                    this.children.forEach((problemreporter_f, problemreporter_a_b) -> {
                        list1.addAll(problemreporter_a_b.getLines());
                    });
                    list1.replaceAll((s2) -> {
                        return "  " + s2;
                    });

                    for (ProblemReporter.g problemreporter_g : this.problems) {
                        list1.add("  " + problemreporter_g.description());
                    }

                    list1.addFirst(this.element.get() + ":");
                    return list1;
                }
            }
        }
    }

    public static class j extends ProblemReporter.a implements AutoCloseable {

        private final Logger logger;

        public j(Logger logger) {
            this.logger = logger;
        }

        public j(ProblemReporter.f problemreporter_f, Logger logger) {
            super(problemreporter_f);
            this.logger = logger;
        }

        public void close() {
            if (!this.isEmpty()) {
                this.logger.warn("[{}] Serialization errors:\n{}", this.logger.getName(), this.getTreeReport());
            }

        }
    }

    @FunctionalInterface
    public interface f {

        String get();
    }

    public interface g {

        String description();
    }
}
