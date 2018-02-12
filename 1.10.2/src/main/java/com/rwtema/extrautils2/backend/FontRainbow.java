package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.*;

public class FontRainbow extends FontRenderer {
	public static FontRainbow INSTANCE = new FontRainbow();
	private float hue;
	private float r;
	private float g;
	private float b;
	private float a;
	private boolean firstLine;
	private boolean firstLineOnly;

	private FontRainbow() {
		super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().getTextureManager(), Minecraft.getMinecraft().isUnicode());
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
	}

	@Override
	public int drawStringWithShadow(@Nonnull String text, float x, float y, int color) {
		int length = super.drawStringWithShadow(text, x, y, color);
		firstLine = false;
		return length;
	}

	@Override
	public void drawSplitString(@Nonnull String str, int x, int y, int wrapWidth, int textColor) {
		super.drawSplitString(str, x, y, wrapWidth, textColor);
		firstLine = false;
	}

	public FontRainbow init(boolean firstLineOnly) {
		hue = ((int) (Minecraft.getSystemTime() / 80) / 16F);
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		setUnicodeFlag(fontRenderer.getUnicodeFlag());
		setBidiFlag(fontRenderer.getBidiFlag());
		firstLine = true;
		this.firstLineOnly = firstLineOnly;
		return this;
	}


	@Override
	protected void setColor(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;

		setHueColor();
	}

	@Override
	protected float renderDefaultChar(int ch, boolean italic) {
		advanceHue();
		setHueColor();
		return super.renderDefaultChar(ch, italic);
	}

	private void advanceHue() {
		hue += 1 / 16F;
	}

	private void setHueColor() {
		int rgb = Color.HSBtoRGB(hue, 1, 1);

		if (!firstLine && firstLineOnly) {
			super.setColor(r, g, b, a);
			return;
		}


		if ((r == g && g == b)) {
			super.setColor(
					r * ColorHelper.getRF(rgb),
					g * ColorHelper.getGF(rgb),
					b * ColorHelper.getBF(rgb),
					a);
		} else {
			super.setColor(
					r * 0.5F * (1 + ColorHelper.getRF(rgb)),
					g * 0.5F * (1 + ColorHelper.getGF(rgb)),
					b * 0.5F * (1 + ColorHelper.getBF(rgb)),
					a);
		}
	}

	@Override
	protected float renderUnicodeChar(char ch, boolean italic) {
		advanceHue();
		setHueColor();
		return super.renderUnicodeChar(ch, italic);
	}
}
