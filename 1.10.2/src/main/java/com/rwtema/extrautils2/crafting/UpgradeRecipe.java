package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class UpgradeRecipe extends XUShapedRecipe {

	private final int row;
	private final int column;

	public UpgradeRecipe(ResourceLocation location, ItemStack result, int row, int column, Object... recipe) {
		super(location, result, recipe);
		this.row = row;
		this.column = column;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		ItemStack craftingResult = super.getCraftingResult(var1);
		ItemStack stackInRowAndColumn = var1.getStackInRowAndColumn(row, column);
		if (StackHelper.isNonNull(craftingResult) && StackHelper.isNonNull(stackInRowAndColumn) && stackInRowAndColumn.hasTagCompound()) {
			craftingResult.setTagCompound(Validate.notNull(stackInRowAndColumn.getTagCompound()).copy());
		}
		return craftingResult;
	}
}
