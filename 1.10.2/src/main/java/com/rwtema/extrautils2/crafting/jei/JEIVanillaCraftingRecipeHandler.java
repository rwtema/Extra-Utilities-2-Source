package com.rwtema.extrautils2.crafting.jei;

import com.rwtema.extrautils2.api.recipes.IRecipeInfoWrapper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

public class JEIVanillaCraftingRecipeHandler<T extends IRecipeInfoWrapper<? extends IRecipe>> implements IRecipeHandler<T> {

	final Class<T> clazz;

	public JEIVanillaCraftingRecipeHandler(Class<T> clazz) {
		this.clazz = clazz;
	}


	@Nonnull
	@Override
	public Class<T> getRecipeClass() {
		return clazz;
	}

	@Nonnull
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}


	@Nonnull
	@Override
	public String getRecipeCategoryUid(@Nonnull T recipe) {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull T recipe) {
		int[] dimensions = recipe.getDimensions();
		if (dimensions != null) {
			return new MyIShapedCraftingRecipeWrapper(recipe, dimensions[0], dimensions[1]);
		}

		return new MyICraftingRecipeWrapper(recipe);
	}


	public void drawInfo(T recipe, ICraftingRecipeWrapper iCraftingRecipeWrapper, Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String info = recipe.info();
		if (info != null) minecraft.fontRenderer.drawString(info, 60, 10, Color.black.getRGB());

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isRecipeValid(@Nonnull T recipe) {
		return true;
	}

	private class MyIShapedCraftingRecipeWrapper extends MyICraftingRecipeWrapper implements IShapedCraftingRecipeWrapper {
		final int width;
		final int height;

		public MyIShapedCraftingRecipeWrapper(T recipe, int width, int height) {
			super(recipe);

			this.width = width;
			this.height = height;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}

	@SuppressWarnings("deprecation")
	private class MyICraftingRecipeWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper {

		protected final T recipe;
		protected final List<List<ItemStack>> inputs;
		@ItemStackNonNull
		protected final ItemStack output;


		public MyICraftingRecipeWrapper(T recipe) {
			this.recipe = recipe;
			IRecipe originalRecipe = recipe.getOriginalRecipe();
			this.inputs = recipe.getInputList();
			this.output = originalRecipe.getRecipeOutput();
		}

		@Override
		public void getIngredients(@Nonnull IIngredients ingredients) {
			ingredients.setOutput(ItemStack.class, output);
			ingredients.setInputLists(ItemStack.class, inputs);
		}


		@Override
		public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			JEIVanillaCraftingRecipeHandler.this.drawInfo(recipe, this, minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
		}
	}
}
