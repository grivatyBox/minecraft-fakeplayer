package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.slf4j.Logger;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class RecipeBookServer extends RecipeBook {

    public static final String RECIPE_BOOK_TAG = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RecipeBookServer.a displayResolver;
    @VisibleForTesting
    public final Set<ResourceKey<IRecipe<?>>> known = Sets.newIdentityHashSet();
    @VisibleForTesting
    protected final Set<ResourceKey<IRecipe<?>>> highlight = Sets.newIdentityHashSet();

    public RecipeBookServer(RecipeBookServer.a recipebookserver_a) {
        this.displayResolver = recipebookserver_a;
    }

    public void add(ResourceKey<IRecipe<?>> resourcekey) {
        this.known.add(resourcekey);
    }

    public boolean contains(ResourceKey<IRecipe<?>> resourcekey) {
        return this.known.contains(resourcekey);
    }

    public void remove(ResourceKey<IRecipe<?>> resourcekey) {
        this.known.remove(resourcekey);
        this.highlight.remove(resourcekey);
    }

    public void removeHighlight(ResourceKey<IRecipe<?>> resourcekey) {
        this.highlight.remove(resourcekey);
    }

    private void addHighlight(ResourceKey<IRecipe<?>> resourcekey) {
        this.highlight.add(resourcekey);
    }

    public int addRecipes(Collection<RecipeHolder<?>> collection, EntityPlayer entityplayer) {
        List<ClientboundRecipeBookAddPacket.a> list = new ArrayList();

        for (RecipeHolder<?> recipeholder : collection) {
            ResourceKey<IRecipe<?>> resourcekey = recipeholder.id();

            if (!this.known.contains(resourcekey) && !recipeholder.value().isSpecial() && CraftEventFactory.handlePlayerRecipeListUpdateEvent(entityplayer, resourcekey.location())) { // CraftBukkit
                this.add(resourcekey);
                this.addHighlight(resourcekey);
                this.displayResolver.displaysForRecipe(resourcekey, (recipedisplayentry) -> {
                    list.add(new ClientboundRecipeBookAddPacket.a(recipedisplayentry, recipeholder.value().showNotification(), true));
                });
                CriterionTriggers.RECIPE_UNLOCKED.trigger(entityplayer, recipeholder);
            }
        }

        if (!list.isEmpty() && entityplayer.connection != null) { // SPIGOT-4478 during PlayerLoginEvent
            entityplayer.connection.send(new ClientboundRecipeBookAddPacket(list, false));
        }

        return list.size();
    }

    public int removeRecipes(Collection<RecipeHolder<?>> collection, EntityPlayer entityplayer) {
        List<RecipeDisplayId> list = Lists.newArrayList();

        for (RecipeHolder<?> recipeholder : collection) {
            ResourceKey<IRecipe<?>> resourcekey = recipeholder.id();

            if (this.known.contains(resourcekey)) {
                this.remove(resourcekey);
                this.displayResolver.displaysForRecipe(resourcekey, (recipedisplayentry) -> {
                    list.add(recipedisplayentry.id());
                });
            }
        }

        if (!list.isEmpty() && entityplayer.connection != null) { // SPIGOT-4478 during PlayerLoginEvent
            entityplayer.connection.send(new ClientboundRecipeBookRemovePacket(list));
        }

        return list.size();
    }

    private void loadRecipes(List<ResourceKey<IRecipe<?>>> list, Consumer<ResourceKey<IRecipe<?>>> consumer, Predicate<ResourceKey<IRecipe<?>>> predicate) {
        for (ResourceKey<IRecipe<?>> resourcekey : list) {
            if (!predicate.test(resourcekey)) {
                RecipeBookServer.LOGGER.error("Tried to load unrecognized recipe: {} removed now.", resourcekey);
            } else {
                consumer.accept(resourcekey);
            }
        }

    }

    public void sendInitialRecipeBook(EntityPlayer entityplayer) {
        entityplayer.connection.send(new ClientboundRecipeBookSettingsPacket(this.getBookSettings().copy()));
        List<ClientboundRecipeBookAddPacket.a> list = new ArrayList(this.known.size());

        for (ResourceKey<IRecipe<?>> resourcekey : this.known) {
            this.displayResolver.displaysForRecipe(resourcekey, (recipedisplayentry) -> {
                list.add(new ClientboundRecipeBookAddPacket.a(recipedisplayentry, false, this.highlight.contains(resourcekey)));
            });
        }

        entityplayer.connection.send(new ClientboundRecipeBookAddPacket(list, true));
    }

    public void copyOverData(RecipeBookServer recipebookserver) {
        this.apply(recipebookserver.pack());
    }

    public RecipeBookServer.b pack() {
        return new RecipeBookServer.b(this.bookSettings.copy(), List.copyOf(this.known), List.copyOf(this.highlight));
    }

    private void apply(RecipeBookServer.b recipebookserver_b) {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom(recipebookserver_b.settings);
        this.known.addAll(recipebookserver_b.known);
        this.highlight.addAll(recipebookserver_b.highlight);
    }

    public void loadUntrusted(RecipeBookServer.b recipebookserver_b, Predicate<ResourceKey<IRecipe<?>>> predicate) {
        this.bookSettings.replaceFrom(recipebookserver_b.settings);
        List list = recipebookserver_b.known;
        Set set = this.known;

        Objects.requireNonNull(this.known);
        this.loadRecipes(list, set::add, predicate);
        list = recipebookserver_b.highlight;
        set = this.highlight;
        Objects.requireNonNull(this.highlight);
        this.loadRecipes(list, set::add, predicate);
    }

    public static record b(RecipeBookSettings settings, List<ResourceKey<IRecipe<?>>> known, List<ResourceKey<IRecipe<?>>> highlight) {

        public static final Codec<RecipeBookServer.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(RecipeBookSettings.MAP_CODEC.forGetter(RecipeBookServer.b::settings), IRecipe.KEY_CODEC.listOf().fieldOf("recipes").forGetter(RecipeBookServer.b::known), IRecipe.KEY_CODEC.listOf().fieldOf("toBeDisplayed").forGetter(RecipeBookServer.b::highlight)).apply(instance, RecipeBookServer.b::new);
        });
    }

    @FunctionalInterface
    public interface a {

        void displaysForRecipe(ResourceKey<IRecipe<?>> resourcekey, Consumer<RecipeDisplayEntry> consumer);
    }
}
