package com.rwtema.extrautils2.api.recipes;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraftforge.common.util.Constants;

public interface ICustomRecipeMatching {

	static boolean satisfies(NBTBase target, NBTBase inputTags) {
		if (inputTags == null) return target == null || target.hasNoTags();
		if (target == null) return true;
		byte id = target.getId();

		byte otherID = inputTags.getId();
		if (id > 0 && id <= 4) {
			if (otherID <= 0 || otherID > 4) return false;
			long a;
			switch (id) {
				case Constants.NBT.TAG_BYTE:
					a = ((NBTTagByte)target).getLong();
					break;
				case Constants.NBT.TAG_SHORT:
					a = ((NBTTagShort)target).getLong();
					break;
				case Constants.NBT.TAG_INT:
					a = ((NBTTagInt)target).getLong();
					break;
				case Constants.NBT.TAG_LONG:
					a = ((NBTTagLong)target).getLong();
					break;
				default:
					throw new IllegalStateException();
			}

			long b;
			switch (otherID) {
				case Constants.NBT.TAG_BYTE:
					b = ((NBTTagByte)inputTags).getLong();
					break;
				case Constants.NBT.TAG_SHORT:
					b = ((NBTTagShort)inputTags).getLong();
					break;
				case Constants.NBT.TAG_INT:
					b = ((NBTTagInt)inputTags).getLong();
					break;
				case Constants.NBT.TAG_LONG:
					b = ((NBTTagLong)inputTags).getLong();
					break;
				default:
					throw new IllegalStateException();
			}
			return a == b;
		}

		if(id ==5 || id == 6){
			if (otherID != 5 && otherID != 6) return false;
			double a;
			switch (id) {
				case Constants.NBT.TAG_FLOAT:
					a = ((NBTTagFloat)target).getDouble();
					break;
				case Constants.NBT.TAG_DOUBLE:
					a = ((NBTTagDouble)target).getDouble();
					break;
				default:
					throw new IllegalStateException();
			}

			double b;
			switch (otherID) {
				case Constants.NBT.TAG_FLOAT:
					b = ((NBTTagFloat)inputTags).getDouble();
					break;
				case Constants.NBT.TAG_DOUBLE:
					b = ((NBTTagDouble)inputTags).getDouble();
					break;
				default:
					throw new IllegalStateException();
			}
			return a == b;
		}

		if (otherID != id) return false;

		if (id == Constants.NBT.TAG_COMPOUND) {
			NBTTagCompound nbt_a = (NBTTagCompound) target;
			NBTTagCompound nbt_b = (NBTTagCompound) inputTags;
			for (String s : nbt_a.getKeySet()) {
				if (!satisfies(nbt_a.getTag(s), nbt_b.getTag(s))) {
					return false;
				}
			}
			return true;
		} else if (id == Constants.NBT.TAG_LIST) {
			NBTTagList a = (NBTTagList) target;
			NBTTagList b = (NBTTagList) target;
			int n = a.tagCount();
			if (n != b.tagCount()) return false;
			for (int i = 0; i < n; i++) {
				if (!satisfies(a.get(i), b.get(i))) return false;
			}
			return true;
		} else {
			return target.equals(inputTags);
		}
	}

	boolean itemsMatch(ItemStack slot, @Nonnull ItemStack target);
}
