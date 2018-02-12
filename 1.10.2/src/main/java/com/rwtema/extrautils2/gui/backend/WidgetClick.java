package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import javax.annotation.Nonnull;

public abstract class WidgetClick extends WidgetClickBase {

	public WidgetClick(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	@Override
	public void receiveClientPacket(XUPacketBuffer buffer) {
		byte b = buffer.readByte();
		onClick(b);
	}

	public abstract void onClick(byte b);

	@Override
	@Nonnull
	public XUPacketBuffer getPacketToSend(int mouseButton) {
		XUPacketBuffer pkt = new XUPacketBuffer();
		pkt.writeByte(mouseButton);
		return pkt;
	}

}
