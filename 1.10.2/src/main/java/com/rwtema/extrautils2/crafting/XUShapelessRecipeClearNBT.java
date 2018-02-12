package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;

public class XUShapelessRecipeClearNBT extends AlwaysLast.XUShapelessRecipeAlwaysLast {
	HashSet<String> keys = new HashSet<>();

	public XUShapelessRecipeClearNBT(ResourceLocation location, ItemStack result, String... keys) {
		super(location, result, result);
		Collections.addAll(this.keys, keys);
	}

	@Override
	public boolean itemsMatch(ItemStack slot, @Nonnull ItemStack target) {
		if (super.itemsMatch(slot, target)) {
			if (StackHelper.isNull(slot)) return false;
			NBTTagCompound nbt = slot.getTagCompound();
			if (nbt == null) {
				return false;
			}
			for (String key : keys) {
				if (nbt.hasKey(key)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stackInSlot = var1.getStackInSlot(i);
			if (StackHelper.isNonNull(stackInSlot)) {
				ItemStack copy = stackInSlot.copy();

				StackHelper.setStackSize(copy , 1);
				NBTTagCompound nbt = copy.getTagCompound();
				if (nbt != null) {
					for (String key : keys) {
						nbt.removeTag(key);
					}
					copy.setTagCompound(nbt);
				}
				return copy;
			}
		}
		return StackHelper.empty();
	}
}
