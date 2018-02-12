package com.rwtema.extrautils2.gui.backend;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WidgetBackground extends WidgetBase {
	private ResourceLocation texture;

	public WidgetBackground(IWidget other, ResourceLocation location, int expand){
		this(other.getX() - expand,other.getY() - expand,other.getW() + 2 * expand,other.getH() + 2 * expand, location);
	}

	public WidgetBackground(int x, int y, int w, int h, ResourceLocation texture) {
		super(x, y, w, h);
		this.texture = texture;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		gui.drawBasicBackground(guiLeft + getX(), guiTop + getY(), getW(), getH());
	}
}
