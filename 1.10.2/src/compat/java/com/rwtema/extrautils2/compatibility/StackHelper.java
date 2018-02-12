package com.rwtema.extrautils2.compatibility;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class StackHelper {
	public static boolean isNull(@Nullable ItemStack stack) {
		return stack == null;
	}
	public static boolean isNonNull(@Nullable ItemStack stack) {
		return stack != null;
	}

	public static int getStacksize(@Nonnull ItemStack stack) {
		return stack == null ? 0 : stack.stackSize;
	}

	public static void setStackSize(@Nonnull ItemStack stack, int amount) {
		stack.stackSize = amount;
	}

	public static void increase(@Nonnull ItemStack stack) {
		stack.stackSize++;
	}

	public static void decrease(@Nonnull ItemStack stack) {
		stack.stackSize--;
	}

	public static boolean isEmpty(@Nonnull ItemStack stack) {
		return StackHelper.isNull(stack) || StackHelper.getStacksize(stack) == 0;
	}

	public static void increase(@Nonnull ItemStack stack, int amount) {
		stack.stackSize += amount;
	}

	public static void decrease(@Nonnull ItemStack stack, int amount) {
		stack.stackSize -= amount;
	}

	@Nullable
	public static ItemStack empty() {
		return null;
	}

	@Nonnull
	public static ItemStack loadFromNBT(@Nonnull NBTTagCompound tag){
		return ItemStack.func_77949_a(tag);
	}

	@Nonnull
	public static ItemStack safeCopy(@Nonnull ItemStack stack){
		return ItemStack.func_77944_b(stack);
	}

	@Nullable
	public static Item nullItem() {
		return null;
	}

	public static NBTTagCompound serializeSafe(ItemStack stack){
		if(stack == null) return new NBTTagCompound();
		return stack.serializeNBT();
	}

	public static ItemStack deserializeSafe(@Nonnull NBTTagCompound tag){
		if(tag == null) return empty();
		return ItemStack.func_77949_a(tag);
	}
}
