package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WidgetScrollBar extends WidgetBase implements IWidgetMouseInput {
	public static final int BAR_WIDTH = 14;
	public int minValue;
	public int maxValue;
	public int scrollValue;
	@SideOnly(Side.CLIENT)
	boolean allowGeneralMouseWheel;
	private float drawValue;
	private boolean isScrolling;

	public boolean hideWhenInvalid;

	public WidgetScrollBar(int x, int y, int h, int minValue, int maxValue) {
		super(x, y, BAR_WIDTH, h);
		this.minValue = minValue;
		this.maxValue = maxValue;
		scrollValue = minValue;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseClicked(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
		if (minValue == maxValue) return;
		if (mouseOver && mouseButton == 0) {
			isScrolling = true;
			scroll(mouseY - gui.guiTop - y);
		}
	}

	@SideOnly(Side.CLIENT)
	public void setValues(int minValue, int maxValue) {
//		if (minValue == this.minValue && maxValue == this.maxValue) {
//			return;
//		}
		this.minValue = minValue;
		this.maxValue = maxValue;
		if (minValue == maxValue) isScrolling = false;
		reScroll();
	}

	@SideOnly(Side.CLIENT)
	public void reScroll() {
		scrollValue = MathHelper.clamp(scrollValue, minValue, maxValue);
		float a = (scrollValue - minValue);
		drawValue = MathHelper.clamp((a * (h - 7F) / (float) (maxValue - minValue)), 0, h - 17);
		onChange();
	}

	@SideOnly(Side.CLIENT)
	private void scroll(float y) {

		if (y <= 9) {
			drawValue = 0;
			scrollValue = minValue;
		} else if (y > (h - 8)) {
			drawValue = h - 17;
			scrollValue = maxValue;
		} else {
			drawValue = y - 9;
			float a = drawValue * (maxValue - minValue) / (float) (h - 7);
			int num = minValue + Math.round(a);
			scrollValue = MathHelper.clamp(num, minValue, maxValue);
		}

		onChange();
	}

	@SideOnly(Side.CLIENT)
	protected void onChange() {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseReleased(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
		isScrolling = false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastMove, boolean mouseOver) {
		if (isScrolling) {
			scroll(mouseY - gui.guiTop - y);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if(hideWhenInvalid && minValue == maxValue) return;
		if (h == 112) {
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 220, 0, BAR_WIDTH, 112);
		} else if (h < 112) {
			int h2 = h / 2;
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 220, 0, BAR_WIDTH, h2);
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY() + h - h2 - 1, 220, 112 - h2, BAR_WIDTH, h2 + 1);
		} else {
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY(), 220, 0, BAR_WIDTH, 16);
			int k = 16;
			for (; (k + 80) < h; k += 80)
				gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY() + k, 220, 16, BAR_WIDTH, 80);
			gui.drawTexturedModalRect(guiLeft + getX(), guiTop + getY() + k, 220, 112 - (h - k), BAR_WIDTH, h - k);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if(hideWhenInvalid && minValue == maxValue) return;
		gui.drawTexturedModalRect(guiLeft + getX() + 1, guiTop + getY() + drawValue, 196, 0, 12, 15);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseWheelScroll(int delta, boolean mouseOver) {
		if (!mouseOver && !allowGeneralMouseWheel) return;

		if (delta == 0) return;
		if (delta > 0)
			delta = -1;
		else if (delta < 0)
			delta = 1;

		setValue(scrollValue + delta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseTick(int mouseX, int mouseY, boolean mouseOver) {

	}

	@Override
	public boolean usesMouseWheel() {
		return true;
	}

	public void setValue(int newValue) {
		newValue = MathHelper.clamp(newValue, minValue, maxValue);
		if (scrollValue != newValue) {
			scrollValue = newValue;
			reScroll();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addToGui(DynamicGui gui) {
		super.addToGui(gui);
		allowGeneralMouseWheel = true;
		for (IWidgetMouseInput input : gui.container.getWidgetMouseInputs()) {
			if (input != this && input.usesMouseWheel()) {
				allowGeneralMouseWheel = false;
				break;
			}
		}
	}
}
