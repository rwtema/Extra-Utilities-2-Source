package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.RecipeCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NullRecipe implements RecipeCompat {
	public static final NullRecipe INSTANCE = new NullRecipe();

	private NullRecipe() {
	}

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
		return false;
	}

	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		return StackHelper.empty();
	}


	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public int func_77570_a() {
		return 9;
	}

	@Override
	public int getRecipeSize() {
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return StackHelper.empty();
	}

	@Nonnull
	@Override
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		return new ItemStack[0];
	}


	public IRecipe setRegistryName(ResourceLocation name) {
		throw new IllegalStateException();
	}

	@Nullable
	public ResourceLocation getRegistryName() {
		throw new IllegalStateException();
	}

	public Class<IRecipe> getRegistryType() {
		throw new IllegalStateException();
	}
}
