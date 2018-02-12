package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;

public interface IWidgetClientNetwork extends IWidget {
	void receiveClientPacket(XUPacketBuffer buffer);
}
