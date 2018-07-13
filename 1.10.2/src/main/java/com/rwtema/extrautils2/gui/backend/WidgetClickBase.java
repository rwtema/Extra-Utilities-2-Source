package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetClickBase extends WidgetBase implements IWidgetMouseInput, IWidgetClientNetwork {
	@SideOnly(Side.CLIENT)
	protected boolean mouseOver;

	@SideOnly(Side.CLIENT)
	protected boolean hover;

	public WidgetClickBase(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseClicked(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
		if (mouseOver)
			this.mouseOver = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseReleased(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
		if (this.mouseOver) {
			if (mouseOver) {
				sendClick(mouseButton);
			}
			this.mouseOver = false;
		}
	}

	@SideOnly(Side.CLIENT)
	public void sendClick(int mouseButton) {
		XUPacketBuffer pkt = getPacketToSend(mouseButton);
		if (pkt == null) return;
		container.sendInputPacket(this, pkt);
	}

	@SideOnly(Side.CLIENT)
	public abstract XUPacketBuffer getPacketToSend(int mouseButton);

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseTick(int mouseX, int mouseY, boolean mouseOver) {
		hover = mouseOver;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastMove, boolean mouseOver) {

	}

	@Override
	public void mouseWheelScroll(int delta, boolean mouseOver) {

	}

	@Override
	public boolean usesMouseWheel() {
		return false;
	}
}
