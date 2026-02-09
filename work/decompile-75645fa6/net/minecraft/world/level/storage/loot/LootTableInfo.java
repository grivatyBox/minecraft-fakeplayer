package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableInfo {

    private final LootParams params;
    private final RandomSource random;
    private final HolderGetter.a lootDataResolver;
    private final Set<LootTableInfo.e<?>> visitedElements = Sets.newLinkedHashSet();

    LootTableInfo(LootParams lootparams, RandomSource randomsource, HolderGetter.a holdergetter_a) {
        this.params = lootparams;
        this.random = randomsource;
        this.lootDataResolver = holdergetter_a;
    }

    public boolean hasParameter(ContextKey<?> contextkey) {
        return this.params.contextMap().has(contextkey);
    }

    public <T> T getParameter(ContextKey<T> contextkey) {
        return (T) this.params.contextMap().getOrThrow(contextkey);
    }

    @Nullable
    public <T> T getOptionalParameter(ContextKey<T> contextkey) {
        return (T) this.params.contextMap().getOptional(contextkey);
    }

    public void addDynamicDrops(MinecraftKey minecraftkey, Consumer<ItemStack> consumer) {
        this.params.addDynamicDrops(minecraftkey, consumer);
    }

    public boolean hasVisitedElement(LootTableInfo.e<?> loottableinfo_e) {
        return this.visitedElements.contains(loottableinfo_e);
    }

    public boolean pushVisitedElement(LootTableInfo.e<?> loottableinfo_e) {
        return this.visitedElements.add(loottableinfo_e);
    }

    public void popVisitedElement(LootTableInfo.e<?> loottableinfo_e) {
        this.visitedElements.remove(loottableinfo_e);
    }

    public HolderGetter.a getResolver() {
        return this.lootDataResolver;
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.params.getLuck();
    }

    public WorldServer getLevel() {
        return this.params.getLevel();
    }

    public static LootTableInfo.e<LootTable> createVisitedEntry(LootTable loottable) {
        return new LootTableInfo.e<LootTable>(LootDataType.TABLE, loottable);
    }

    public static LootTableInfo.e<LootItemCondition> createVisitedEntry(LootItemCondition lootitemcondition) {
        return new LootTableInfo.e<LootItemCondition>(LootDataType.PREDICATE, lootitemcondition);
    }

    public static LootTableInfo.e<LootItemFunction> createVisitedEntry(LootItemFunction lootitemfunction) {
        return new LootTableInfo.e<LootItemFunction>(LootDataType.MODIFIER, lootitemfunction);
    }

    public static class Builder {

        private final LootParams params;
        @Nullable
        private RandomSource random;

        public Builder(LootParams lootparams) {
            this.params = lootparams;
        }

        public LootTableInfo.Builder withOptionalRandomSeed(long i) {
            if (i != 0L) {
                this.random = RandomSource.create(i);
            }

            return this;
        }

        public LootTableInfo.Builder withOptionalRandomSource(RandomSource randomsource) {
            this.random = randomsource;
            return this;
        }

        public WorldServer getLevel() {
            return this.params.getLevel();
        }

        public LootTableInfo create(Optional<MinecraftKey> optional) {
            WorldServer worldserver = this.getLevel();
            MinecraftServer minecraftserver = worldserver.getServer();
            Optional optional1 = Optional.ofNullable(this.random).or(() -> {
                Objects.requireNonNull(worldserver);
                return optional.map(worldserver::getRandomSequence);
            });

            Objects.requireNonNull(worldserver);
            RandomSource randomsource = (RandomSource) optional1.orElseGet(worldserver::getRandom);

            return new LootTableInfo(this.params, randomsource, minecraftserver.reloadableRegistries().lookup());
        }
    }

    public static enum EntityTarget implements INamable {

        THIS("this", LootContextParameters.THIS_ENTITY), ATTACKER("attacker", LootContextParameters.ATTACKING_ENTITY), DIRECT_ATTACKER("direct_attacker", LootContextParameters.DIRECT_ATTACKING_ENTITY), ATTACKING_PLAYER("attacking_player", LootContextParameters.LAST_DAMAGE_PLAYER), TARGET_ENTITY("target_entity", LootContextParameters.TARGET_ENTITY), INTERACTING_ENTITY("interacting_entity", LootContextParameters.INTERACTING_ENTITY);

        public static final INamable.a<LootTableInfo.EntityTarget> CODEC = INamable.<LootTableInfo.EntityTarget>fromEnum(LootTableInfo.EntityTarget::values);
        private final String name;
        private final ContextKey<? extends Entity> param;

        private EntityTarget(final String s, final ContextKey contextkey) {
            this.name = s;
            this.param = contextkey;
        }

        public ContextKey<? extends Entity> getParam() {
            return this.param;
        }

        public static LootTableInfo.EntityTarget getByName(String s) {
            LootTableInfo.EntityTarget loottableinfo_entitytarget = LootTableInfo.EntityTarget.CODEC.byName(s);

            if (loottableinfo_entitytarget != null) {
                return loottableinfo_entitytarget;
            } else {
                throw new IllegalArgumentException("Invalid entity target " + s);
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static enum a implements INamable {

        BLOCK_ENTITY("block_entity", LootContextParameters.BLOCK_ENTITY);

        private final String name;
        private final ContextKey<? extends TileEntity> param;

        private a(final String s, final ContextKey contextkey) {
            this.name = s;
            this.param = contextkey;
        }

        public ContextKey<? extends TileEntity> getParam() {
            return this.param;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static enum d implements INamable {

        TOOL("tool", LootContextParameters.TOOL);

        private final String name;
        private final ContextKey<? extends ItemStack> param;

        private d(final String s, final ContextKey contextkey) {
            this.name = s;
            this.param = contextkey;
        }

        public ContextKey<? extends ItemStack> getParam() {
            return this.param;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static record e<T>(LootDataType<T> type, T value) {

    }
}
