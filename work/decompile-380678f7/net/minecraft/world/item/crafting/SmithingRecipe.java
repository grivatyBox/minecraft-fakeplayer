package net.minecraft.world.item.crafting;

import java.util.Optional;
import net.minecraft.world.level.World;

public interface SmithingRecipe extends IRecipe<SmithingRecipeInput> {

    @Override
    default Recipes<SmithingRecipe> getType() {
        return Recipes.SMITHING;
    }

    @Override
    RecipeSerializer<? extends SmithingRecipe> getSerializer();

    default boolean matches(SmithingRecipeInput smithingrecipeinput, World world) {
        return RecipeItemStack.testOptionalIngredient(this.templateIngredient(), smithingrecipeinput.template()) && this.baseIngredient().test(smithingrecipeinput.base()) && RecipeItemStack.testOptionalIngredient(this.additionIngredient(), smithingrecipeinput.addition());
    }

    Optional<RecipeItemStack> templateIngredient();

    RecipeItemStack baseIngredient();

    Optional<RecipeItemStack> additionIngredient();

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.SMITHING;
    }
}
