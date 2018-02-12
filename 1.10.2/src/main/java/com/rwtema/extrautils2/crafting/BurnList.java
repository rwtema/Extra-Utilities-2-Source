package com.rwtema.extrautils2.crafting;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.ArrayList;
import java.util.List;

public class BurnList {
	private static List<ItemStack> stacks;

	public static List<ItemStack> getStacks(boolean incLava) {
		if (stacks == null) {
			ArrayList<ItemStack> list = new ArrayList<>();
			for (Item item : Item.REGISTRY) {
				if (!incLava && item == Items.LAVA_BUCKET) {
					continue;
				}

				for (ItemStack itemStack : ExtraUtils2.proxy.getSubItems(item)) {
					if (StackHelper.isNonNull(itemStack) && TileEntityFurnace.getItemBurnTime(itemStack) > 0) {
						list.add(itemStack.copy());
					}
				}
			}
			stacks = ImmutableList.copyOf(list);
		}
		return stacks;
	}
}
