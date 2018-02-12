package com.rwtema.extrautils2.itemhandler;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class SingleStackHandler extends SingleStackHandlerBase implements INBTSerializable<NBTTagCompound> {
	@ItemStackNonNull
	protected ItemStack curStack = StackHelper.empty();

	@ItemStackNonNull
	@Override
	public ItemStack getStack() {
		return curStack;
	}

	@Override
	public void setStack(@ItemStackNonNull ItemStack stack) {
		this.curStack = stack;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		if (StackHelper.isNonNull(curStack)) {
			curStack.writeToNBT(tagCompound);
			if (StackHelper.getStacksize(curStack) > 64)
				tagCompound.setInteger("ExtendedCount", StackHelper.getStacksize(curStack));
		}
		return tagCompound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		curStack = StackHelper.empty();
		if (nbt.hasKey("id")) {
			curStack = StackHelper.loadFromNBT(nbt);
			if (StackHelper.isNonNull(curStack) && nbt.hasKey("ExtendedCount")) {
				StackHelper.setStackSize(curStack, nbt.getInteger("ExtendedCount"));
			}
		}
	}


}
