package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;

public class WidgetTextSmallText extends WidgetTextMultiline {
	double scale = 2;

	public WidgetTextSmallText(int x, int y, String msg, int w) {
		super(x, y, msg, w);
	}

	public WidgetTextSmallText(int x, int y, int align, int color, String msg) {
		super(x, y, align, color, msg);
	}

	public WidgetTextSmallText(int x, int y, int w, int h, int align, int color, String msg) {
		super(x, y, w, h, align, color, msg);
	}

	@SuppressWarnings("unchecked")
	public <T extends WidgetTextSmallText> T setScale(double scale) {
		this.scale = scale;
		return (T) this;
	}

	@Override
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		String msg = getMsgClient();

		if (msg == null) return;
		int width = (int) (gui.getFontRenderer().getStringWidth(msg) / scale);
		int x1 = getX() + ((1 - align) * (getW() - width)) / 2;

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(guiLeft + x1, guiTop + getY(), 0);
			GlStateManager.scale(1 / scale, 1 / scale, 1);
			gui.getFontRenderer().drawSplitString(msg, 0, 0, (int) (getW() * scale), 4210752);
			GlStateManager.translate(0.1, 0.1, 0);
			gui.getFontRenderer().drawSplitString(msg, 0, 0, (int) (getW() * scale), 4210752);
		}
		GlStateManager.popMatrix();
		manager.bindTexture(gui.getWidgetTexture());
	}
}
