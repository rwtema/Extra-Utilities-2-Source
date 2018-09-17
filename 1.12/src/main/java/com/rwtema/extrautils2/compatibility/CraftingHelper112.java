package com.rwtema.extrautils2.compatibility;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.api.recipes.ICustomRecipeMatching;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.gui.backend.WidgetCraftingMatrix;
import com.rwtema.extrautils2.utils.datastructures.ConcatList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftingHelper112 {

	public static List<IRecipe> getRecipeList() {
		return ForgeRegistries.RECIPES.getValues();
	}

	public static Object[] getShapedIngredientRecipes(Object... recipes) {
		for (int i = 0; i < recipes.length; i++) {
			Object in = recipes[i];
			Object result = in;
			if (in instanceof ItemStack) {
				ItemStack stack = (ItemStack) in;
				Item item = stack.getItem();
				ICustomRecipeMatching matching = null;
				if (item instanceof ICustomRecipeMatching) {
					matching = ((ICustomRecipeMatching) item);
				} else if (item instanceof ItemBlock && ((ItemBlock) item).getBlock() instanceof ICustomRecipeMatching) {
					matching = ((ICustomRecipeMatching) ((ItemBlock) item).getBlock());
				}
				if (matching != null) {
					ICustomRecipeMatching finalMatching = matching;
					result = new Ingredient(stack) {
						@Override
						public boolean apply(@Nullable ItemStack input) {
							return finalMatching.itemsMatch(input, stack);
						}
					};
				}
			} else if (in instanceof ICustomRecipeMatching) {
				ItemStack stack;
				if (in instanceof Item) {
					stack = new ItemStack((Item) in);
				} else if (in instanceof Block) stack = new ItemStack(((Block) in));
				else throw new IllegalArgumentException();
				result = new Ingredient(stack) {
					@Override
					public boolean apply(@Nullable ItemStack input) {
						return ((ICustomRecipeMatching) in).itemsMatch(input, stack);
					}
				};
			} else if (in instanceof List) {
				List<ItemStack> stackList = new ArrayList<>();
				XUShapedRecipe.handleListInput(stackList, in);
				result = new Ingredient(stackList.toArray(new ItemStack[stackList.size()])) {

				};
			} else if (in instanceof IItemStackMaker) {
				result = Ingredient.fromStacks(((IItemStackMaker) in).newStack());
			} else if (in instanceof IBlockState) {
				IBlockState state = (IBlockState) in;
				Block block = (state).getBlock();
				Item itemDropped = block.getItemDropped(state, null, 0);
				if (itemDropped != StackHelper.nullItem())
					result = Ingredient.fromStacks(new ItemStack(itemDropped, 1, block.damageDropped(state)));
			}

			recipes[i] = result;
		}
		return recipes;
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
		return CraftingManager.findMatchingResult(widgetCraftingMatrix.crafter, player.world);
	}


	@Nonnull
	public static List<Object> getRecipeInputs(IRecipe curRecipe) {
		List<Object> input = new ArrayList<>();
		if (curRecipe instanceof IShapedRecipe) {
			int w, h;
			List<Ingredient> inputs;

			IShapedRecipe recipe = (IShapedRecipe) curRecipe;
			inputs = recipe.getIngredients();
			w = recipe.getRecipeWidth();
			h = recipe.getRecipeHeight();

			Object[] inputs3x3 = new Object[9];
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					inputs3x3[x + y * 3] = inputs.get(x + y * w);
				}
			}
			Collections.addAll(input, inputs3x3);
		} else {
			input.addAll(curRecipe.getIngredients());
		}
		return input;
	}

	public static Object unwrapIngredients(Object o) {
		if (o instanceof Ingredient) {
			ItemStack[] matchingStacks = ((Ingredient) o).getMatchingStacks();
			return Lists.newArrayList(matchingStacks);
		}
		return o;
	}
}
