package com.rwtema.extrautils2.gui.backend;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public interface ISlotClick {
	ItemStack slotClick(DynamicContainer dynamicContainer, int slotId, int clickedButton, ClickType mode, EntityPlayer playerIn);
}
