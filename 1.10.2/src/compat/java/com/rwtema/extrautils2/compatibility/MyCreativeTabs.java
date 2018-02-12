package com.rwtema.extrautils2.compatibility;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.entries.Entry;
import com.rwtema.extrautils2.backend.entries.EntryHandler;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MyCreativeTabs extends CreativeTabs implements Comparator<ItemStack> {
	public MyCreativeTabs() {
		super(ExtraUtils2.MODID);
	}

	@Nonnull
	@Override
	public ItemStack getIconItemStack() {
		if (XU2Entries.angelBlock.enabled) {
			return XU2Entries.angelBlock.newStack();
		}

		List<ItemStack> stacks = Lists.newArrayList();
		for (Entry<?> activeEntry : EntryHandler.activeEntries) {
			stacks.addAll(activeEntry.getCreativeStacks());
		}

		int n = stacks.size();
		if (n == 0) throw new IllegalStateException("No active entries");
		Random random = new Random(Minecraft.getSystemTime() / 1024 / 4);
		int i = random.nextInt(n);
		return stacks.get(i);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		if (XU2Entries.angelBlock.enabled) {
			return Validate.notNull(Item.getItemFromBlock(XU2Entries.angelBlock.value));
		}
		return Items.STICK;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(@Nonnull List<ItemStack> list) {
		List<ItemStack> newList = Lists.newArrayList();
		super.displayAllRelevantItems(newList);
		newList.sort(this);
		list.addAll(newList);
	}

	@Override
	public int compare(ItemStack o1, ItemStack o2) {
		int i = -Boolean.compare(isBlock(o1), isBlock(o2));
		if (i != 0) return i;
		i = ((new ItemStack(o1.getItem())).getDisplayName()).compareTo((new ItemStack(o2.getItem())).getDisplayName());
		if (i != 0) return i;

		return o1.getDisplayName().compareTo(o2.getDisplayName());
	}

	public boolean isBlock(ItemStack a) {
		return a.getItem() instanceof ItemBlock;
	}
}
