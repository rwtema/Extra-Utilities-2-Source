package com.rwtema.extrautils2.backend;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.client.IDisplayableError;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class XUGuiDisplayError extends CustomModLoadingErrorDisplayException implements IDisplayableError {

	final String title, message;

	public XUGuiDisplayError(String title, String message) {
		this.title = title;
		this.message = message;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {

	}

	@Override
	public void drawScreen(GuiErrorScreen gui, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
		gui.drawCenteredString(fontRenderer, this.title, gui.width / 2, 90, 16777215);
		int y = 110;
		for (String s : fontRenderer.listFormattedStringToWidth(message, (gui.width * 9) / 10)) {
			gui.drawCenteredString(fontRenderer, s, gui.width / 2, y, 16777215);
			y += fontRenderer.FONT_HEIGHT+1;
		}
	}
}
