package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.api.recipes.ICustomRecipeMatching;
import com.rwtema.extrautils2.compatibility.CraftingHelper112;
import com.rwtema.extrautils2.compatibility.ShapelessOreCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.IItemMatcher;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XUShapelessRecipe extends ShapelessOreCompat implements IItemMatcher, RecipeCompat {
	public XUShapelessRecipe(ResourceLocation location, Block result, Object... recipe) {
		this(location, new ItemStack(result), recipe);
	}

	public XUShapelessRecipe(ResourceLocation location, Item result, Object... recipe) {
		this(location, new ItemStack(result), recipe);
	}

	public XUShapelessRecipe(ResourceLocation location, ItemStack result, Object... recipe) {
		super(location, result, CraftingHelper112.getShapedIngredientRecipes(recipe));
	}

	public boolean itemsMatch(ItemStack slot, @Nonnull ItemStack target) {
		Item item = target.getItem();
		if (item instanceof ICustomRecipeMatching)
			return ((ICustomRecipeMatching) item).itemsMatch(slot, target);

		return OreDictionary.itemMatches(target, slot, false);
	}
}
