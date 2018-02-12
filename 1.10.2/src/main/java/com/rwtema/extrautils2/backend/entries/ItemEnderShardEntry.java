package com.rwtema.extrautils2.backend.entries;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.items.ItemEnderShard;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ItemEnderShardEntry extends ItemClassEntry<ItemEnderShard> {
	public ItemEnderShardEntry() {
		super(ItemEnderShard.class);
	}

	@Override
	public void addRecipes() {
		addShapeless("ender_shard", newStack(8), Items.ENDER_PEARL, XU2Entries.itemGlassCutter.newWildcardStack());
	}

	public List<ItemStack> anySizeStack() {
		ItemStack[] stack = new ItemStack[8];
		for (int i = 0; i < 8; i++) {
			stack[i] = newStack(i + 1);
			if (i != 0) {
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("I", i);
				stack[i].setTagCompound(tags);
				ItemStackHelper.addLore(stack[i], Lang.translatePrefix("(Any size stack can be used.)"));
			}

		}
		return ImmutableList.copyOf(stack);
	}
}
