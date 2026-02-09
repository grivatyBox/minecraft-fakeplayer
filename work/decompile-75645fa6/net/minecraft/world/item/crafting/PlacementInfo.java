package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlacementInfo {

    public static final int EMPTY_SLOT = -1;
    public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), IntList.of());
    private final List<RecipeItemStack> ingredients;
    private final IntList slotsToIngredientIndex;

    private PlacementInfo(List<RecipeItemStack> list, IntList intlist) {
        this.ingredients = list;
        this.slotsToIngredientIndex = intlist;
    }

    public static PlacementInfo create(RecipeItemStack recipeitemstack) {
        return recipeitemstack.isEmpty() ? PlacementInfo.NOT_PLACEABLE : new PlacementInfo(List.of(recipeitemstack), IntList.of(0));
    }

    public static PlacementInfo createFromOptionals(List<Optional<RecipeItemStack>> list) {
        int i = list.size();
        List<RecipeItemStack> list1 = new ArrayList(i);
        IntList intlist = new IntArrayList(i);
        int j = 0;

        for (Optional<RecipeItemStack> optional : list) {
            if (optional.isPresent()) {
                RecipeItemStack recipeitemstack = (RecipeItemStack) optional.get();

                if (recipeitemstack.isEmpty()) {
                    return PlacementInfo.NOT_PLACEABLE;
                }

                list1.add(recipeitemstack);
                intlist.add(j++);
            } else {
                intlist.add(-1);
            }
        }

        return new PlacementInfo(list1, intlist);
    }

    public static PlacementInfo create(List<RecipeItemStack> list) {
        int i = list.size();
        IntList intlist = new IntArrayList(i);

        for (int j = 0; j < i; ++j) {
            RecipeItemStack recipeitemstack = (RecipeItemStack) list.get(j);

            if (recipeitemstack.isEmpty()) {
                return PlacementInfo.NOT_PLACEABLE;
            }

            intlist.add(j);
        }

        return new PlacementInfo(list, intlist);
    }

    public IntList slotsToIngredientIndex() {
        return this.slotsToIngredientIndex;
    }

    public List<RecipeItemStack> ingredients() {
        return this.ingredients;
    }

    public boolean isImpossibleToPlace() {
        return this.slotsToIngredientIndex.isEmpty();
    }
}
