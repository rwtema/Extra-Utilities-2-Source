package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class WidgetSlotRespectsInsertExtract extends WidgetSlot {
	public WidgetSlotRespectsInsertExtract(ISidedInventory inv, int slot, int x, int y) {
		super(inv, slot, x, y);
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
		return StackHelper.isNonNull(this.getStack()) && ((ISidedInventory) inventory).canExtractItem(this.getSlotIndex(), this.getStack(), EnumFacing.DOWN);

	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		return !(this.getHasStack() || !super.isItemValid(par1ItemStack)) && ((ISidedInventory) inventory).canInsertItem(this.getSlotIndex(), par1ItemStack, EnumFacing.DOWN);

	}
}
