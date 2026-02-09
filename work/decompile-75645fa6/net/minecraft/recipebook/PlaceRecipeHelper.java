package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.crafting.ShapedRecipes;

public interface PlaceRecipeHelper {

    static <T> void placeRecipe(int i, int j, IRecipe<?> irecipe, Iterable<T> iterable, PlaceRecipeHelper.a<T> placerecipehelper_a) {
        if (irecipe instanceof ShapedRecipes shapedrecipes) {
            placeRecipe(i, j, shapedrecipes.getWidth(), shapedrecipes.getHeight(), iterable, placerecipehelper_a);
        } else {
            placeRecipe(i, j, i, j, iterable, placerecipehelper_a);
        }

    }

    static <T> void placeRecipe(int i, int j, int k, int l, Iterable<T> iterable, PlaceRecipeHelper.a<T> placerecipehelper_a) {
        Iterator<T> iterator = iterable.iterator();
        int i1 = 0;

        for (int j1 = 0; j1 < j; ++j1) {
            boolean flag = (float) l < (float) j / 2.0F;
            int k1 = MathHelper.floor((float) j / 2.0F - (float) l / 2.0F);

            if (flag && k1 > j1) {
                i1 += i;
                ++j1;
            }

            for (int l1 = 0; l1 < i; ++l1) {
                if (!iterator.hasNext()) {
                    return;
                }

                flag = (float) k < (float) i / 2.0F;
                k1 = MathHelper.floor((float) i / 2.0F - (float) k / 2.0F);
                int i2 = k;
                boolean flag1 = l1 < k;

                if (flag) {
                    i2 = k1 + k;
                    flag1 = k1 <= l1 && l1 < k1 + k;
                }

                if (flag1) {
                    placerecipehelper_a.addItemToSlot(iterator.next(), i1, l1, j1);
                } else if (i2 == l1) {
                    i1 += i - l1;
                    break;
                }

                ++i1;
            }
        }

    }

    @FunctionalInterface
    public interface a<T> {

        void addItemToSlot(T t0, int i, int j, int k);
    }
}
