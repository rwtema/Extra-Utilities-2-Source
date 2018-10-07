package com.rwtema.extrautils2.api.resonator;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public interface IResonatorRecipe {

	List<ItemStack> getInputs();

	ItemStack getOutput();

	int getEnergy();

	default ItemStack getOutput(ItemStack input){
		return getOutput();
	}

	default boolean matches(ItemStack input){
		for (ItemStack target : getInputs()) {
			if (OreDictionary.itemMatches(target, input, false) ) {
				return true;
			}
		}
		return false;
	}

	default int getNumberOfInputsToConsume(ItemStack input){
		return 1;
	}

	default int getEnergy(ItemStack input){
		return getEnergy();
	}

	default boolean shouldAddOwnerTag(){
		return false;
	}

	default boolean shouldProgress(TileEntity resonator, int frequency, ItemStack input) {
		return true;
	}

	default String getRequirementText() {
		return "";
	}
}
