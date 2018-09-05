package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.tile.TileResonator;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import gnu.trove.set.hash.TCustomHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashSet;

public class ResonatorRecipe {
	public static HashSet<Item> WildCardItems = new HashSet<>();
	public static TCustomHashSet<ItemStack> SpecificItems = new TCustomHashSet<>(CollectionHelper.HASHING_STRATEGY_ITEMSTACK);

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

	public boolean shouldProgress(TileResonator resonator, int frequency) {
		return true;
	}

	public String getRequirementText() {
		return "";
	}
}
