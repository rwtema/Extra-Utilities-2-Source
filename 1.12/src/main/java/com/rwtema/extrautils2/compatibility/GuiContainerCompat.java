package com.rwtema.extrautils2.compatibility;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiContainerCompat extends GuiContainer {
	public GuiContainerCompat(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}
}
