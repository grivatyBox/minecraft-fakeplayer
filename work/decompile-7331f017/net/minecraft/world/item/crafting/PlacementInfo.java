package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;

public class PlacementInfo {

    public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), List.of(), List.of());
    private final List<RecipeItemStack> ingredients;
    private final List<AutoRecipeStackManager.a<Holder<Item>>> unpackedIngredients;
    private final List<Optional<PlacementInfo.a>> slotInfo;

    private PlacementInfo(List<RecipeItemStack> list, List<AutoRecipeStackManager.a<Holder<Item>>> list1, List<Optional<PlacementInfo.a>> list2) {
        this.ingredients = list;
        this.unpackedIngredients = list1;
        this.slotInfo = list2;
    }

    public static AutoRecipeStackManager.a<Holder<Item>> ingredientToContents(RecipeItemStack recipeitemstack) {
        return StackedItemContents.convertIngredientContents(recipeitemstack.items().stream());
    }

    public static PlacementInfo create(RecipeItemStack recipeitemstack) {
        if (recipeitemstack.items().isEmpty()) {
            return PlacementInfo.NOT_PLACEABLE;
        } else {
            AutoRecipeStackManager.a<Holder<Item>> autorecipestackmanager_a = ingredientToContents(recipeitemstack);
            PlacementInfo.a placementinfo_a = new PlacementInfo.a(0);

            return new PlacementInfo(List.of(recipeitemstack), List.of(autorecipestackmanager_a), List.of(Optional.of(placementinfo_a)));
        }
    }

    public static PlacementInfo createFromOptionals(List<Optional<RecipeItemStack>> list) {
        int i = list.size();
        List<RecipeItemStack> list1 = new ArrayList(i);
        List<AutoRecipeStackManager.a<Holder<Item>>> list2 = new ArrayList(i);
        List<Optional<PlacementInfo.a>> list3 = new ArrayList(i);
        int j = 0;
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Optional<RecipeItemStack> optional = (Optional) iterator.next();

            if (optional.isPresent()) {
                RecipeItemStack recipeitemstack = (RecipeItemStack) optional.get();

                if (recipeitemstack.items().isEmpty()) {
                    return PlacementInfo.NOT_PLACEABLE;
                }

                list1.add(recipeitemstack);
                list2.add(ingredientToContents(recipeitemstack));
                list3.add(Optional.of(new PlacementInfo.a(j++)));
            } else {
                list3.add(Optional.empty());
            }
        }

        return new PlacementInfo(list1, list2, list3);
    }

    public static PlacementInfo create(List<RecipeItemStack> list) {
        int i = list.size();
        List<AutoRecipeStackManager.a<Holder<Item>>> list1 = new ArrayList(i);
        List<Optional<PlacementInfo.a>> list2 = new ArrayList(i);

        for (int j = 0; j < i; ++j) {
            RecipeItemStack recipeitemstack = (RecipeItemStack) list.get(j);

            if (recipeitemstack.items().isEmpty()) {
                return PlacementInfo.NOT_PLACEABLE;
            }

            list1.add(ingredientToContents(recipeitemstack));
            list2.add(Optional.of(new PlacementInfo.a(j)));
        }

        return new PlacementInfo(list, list1, list2);
    }

    public List<Optional<PlacementInfo.a>> slotInfo() {
        return this.slotInfo;
    }

    public List<RecipeItemStack> ingredients() {
        return this.ingredients;
    }

    public List<AutoRecipeStackManager.a<Holder<Item>>> unpackedIngredients() {
        return this.unpackedIngredients;
    }

    public boolean isImpossibleToPlace() {
        return this.slotInfo.isEmpty();
    }

    public static record a(int placerOutputPosition) {

    }
}
