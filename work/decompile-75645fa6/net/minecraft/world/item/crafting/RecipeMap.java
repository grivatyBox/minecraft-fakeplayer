package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.World;

public class RecipeMap {

    public static final RecipeMap EMPTY = new RecipeMap(ImmutableMultimap.of(), Map.of());
    public final Multimap<Recipes<?>, RecipeHolder<?>> byType;
    private final Map<ResourceKey<IRecipe<?>>, RecipeHolder<?>> byKey;

    private RecipeMap(Multimap<Recipes<?>, RecipeHolder<?>> multimap, Map<ResourceKey<IRecipe<?>>, RecipeHolder<?>> map) {
        this.byType = multimap;
        this.byKey = map;
    }

    public static RecipeMap create(Iterable<RecipeHolder<?>> iterable) {
        ImmutableMultimap.Builder<Recipes<?>, RecipeHolder<?>> immutablemultimap_builder = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<IRecipe<?>>, RecipeHolder<?>> immutablemap_builder = ImmutableMap.builder();

        for (RecipeHolder<?> recipeholder : iterable) {
            immutablemultimap_builder.put(recipeholder.value().getType(), recipeholder);
            immutablemap_builder.put(recipeholder.id(), recipeholder);
        }

        return new RecipeMap(immutablemultimap_builder.build(), immutablemap_builder.build());
    }

    public <I extends RecipeInput, T extends IRecipe<I>> Collection<RecipeHolder<T>> byType(Recipes<T> recipes) {
        return this.byType.get(recipes);
    }

    public Collection<RecipeHolder<?>> values() {
        return this.byKey.values();
    }

    @Nullable
    public RecipeHolder<?> byKey(ResourceKey<IRecipe<?>> resourcekey) {
        return (RecipeHolder) this.byKey.get(resourcekey);
    }

    public <I extends RecipeInput, T extends IRecipe<I>> Stream<RecipeHolder<T>> getRecipesFor(Recipes<T> recipes, I i0, World world) {
        return i0.isEmpty() ? Stream.empty() : this.byType(recipes).stream().filter((recipeholder) -> {
            return recipeholder.value().matches(i0, world);
        });
    }
}
