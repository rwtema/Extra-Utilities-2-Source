package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetClickMCButtonBase extends WidgetClickBase {
	public static final ResourceLocation vanillaButtonTexture = new ResourceLocation("textures/gui/widgets.png");
	public boolean visible = true;
	public boolean enabled = true;

	public WidgetClickMCButtonBase(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (!this.visible) return;

		manager.bindTexture(vanillaButtonTexture);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		int hoverState = this.getHoverState(this.hover);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.blendFunc(770, 771);
		int x = guiLeft + getX();
		int y = guiTop + getY();


		int h = getH();
		int w2 = getW() / 2;
		int v = 46 + hoverState * 20;
		int u2 = 200 - w2;
		if (h == 20) {
			gui.drawTexturedModalRect(x, y, 0, v, w2, h);
			gui.drawTexturedModalRect(x + w2, y, u2, v, w2, h);
		} else if (h < 20) {
			int h2 = h / 2;
			gui.drawTexturedModalRect(x, y, 0, v, w2, h2);
			gui.drawTexturedModalRect(x + w2, y, u2, v, w2, h2);
			int v2 = v + 20 - (h - h2);
			gui.drawTexturedModalRect(x, y + h - h2, 0, v2, w2, h - h2);
			gui.drawTexturedModalRect(x + w2, y + h - h2, u2, v2, w2, h - h2);
		} else {
			gui.drawTexturedModalRect(x, y, 0, v, w2, 10);
			gui.drawTexturedModalRect(x + w2, y, u2, v, w2, 10);
			int y2 = y + h - 10;
			for (int k = y+10; k < y2; k += 10) {
				int dk = Math.min(k + 10, y2) - k;
				gui.drawTexturedModalRect(x, k, 0, v+5, w2, dk);
				gui.drawTexturedModalRect(x + w2, k, u2, v+5, w2, dk);
			}

			gui.drawTexturedModalRect(x, y2, 0, v + 10, w2, 10);
			gui.drawTexturedModalRect(x + w2, y2, u2, v + 10, w2, 10);
		}


	}

	@SideOnly(Side.CLIENT)
	private int getHoverState(boolean hover) {
		int i = 1;

		if (!this.enabled) {
			i = 0;
		} else if (mouseOver) {
			i = 2;
		}

		return i;
	}
}
