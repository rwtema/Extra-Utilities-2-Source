package com.rwtema.extrautils2.compatibility;


import com.rwtema.extrautils2.gui.backend.WidgetCraftingMatrix;
import com.rwtema.extrautils2.tile.TileCrafter;
import com.rwtema.extrautils2.utils.datastructures.ConcatList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class CraftingHelper112 {
	public static List<IRecipe> getRecipeList() {
		return CraftingManager.func_77594_a().func_77592_b();
	}


	public static Object[] getShapedIngredientRecipes(Object... recipes) {
		return new Object[]{"S", 'S', Items.STICK};
	}


	public static void processRecipe(XUShapedRecipe shapedRecipe, ItemStack result, Object... recipe) {

		int width = 0;
		int height = 0;

		StringBuilder shape = new StringBuilder();
		int idx = 0;

		if (recipe[idx] instanceof BiFunction) {
			shapedRecipe.finalOutputTransform = (BiFunction<ItemStack, InventoryCrafting, ItemStack>) recipe[idx];
			if (recipe[idx + 1] instanceof Object[]) {
				recipe = (Object[]) recipe[idx + 1];
			} else {
				idx++;
			}
		}

		if (recipe[idx] instanceof Boolean) {
			shapedRecipe.setMirrored((Boolean) recipe[idx]);
			if (recipe[idx + 1] instanceof Object[]) {
				recipe = (Object[]) recipe[idx + 1];
			} else {
				idx++;
			}
		}

		if (recipe[idx] instanceof String[]) {
			String[] parts = ((String[]) recipe[idx++]);

			for (String s : parts) {
				width = s.length();
				shape.append(s);
			}

			height = parts.length;
		} else {
			while (recipe[idx] instanceof String) {
				String s = (String) recipe[idx++];
				shape.append(s);
				width = s.length();
				height++;
			}
		}

		if (width * height != shape.length()) {
			StringBuilder ret = new StringBuilder("Invalid shaped ore recipe: ");
			for (Object tmp : recipe) {
				ret.append(tmp).append(", ");
			}
			ret.append(shapedRecipe.getRecipeOutput());
			throw new RuntimeException(ret.toString());
		}


		HashMap<Character, Object> itemMap = new
				HashMap<>();

		for (; idx < recipe.length; idx += 2) {
			Character chr = (Character) recipe[idx];
			Object in = recipe[idx + 1];

			itemMap.put(chr, getRecipeObject(in));
		}

		Object[] input = new Object[width * height];
		int x = 0;
		for (char chr : shape.toString().toCharArray()) {
			input[x++] = itemMap.get(chr);
		}

		shapedRecipe.setWidth(width);
		shapedRecipe.setHeight(height);
		shapedRecipe.setInput(input);
	}

	public static Object getRecipeObject(Object in) {
		ConcatList<ItemStack> list = new ConcatList<>();
		XUShapedRecipe.handleListInput(list, in);
		Object out;

		if (list.subLists.size() == 1) {
			if (list.size() == 1) {
				out = list.modifiableList.get(0);
			} else {
				out = list.modifiableList;
			}
		} else {
			if (list.subLists.size() == 2 && list.modifiableList.isEmpty()) {
				out = list.subLists.get(1);
			} else
				out = list;
		}
		return out;
	}

	public static ItemStack getMatchingResult(WidgetCraftingMatrix widgetCraftingMatrix, EntityPlayer player) {
		return CraftingManager.func_77594_a().findMatchingResult(widgetCraftingMatrix.crafter, player.world);
	}

	@Nonnull
	public static List<Object> getRecipeInputs(IRecipe curRecipe) {
		List<Object> input = new ArrayList<>();
		if (curRecipe instanceof ShapedRecipes || curRecipe instanceof ShapedOreRecipe) {
			int w, h;
			Object[] inputs;
			if (curRecipe instanceof ShapedOreRecipe) {
				ShapedOreRecipe recipe = (ShapedOreRecipe) curRecipe;
				inputs = recipe.getInput();
				w = TileCrafter.SOR_WIDTH.getUnchecked(recipe);
				h = TileCrafter.SOR_HEIGHT.getUnchecked(recipe);
			} else {
				ShapedRecipes recipe = (ShapedRecipes) curRecipe;
				inputs = recipe.recipeItems;
				w = recipe.recipeWidth;
				h = recipe.recipeHeight;
			}
			Object[] inputs3x3 = new Object[9];
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (x >= 0 && x < w && y >= 0 && y < h) {
						inputs3x3[x + y * 3] = inputs[x + y * w];
					}
				}
			}
			Collections.addAll(input, inputs3x3);
		} else if (curRecipe instanceof ShapelessRecipes) {
			input.addAll(((ShapelessRecipes) curRecipe).recipeItems);
		} else if (curRecipe instanceof ShapelessOreRecipe) {
			input.addAll(((ShapelessOreRecipe) curRecipe).getInput());
		}
		return input;
	}

	public static Object unwrapIngredients(Object o) {
		return o;
	}
}
