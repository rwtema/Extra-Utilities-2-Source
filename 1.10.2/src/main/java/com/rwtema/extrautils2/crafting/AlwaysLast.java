package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.RecipeCompat;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import com.rwtema.extrautils2.compatibility.XUShapelessRecipe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class AlwaysLast {

	public static class XUShapelessRecipeAlwaysLast extends XUShapelessRecipe {

		public XUShapelessRecipeAlwaysLast(ResourceLocation location, Block result, Object... recipe) {
			super(location, result, recipe);
		}

		public XUShapelessRecipeAlwaysLast(ResourceLocation location, Item result, Object... recipe) {
			super(location, result, recipe);
		}

		public XUShapelessRecipeAlwaysLast(ResourceLocation location, ItemStack result, Object... recipe) {
			super(location, result, recipe);
		}
	}

	public static class XUShapedRecipeAlwaysLast extends XUShapedRecipe  {


		public XUShapedRecipeAlwaysLast(ResourceLocation location, Block result, Object... recipe) {
			super(location, result, recipe);
		}

		public XUShapedRecipeAlwaysLast(ResourceLocation location, Item result, Object... recipe) {
			super(location, result, recipe);
		}

		public XUShapedRecipeAlwaysLast(ResourceLocation location, ItemStack result, Object... recipe) {
			super(location, result, recipe);
		}
	}

}
