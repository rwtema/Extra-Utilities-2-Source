package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class WidgetSlotGhost extends WidgetSlotItemHandler implements ISlotClick {

	public WidgetSlotGhost(IItemHandlerModifiable itemHandlerModifiable, int index, int xPosition, int yPosition) {
		super(itemHandlerModifiable, index, xPosition, yPosition);
	}

	@Override
	public ItemStack slotClick(DynamicContainer dynamicContainer, int slotId, int clickedButton, ClickType mode, EntityPlayer playerIn) {
		putStack(playerIn.inventory.getItemStack());
		dynamicContainer.detectAndSendChanges();
		return StackHelper.empty();
	}

	@Override
	public void putStack(ItemStack stack) {
		if (StackHelper.isNonNull(stack)) {
			stack = stack.copy();
			StackHelper.setStackSize(stack, 1);
		}
		super.putStack(stack);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		return StackHelper.empty();
	}
}
