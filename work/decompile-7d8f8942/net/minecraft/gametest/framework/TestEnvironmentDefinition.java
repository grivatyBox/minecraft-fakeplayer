package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public interface TestEnvironmentDefinition {

    Codec<TestEnvironmentDefinition> DIRECT_CODEC = BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE.byNameCodec().dispatch(TestEnvironmentDefinition::codec, (mapcodec) -> {
        return mapcodec;
    });
    Codec<Holder<TestEnvironmentDefinition>> CODEC = RegistryFileCodec.<Holder<TestEnvironmentDefinition>>create(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC);

    static MapCodec<? extends TestEnvironmentDefinition> bootstrap(IRegistry<MapCodec<? extends TestEnvironmentDefinition>> iregistry) {
        IRegistry.register(iregistry, "all_of", TestEnvironmentDefinition.a.CODEC);
        IRegistry.register(iregistry, "game_rules", TestEnvironmentDefinition.c.CODEC);
        IRegistry.register(iregistry, "time_of_day", TestEnvironmentDefinition.d.CODEC);
        IRegistry.register(iregistry, "weather", TestEnvironmentDefinition.e.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "function", TestEnvironmentDefinition.b.CODEC);
    }

    void setup(WorldServer worldserver);

    default void teardown(WorldServer worldserver) {}

    MapCodec<? extends TestEnvironmentDefinition> codec();

    public static record e(TestEnvironmentDefinition.e.a weather) implements TestEnvironmentDefinition {

        public static final MapCodec<TestEnvironmentDefinition.e> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(TestEnvironmentDefinition.e.a.CODEC.fieldOf("weather").forGetter(TestEnvironmentDefinition.e::weather)).apply(instance, TestEnvironmentDefinition.e::new);
        });

        @Override
        public void setup(WorldServer worldserver) {
            this.weather.apply(worldserver);
        }

        @Override
        public void teardown(WorldServer worldserver) {
            worldserver.resetWeatherCycle();
        }

        @Override
        public MapCodec<TestEnvironmentDefinition.e> codec() {
            return TestEnvironmentDefinition.e.CODEC;
        }

        public static enum a implements INamable {

            CLEAR("clear", 100000, 0, false, false), RAIN("rain", 0, 100000, true, false), THUNDER("thunder", 0, 100000, true, true);

            public static final Codec<TestEnvironmentDefinition.e.a> CODEC = INamable.<TestEnvironmentDefinition.e.a>fromEnum(TestEnvironmentDefinition.e.a::values);
            private final String id;
            private final int clearTime;
            private final int rainTime;
            private final boolean raining;
            private final boolean thundering;

            private a(final String s, final int i, final int j, final boolean flag, final boolean flag1) {
                this.id = s;
                this.clearTime = i;
                this.rainTime = j;
                this.raining = flag;
                this.thundering = flag1;
            }

            void apply(WorldServer worldserver) {
                worldserver.setWeatherParameters(this.clearTime, this.rainTime, this.raining, this.thundering);
            }

            @Override
            public String getSerializedName() {
                return this.id;
            }
        }
    }

    public static record d(int time) implements TestEnvironmentDefinition {

        public static final MapCodec<TestEnvironmentDefinition.d> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("time").forGetter(TestEnvironmentDefinition.d::time)).apply(instance, TestEnvironmentDefinition.d::new);
        });

        @Override
        public void setup(WorldServer worldserver) {
            worldserver.setDayTime((long) this.time);
        }

        @Override
        public MapCodec<TestEnvironmentDefinition.d> codec() {
            return TestEnvironmentDefinition.d.CODEC;
        }
    }

    public static record c(List<TestEnvironmentDefinition.c.a<Boolean, GameRules.GameRuleBoolean>> boolRules, List<TestEnvironmentDefinition.c.a<Integer, GameRules.GameRuleInt>> intRules) implements TestEnvironmentDefinition {

        public static final MapCodec<TestEnvironmentDefinition.c> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(TestEnvironmentDefinition.c.a.codec(GameRules.GameRuleBoolean.class, Codec.BOOL).listOf().fieldOf("bool_rules").forGetter(TestEnvironmentDefinition.c::boolRules), TestEnvironmentDefinition.c.a.codec(GameRules.GameRuleInt.class, Codec.INT).listOf().fieldOf("int_rules").forGetter(TestEnvironmentDefinition.c::intRules)).apply(instance, TestEnvironmentDefinition.c::new);
        });

        @Override
        public void setup(WorldServer worldserver) {
            GameRules gamerules = worldserver.getGameRules();
            MinecraftServer minecraftserver = worldserver.getServer();

            for (TestEnvironmentDefinition.c.a<Boolean, GameRules.GameRuleBoolean> testenvironmentdefinition_c_a : this.boolRules) {
                ((GameRules.GameRuleBoolean) gamerules.getRule(testenvironmentdefinition_c_a.key())).set((Boolean) testenvironmentdefinition_c_a.value(), minecraftserver);
            }

            for (TestEnvironmentDefinition.c.a<Integer, GameRules.GameRuleInt> testenvironmentdefinition_c_a1 : this.intRules) {
                ((GameRules.GameRuleInt) gamerules.getRule(testenvironmentdefinition_c_a1.key())).set((Integer) testenvironmentdefinition_c_a1.value(), minecraftserver);
            }

        }

        @Override
        public void teardown(WorldServer worldserver) {
            GameRules gamerules = worldserver.getGameRules();
            MinecraftServer minecraftserver = worldserver.getServer();

            for (TestEnvironmentDefinition.c.a<Boolean, GameRules.GameRuleBoolean> testenvironmentdefinition_c_a : this.boolRules) {
                ((GameRules.GameRuleBoolean) gamerules.getRule(testenvironmentdefinition_c_a.key())).setFrom((GameRules.GameRuleBoolean) GameRules.getType(testenvironmentdefinition_c_a.key()).createRule(), minecraftserver);
            }

            for (TestEnvironmentDefinition.c.a<Integer, GameRules.GameRuleInt> testenvironmentdefinition_c_a1 : this.intRules) {
                ((GameRules.GameRuleInt) gamerules.getRule(testenvironmentdefinition_c_a1.key())).setFrom((GameRules.GameRuleInt) GameRules.getType(testenvironmentdefinition_c_a1.key()).createRule(), minecraftserver);
            }

        }

        @Override
        public MapCodec<TestEnvironmentDefinition.c> codec() {
            return TestEnvironmentDefinition.c.CODEC;
        }

        public static <S, T extends GameRules.GameRuleValue<T>> TestEnvironmentDefinition.c.a<S, T> entry(GameRules.GameRuleKey<T> gamerules_gamerulekey, S s0) {
            return new TestEnvironmentDefinition.c.a<S, T>(gamerules_gamerulekey, s0);
        }

        public static record a<S, T extends GameRules.GameRuleValue<T>>(GameRules.GameRuleKey<T> key, S value) {

            public static <S, T extends GameRules.GameRuleValue<T>> Codec<TestEnvironmentDefinition.c.a<S, T>> codec(Class<T> oclass, Codec<S> codec) {
                return RecordCodecBuilder.create((instance) -> {
                    return instance.group(GameRules.keyCodec(oclass).fieldOf("rule").forGetter(TestEnvironmentDefinition.c.a::key), codec.fieldOf("value").forGetter(TestEnvironmentDefinition.c.a::value)).apply(instance, TestEnvironmentDefinition.c.a::new);
                });
            }
        }
    }

    public static record b(Optional<MinecraftKey> setupFunction, Optional<MinecraftKey> teardownFunction) implements TestEnvironmentDefinition {

        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<TestEnvironmentDefinition.b> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(MinecraftKey.CODEC.optionalFieldOf("setup").forGetter(TestEnvironmentDefinition.b::setupFunction), MinecraftKey.CODEC.optionalFieldOf("teardown").forGetter(TestEnvironmentDefinition.b::teardownFunction)).apply(instance, TestEnvironmentDefinition.b::new);
        });

        @Override
        public void setup(WorldServer worldserver) {
            this.setupFunction.ifPresent((minecraftkey) -> {
                run(worldserver, minecraftkey);
            });
        }

        @Override
        public void teardown(WorldServer worldserver) {
            this.teardownFunction.ifPresent((minecraftkey) -> {
                run(worldserver, minecraftkey);
            });
        }

        private static void run(WorldServer worldserver, MinecraftKey minecraftkey) {
            MinecraftServer minecraftserver = worldserver.getServer();
            CustomFunctionData customfunctiondata = minecraftserver.getFunctions();
            Optional<CommandFunction<CommandListenerWrapper>> optional = customfunctiondata.get(minecraftkey);

            if (optional.isPresent()) {
                CommandListenerWrapper commandlistenerwrapper = minecraftserver.createCommandSourceStack().withPermission(2).withSuppressedOutput().withLevel(worldserver);

                customfunctiondata.execute((CommandFunction) optional.get(), commandlistenerwrapper);
            } else {
                TestEnvironmentDefinition.b.LOGGER.error("Test Batch failed for non-existent function {}", minecraftkey);
            }

        }

        @Override
        public MapCodec<TestEnvironmentDefinition.b> codec() {
            return TestEnvironmentDefinition.b.CODEC;
        }
    }

    public static record a(List<Holder<TestEnvironmentDefinition>> definitions) implements TestEnvironmentDefinition {

        public static final MapCodec<TestEnvironmentDefinition.a> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(TestEnvironmentDefinition.CODEC.listOf().fieldOf("definitions").forGetter(TestEnvironmentDefinition.a::definitions)).apply(instance, TestEnvironmentDefinition.a::new);
        });

        public a(TestEnvironmentDefinition... atestenvironmentdefinition) {
            this(Arrays.stream(atestenvironmentdefinition).map(Holder::direct).toList());
        }

        @Override
        public void setup(WorldServer worldserver) {
            this.definitions.forEach((holder) -> {
                ((TestEnvironmentDefinition) holder.value()).setup(worldserver);
            });
        }

        @Override
        public void teardown(WorldServer worldserver) {
            this.definitions.forEach((holder) -> {
                ((TestEnvironmentDefinition) holder.value()).teardown(worldserver);
            });
        }

        @Override
        public MapCodec<TestEnvironmentDefinition.a> codec() {
            return TestEnvironmentDefinition.a.CODEC;
        }
    }
}
