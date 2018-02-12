package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetClickMCButtonText extends WidgetClickMCButtonBase {

	protected int packedFGColour;
	protected String text;

	public WidgetClickMCButtonText(String text, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.text = text;
	}

	@Override
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

		int col = 14737632;

		if (packedFGColour != 0) {
			col = packedFGColour;
		} else if (!this.enabled) {
			col = 10526880;
		} else if (this.hover) {
			col = 16777120;
		}

		renderButtonText(gui, Minecraft.getMinecraft().fontRenderer, guiLeft + x, guiTop + y, col);
	}

	@SideOnly(Side.CLIENT)
	public void renderButtonText(DynamicGui gui, FontRenderer fontrenderer, int xPosition, int yPosition, int color) {
		gui.drawCenteredString(fontrenderer, text, xPosition + getW() / 2, yPosition + (getH() - 8) / 2, color);
	}
}
