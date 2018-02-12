package com.rwtema.extrautils2.gui.backend;

import java.util.List;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetBase implements IWidget {
	protected int x, y, w, h;
	protected DynamicContainer container;
	@SideOnly(Side.CLIENT)
	protected DynamicGui gui;

	public WidgetBase(int x, int y, int w, int h) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addToGui(DynamicGui gui) {
		this.gui = gui;
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

	@Override
	public void addToContainer(DynamicContainer container) {
		this.container = container;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getToolTip() {
		return null;
	}
}
