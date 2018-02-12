package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import com.rwtema.extrautils2.compatibility.XUShapelessRecipe;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

// 🛠 Crafting methods 🛠
public class CraftingHelper {
	public final static ThreadLocal<Collection<? super IRecipe>> recipeCallback = new ThreadLocal<>();
	private final static Set<Class<? extends IRecipe>> registeredRecipes = new HashSet<>();

	static {
		registeredRecipes.add(AlwaysLast.XUShapedRecipeAlwaysLast.class);
		RecipeSorter.register("extrautils2:alwaysLastShaped", AlwaysLast.XUShapedRecipeAlwaysLast.class, SHAPELESS, "after:minecraft:shaped after:minecraft:shapeless after:forge:shapedore after:forge:shapelessore after:minecraft:repair after:minecraft:pattern_add after:minecraft:mapextending after:minecraft:armordyes after:minecraft:fireworks after:minecraft:bookcloning");
		registeredRecipes.add(AlwaysLast.XUShapelessRecipeAlwaysLast.class);
		RecipeSorter.register("extrautils2:alwaysLastShapeless", AlwaysLast.XUShapedRecipeAlwaysLast.class, SHAPELESS, "after:minecraft:shaped after:minecraft:shapeless after:forge:shapedore after:forge:shapelessore after:minecraft:repair after:minecraft:pattern_add after:minecraft:mapextending after:minecraft:armordyes after:minecraft:fireworks after:minecraft:bookcloning");
	}

	public static void addRecipe(IRecipe recipe) {
		Collection<? super IRecipe> objects = recipeCallback.get();
		if (objects != null) objects.add(recipe);
		registerRecipe(recipe.getClass());
		CompatHelper112.addRecipe(recipe);
	}

	public static ResourceLocation createLocation(String string) {
		return new ResourceLocation(ExtraUtils2.MODID, string.toLowerCase(Locale.ENGLISH));
	}

	public static void addShapeless(String location, ItemStack stack, Object... recipe) {
		addRecipe(new XUShapelessRecipe(createLocation(location), stack, recipe));
	}

	public static void addShaped(String location, ItemStack stack, Object... recipe) {
		addRecipe(new XUShapedRecipe(createLocation(location), stack, recipe));
	}

	public static void addShapeless(String location, Item stack, Object... recipe) {
		addRecipe(new XUShapelessRecipe(createLocation(location), stack, recipe));
	}

	public static void addShaped(String location, Item stack, Object... recipe) {
		addRecipe(new XUShapedRecipe(createLocation(location), stack, recipe));
	}

	public static void addShapeless(String location, Block stack, Object... recipe) {
		addRecipe(new XUShapelessRecipe(createLocation(location), stack, recipe));
	}

	public static void addShaped(String location, Block stack, Object... recipe) {
		addRecipe(new XUShapedRecipe(createLocation(location), stack, recipe));
	}

	public static void registerRecipe(Class<? extends IRecipe> recipe) {
		if (registeredRecipes.contains(recipe))
			return;

		if (!recipe.getName().startsWith("com.rwtema."))
			return;

		registeredRecipes.add(recipe);
		LogHelper.fine("Registering " + recipe.getSimpleName() + " to RecipeSorter");
		if (ShapedOreRecipe.class.isAssignableFrom(recipe))
			RecipeSorter.register("extrautils2:" + recipe.getSimpleName(), recipe, SHAPED, "after:forge:shapedore");
		else if (ShapelessOreRecipe.class.isAssignableFrom(recipe))
			RecipeSorter.register("extrautils2:" + recipe.getSimpleName(), recipe, SHAPELESS, "after:forge:shapelessore before:extrautils2:alwaysLastShapeless");
		else if (ShapedRecipes.class.isAssignableFrom(recipe))
			RecipeSorter.register("extrautils2:" + recipe.getSimpleName(), recipe, SHAPED, "after:minecraft:shaped");
		else if (ShapelessRecipes.class.isAssignableFrom(recipe))
			RecipeSorter.register("extrautils2:" + recipe.getSimpleName(), recipe, SHAPELESS, "after:minecraft:shapeless before:extrautils2:alwaysLastShapeless");
		else
			RecipeSorter.register("extrautils2:" + recipe.getSimpleName(), recipe, SHAPELESS, "after:forge:shapelessore");
	}

	public static void registerRecipe(Class<? extends IRecipe> recipe, RecipeSorter.Category cat, String s) {
		if (registeredRecipes.contains(recipe))
			return;

		registeredRecipes.add(recipe);
		RecipeSorter.register("extrautils2:" + recipe.getSimpleName(), recipe, cat, s);
	}

	public static void addIngotBlockPackingRecipes(String base, ItemStack ingot, ItemStack block) {
		CraftingHelper.addShaped(base + "_ingot_to_block", StackHelper.safeCopy(block), "sss", "sss", "sss", 's', StackHelper.safeCopy(ingot));
		CraftingHelper.addShapeless(base + "_block_to_ingot", ItemHandlerHelper.copyStackWithSize(ingot, 9), block.copy());
	}
}
