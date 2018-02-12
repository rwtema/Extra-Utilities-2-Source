package com.rwtema.extrautils2.compatibility;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiContainerCompat extends GuiContainer {
	public GuiContainerCompat(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}
}
