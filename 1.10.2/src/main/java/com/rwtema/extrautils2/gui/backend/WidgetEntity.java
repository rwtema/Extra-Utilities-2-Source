package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.utils.client.GLStateAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

public class WidgetEntity extends WidgetBase {
	private final EntityLivingBase entityLivingBase;
	private final int scale;

	public WidgetEntity(EntityLivingBase entityLivingBase, int scale, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.entityLivingBase = entityLivingBase;
		this.scale = scale;
	}

	@Override
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		GLStateAttributes states = GLStateAttributes.loadStates();
		Minecraft.getMinecraft().renderEngine.bindTexture(DynamicContainer.texBackgroundBlack);
		gui.drawBasicBackground(guiLeft + getX(), guiTop + getY(), getW(), getH());

		AxisAlignedBB bb = entityLivingBase.getEntityBoundingBox();
		int h = (int) ((bb.maxY - bb.minY) * scale);

		int posX = guiLeft + getX() + getW() / 2;
		int posY = guiTop + getY() + getH() - (getH() - h) / 4;
		states.restore();

		int i = gui.mouseX;
		int j = gui.mouseY;

		int v = (int) (scale * entityLivingBase.getEyeHeight());

//		SpecialChat.sendChat(Minecraft.getMinecraft().player, new ChatComponentText("" + i + " - " + j + " = " + (i - posX) + " - " + (j - posY) + "  - " + v));

		GuiInventory.drawEntityOnScreen(
				posX,
				posY,
				scale,
				posX - i,
				posY - v - j,
				entityLivingBase);
		states.restore();
	}
}
