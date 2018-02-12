package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class AnvilRecipe extends XUShapedRecipe {
	public AnvilRecipe(ResourceLocation location ,Block result, Object... recipe) {
		super(location, result, recipe);
	}

	public AnvilRecipe(ResourceLocation location ,Item result, Object... recipe) {
		super(location, result, recipe);
	}

	public AnvilRecipe(ResourceLocation location ,ItemStack result, Object... recipe) {
		super(location, result, recipe);
	}

	@Nonnull
	@Override
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		ItemStack[] remainingItemsBase = super.getRemainingItemsBase(inv);
		for (int i = 0; i < remainingItemsBase.length; i++) {
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if (StackHelper.isNonNull(stackInSlot) && stackInSlot.getItem() == Item.getItemFromBlock(Blocks.ANVIL)) {
				ItemStack copy = stackInSlot.copy();
				StackHelper.setStackSize(copy, 1);
				remainingItemsBase[i] = copy;
			}
		}
		return remainingItemsBase;
	}
}
