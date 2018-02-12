package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StackHelper {
	public static boolean isNull(@Nullable ItemStack stack) {
		return stack == null || stack.isEmpty();
	}

	public static boolean isNonNull(@Nullable ItemStack stack) {
		return stack != null && !stack.isEmpty();
	}

	public static int getStacksize(@Nonnull ItemStack stack) {
		return stack.getCount();
	}

	public static void setStackSize(@Nonnull ItemStack stack, int amount) {
		stack.setCount(amount);
	}

	public static void increase(@Nonnull ItemStack stack) {
		stack.grow(1);
	}

	public static void decrease(@Nonnull ItemStack stack) {
		stack.shrink(1);
	}

	public static boolean isEmpty(@Nullable ItemStack stack) {
		return stack == null || stack.isEmpty();
	}

	public static void increase(@Nonnull ItemStack stack, int amount) {
		stack.grow(amount);
	}

	public static void decrease(@Nonnull ItemStack stack, int amount) {
		stack.shrink(amount);
	}

	@ItemStackNonNull
	public static ItemStack empty() {
		return ItemStack.EMPTY;
	}

	@ItemStackNonNull
	public static ItemStack loadFromNBT(@Nonnull NBTTagCompound tag) {
		return new ItemStack(tag);
	}

	@ItemStackNonNull
	public static ItemStack safeCopy(ItemStack stack) {
		if (stack == null) return empty();
		return stack.copy();
	}


	public static Item nullItem() {
		return Items.AIR;
	}


	public static NBTTagCompound serializeSafe(ItemStack stack) {
		if (stack == null) return new NBTTagCompound();
		return stack.serializeNBT();
	}

	public static ItemStack deserializeSafe(@Nonnull NBTTagCompound tag) {
		ItemStack stack = new ItemStack(tag);
		if (stack.isEmpty()) return ItemStack.EMPTY;
		return stack;
	}
}
