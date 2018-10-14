package com.rwtema.extrautils2.crafting;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.api.resonator.IResonatorRecipe;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class ResonatorRecipe implements IResonatorRecipe {
	public ItemStack input;
	public ItemStack output;
	public int energy;
	public boolean addOwnerTag;

	public ResonatorRecipe(ItemStack input, ItemStack output, int energy, boolean addOwnerTag) {
		this.input = input;
		this.output = output;
		this.energy = energy;
		this.addOwnerTag = addOwnerTag;
	}

	@Override
	public String toString() {
		return "ResonatorRecipe{" +
				"input=" + input +
				", output=" + output +
				", energy=" + energy +
				'}';
	}

	@Override
	public boolean shouldProgress(TileEntity resonator, int frequency, ItemStack input) {
		return true;
	}

	@Override
	public List<ItemStack> getInputs() {
		return ImmutableList.of(input);
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public int getNumberOfInputsToConsume(ItemStack inputStack) {
		return StackHelper.getStacksize(input);
	}

	@Override
	public int getEnergy() {
		return energy;
	}

	@Override
	public boolean shouldAddOwnerTag() {
		return addOwnerTag;
	}
}
