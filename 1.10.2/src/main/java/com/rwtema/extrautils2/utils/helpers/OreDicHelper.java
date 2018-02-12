package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.HashMultimap;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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
}
