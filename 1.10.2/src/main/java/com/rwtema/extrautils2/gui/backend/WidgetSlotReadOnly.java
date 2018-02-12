package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class WidgetSlotReadOnly extends WidgetSlotItemHandler implements ISlotClick {
	public WidgetSlotReadOnly(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}


	@Override
	public ItemStack slotClick(DynamicContainer dynamicContainer, int slotId, int clickedButton, ClickType mode, EntityPlayer playerIn) {
		return StackHelper.empty();
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
}
