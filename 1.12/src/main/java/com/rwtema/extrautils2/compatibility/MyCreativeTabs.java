package com.rwtema.extrautils2.compatibility;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.entries.Entry;
import com.rwtema.extrautils2.backend.entries.EntryHandler;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MyCreativeTabs extends CreativeTabs implements Comparator<ItemStack> {

	public MyCreativeTabs() {
		super(ExtraUtils2.MODID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ItemStackNonNull
	public ItemStack getIconItemStack() {
		if (XU2Entries.angelBlock.enabled) return super.getIconItemStack();
		return getTabIconItem();
	}

	@Nonnull
	@Override
	public ItemStack getTabIconItem() {
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

	@Override
	public boolean isTabInFirstRow() {
		return super.isTabInFirstRow();
	}

	@Override
	public void displayAllRelevantItems(@Nonnull NonNullList<ItemStack> itemStacks) {
		List<ItemStack> newList = Lists.newArrayList();
		super.displayAllRelevantItems(new NonNullListWrapper<>(newList));
		newList.sort(this);
		itemStacks.addAll(newList);
	}

	@Override
	public int compare(ItemStack o1, ItemStack o2) {
		int i = -Boolean.compare(isBlock(o1), isBlock(o2));
		if (i != 0) return i;
		i = ((new ItemStack(o1.getItem())).getDisplayName()).compareTo((new ItemStack(o2.getItem())).getDisplayName());
		if (i != 0) return i;

		return o1.getDisplayName().compareTo(o2.getDisplayName());
	}

	@Override
	public String getTabLabel() {
		return super.getTabLabel();
	}

	@Override
	public String getTranslatedTabLabel() {
		return "itemgroup." + this.getTabLabel();
	}

	public boolean isBlock(ItemStack a) {
		return a.getItem() instanceof ItemBlock;
	}
}
