package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class OreDicHelper {
	final static HashMultimap<String, ItemRef> registeredOres = HashMultimap.create();

	public static String extendVanillaOre(String name, Item item) {
		return extendVanillaOre(name, new ItemStack(item));
	}

	public static String extendVanillaOre(String name, Block block) {
		return extendVanillaOre(name, new ItemStack(block));
	}

	public static String extendVanillaOre(String name, ItemStack itemStack) {
		ItemRef ref = ItemRef.wrap(itemStack);
		if (registeredOres.put(name, ref)) {
			OreDictionary.registerOre(name, itemStack);
		}
		return name;
	}

	public static String extendVanillaOres(String oreName, ItemStack... stacks) {
		ArrayList<ItemStack> itemStacks = Lists.newArrayList(stacks);
		itemStacks.removeIf(stack -> OreDictionary.getOres(oreName).stream().anyMatch( target-> OreDictionary.itemMatches(target, stack, false)));
		for (ItemStack stack : itemStacks) {
			OreDictionary.registerOre(oreName, stack);
		}

		return oreName;
	}
}
