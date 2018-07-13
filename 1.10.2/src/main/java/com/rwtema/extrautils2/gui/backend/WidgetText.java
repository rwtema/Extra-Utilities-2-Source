package com.rwtema.extrautils2.gui.backend;


import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class WidgetText extends WidgetBase implements IWidget {
	public int align, color;
	public String msg;

	public WidgetText(int x, int y, String msg) {
		this(x, y, msg, ExtraUtils2.proxy.apply(DynamicContainer.STRING_WIDTH_FUNCTION, msg));
	}

	public WidgetText(int x, int y, String msg, int w) {
		this(x, y, w, 9, 1, 4210752, msg);
	}

	public WidgetText(int x, int y, int align, int color, String msg) {
		this(x, y, msg.length() * 12, 9, align, color, msg);
	}

	public WidgetText(int x, int y, int w, int h, int align, int color, String msg) {
		super(x, y, w, h);

		this.align = align;
		this.color = color;
		this.msg = msg;
	}

	@SuppressWarnings("unchecked")
	public <T extends WidgetText> T setAlign(int align) {
		this.align = align;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends WidgetText> T setColor(int color) {
		this.color = color;
		return (T) this;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getW() {
		return w;
	}

	@Override
	public int getH() {
		return h;
	}

	public String getMsgClient() {
		return msg;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		GlStateManager.color(1, 1, 1, 1);
		int x = getX() + ((1 - align) * (getW() - gui.getFontRenderer().getStringWidth(getMsgClient()))) / 2;
		gui.getFontRenderer().drawString(getMsgClient(), guiLeft + x, guiTop + getY(), 4210752);
		manager.bindTexture(gui.getWidgetTexture());
		GlStateManager.color(1, 1, 1, 1);
	}

	@Override
	public void addToContainer(DynamicContainer container) {
	}

	@Override
	public List<String> getToolTip() {

		return null;
	}

	@Override
	public void addToGui(DynamicGui gui) {
		super.addToGui(gui);
	}
}
