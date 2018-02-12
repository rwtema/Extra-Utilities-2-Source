package com.rwtema.extrautils2.gui.backend;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IWidgetMouseInput extends IWidget {
	@SideOnly(Side.CLIENT)
	void mouseClicked(int mouseX, int mouseY, int mouseButton, boolean mouseOver);

	@SideOnly(Side.CLIENT)
	void mouseReleased(int mouseX, int mouseY, int mouseButton, boolean mouseOver);

	@SideOnly(Side.CLIENT)
	void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastMove, boolean mouseOver);

	@SideOnly(Side.CLIENT)
	void mouseWheelScroll(int delta, boolean mouseOver);

	@SideOnly(Side.CLIENT)
	void mouseTick(int mouseX, int mouseY, boolean mouseOver);

	@SideOnly(Side.CLIENT)
	boolean usesMouseWheel();
}
