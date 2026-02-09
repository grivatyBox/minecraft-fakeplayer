package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.NonNullList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

// CraftBukkit start
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
// CraftBukkit end

public class ShapedRecipes implements RecipeCrafting {

    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, ShapedRecipePattern shapedrecipepattern, ItemStack itemstack, boolean flag) {
        this.group = s;
        this.category = craftingbookcategory;
        this.pattern = shapedrecipepattern;
        this.result = itemstack;
        this.showNotification = flag;
    }

    public ShapedRecipes(String s, CraftingBookCategory craftingbookcategory, ShapedRecipePattern shapedrecipepattern, ItemStack itemstack) {
        this(s, craftingbookcategory, shapedrecipepattern, itemstack, true);
    }

    // CraftBukkit start
    @Override
    public org.bukkit.inventory.ShapedRecipe toBukkitRecipe(NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapedRecipe recipe = new CraftShapedRecipe(id, result, this);
        recipe.setGroup(this.group);
        recipe.setCategory(CraftRecipe.getCategory(this.category()));

        switch (this.pattern.height()) {
        case 1:
            switch (this.pattern.width()) {
            case 1:
                recipe.shape("a");
                break;
            case 2:
                recipe.shape("ab");
                break;
            case 3:
                recipe.shape("abc");
                break;
            }
            break;
        case 2:
            switch (this.pattern.width()) {
            case 1:
                recipe.shape("a","b");
                break;
            case 2:
                recipe.shape("ab","cd");
                break;
            case 3:
                recipe.shape("abc","def");
                break;
            }
            break;
        case 3:
            switch (this.pattern.width()) {
            case 1:
                recipe.shape("a","b","c");
                break;
            case 2:
                recipe.shape("ab","cd","ef");
                break;
            case 3:
                recipe.shape("abc","def","ghi");
                break;
            }
            break;
        }
        char c = 'a';
        for (RecipeItemStack list : this.pattern.ingredients()) {
            RecipeChoice choice = CraftRecipe.toBukkit(list);
            if (choice != null) {
                recipe.setIngredient(c, choice);
            }

            c++;
        }
        return recipe;
    }
    // CraftBukkit end

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(IRegistryCustom iregistrycustom) {
        return this.result;
    }

    @Override
    public NonNullList<RecipeItemStack> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= this.pattern.width() && j >= this.pattern.height();
    }

    public boolean matches(InventoryCrafting inventorycrafting, World world) {
        return this.pattern.matches(inventorycrafting);
    }

    public ItemStack assemble(InventoryCrafting inventorycrafting, IRegistryCustom iregistrycustom) {
        return this.getResultItem(iregistrycustom).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<RecipeItemStack> nonnulllist = this.getIngredients();

        return nonnulllist.isEmpty() || nonnulllist.stream().filter((recipeitemstack) -> {
            return !recipeitemstack.isEmpty();
        }).anyMatch((recipeitemstack) -> {
            return recipeitemstack.getItems().length == 0;
        });
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipes> {

        public static final Codec<ShapedRecipes> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((shapedrecipes) -> {
                return shapedrecipes.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapedrecipes) -> {
                return shapedrecipes.category;
            }), ShapedRecipePattern.MAP_CODEC.forGetter((shapedrecipes) -> {
                return shapedrecipes.pattern;
            }), ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((shapedrecipes) -> {
                return shapedrecipes.result;
            }), ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter((shapedrecipes) -> {
                return shapedrecipes.showNotification;
            })).apply(instance, ShapedRecipes::new);
        });

        public Serializer() {}

        @Override
        public Codec<ShapedRecipes> codec() {
            return ShapedRecipes.Serializer.CODEC;
        }

        @Override
        public ShapedRecipes fromNetwork(PacketDataSerializer packetdataserializer) {
            String s = packetdataserializer.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory) packetdataserializer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.fromNetwork(packetdataserializer);
            ItemStack itemstack = packetdataserializer.readItem();
            boolean flag = packetdataserializer.readBoolean();

            return new ShapedRecipes(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }

        public void toNetwork(PacketDataSerializer packetdataserializer, ShapedRecipes shapedrecipes) {
            packetdataserializer.writeUtf(shapedrecipes.group);
            packetdataserializer.writeEnum(shapedrecipes.category);
            shapedrecipes.pattern.toNetwork(packetdataserializer);
            packetdataserializer.writeItem(shapedrecipes.result);
            packetdataserializer.writeBoolean(shapedrecipes.showNotification);
        }
    }
}
