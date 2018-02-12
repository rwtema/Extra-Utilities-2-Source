package com.rwtema.extrautils2.itemhandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.annotation.Nonnull;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class StackDump implements Iterable<ItemStack>, INBTSerializable<NBTTagList> {
	public final LinkedList<ItemStack> stacks = new LinkedList<>();

	public void addStack(@Nonnull ItemStack stack) {
		if (StackHelper.isEmpty(stack)) return;
		int maxStackSize = stack.getMaxStackSize();
		if (maxStackSize > 1) {
			for (ItemStack itemStack : stacks) {
				if (ItemHandlerHelper.canItemStacksStack(stack, itemStack)) {
					int a = maxStackSize - StackHelper.getStacksize(itemStack);
					if (a == 0) continue;
					int min = Math.min(a, StackHelper.getStacksize(stack));
					StackHelper.increase(itemStack, min);
					StackHelper.decrease(stack, min);
					if (StackHelper.isEmpty(stack)) {
						return;
					}
				}
			}
		}

		stacks.add(stack);
	}

	@Override
	public NBTTagList serializeNBT() {
		NBTTagList list = new NBTTagList();
		for (ItemStack itemStack : stacks) {
			NBTTagCompound nbt = new NBTTagCompound();
			itemStack.writeToNBT(nbt);
			list.appendTag(nbt);
		}

		return list;
	}

	@Override
	public void deserializeNBT(NBTTagList nbt) {
		stacks.clear();
		for (int i = 0; i < nbt.tagCount(); i++) {
			ItemStack stack = StackHelper.loadFromNBT(nbt.getCompoundTagAt(i));
			if (StackHelper.isNonNull(stack)) {
				stacks.add(stack);
			}
		}
	}


	@Override
	public Iterator<ItemStack> iterator() {
		return stacks.iterator();
	}

	public void attemptDump(IItemHandler contents) {
		for (ListIterator<ItemStack> iterator = stacks.listIterator(); iterator.hasNext(); ) {
			ItemStack stack = iterator.next();
			ItemStack insert = InventoryHelper.insert(contents, stack, false);
			if (StackHelper.isNonNull(insert)) {
				iterator.set(insert);
			} else {
				iterator.remove();
			}
		}
	}

	public boolean hasStacks() {
		return !stacks.isEmpty();
	}
}
