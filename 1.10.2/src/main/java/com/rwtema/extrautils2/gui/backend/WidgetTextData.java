package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetTextData extends WidgetText implements IWidgetServerNetwork {
	public WidgetTextData(int x, int y, int w, int h, int align, int color) {
		super(x, y, w, h, align, color, null);
	}


	public WidgetTextData(int x, int y, int w) {
		super(x, y, null, w);
	}

	public WidgetTextData(int x, int y, int w, int h) {
		super(x, y, w, h, 1, 0x404040, null);
	}

	@Override
	public abstract void addToDescription(XUPacketBuffer packet);

	@Override
	@SideOnly(Side.CLIENT)
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		msg = constructText(packet);
	}

	@SideOnly(Side.CLIENT)
	protected abstract String constructText(XUPacketBuffer packet);

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		if (msg == null) return;

		int x = getX() + ((1 - align) * (getW() - gui.getFontRenderer().getStringWidth(getMsgClient()))) / 2;
		gui.getFontRenderer().drawSplitString(msg, guiLeft + x, guiTop + getY(), getW(), 4210752);
		manager.bindTexture(gui.getWidgetTexture());
	}
}
