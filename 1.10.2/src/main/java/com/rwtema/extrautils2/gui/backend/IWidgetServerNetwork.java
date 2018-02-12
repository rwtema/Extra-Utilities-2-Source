package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IWidgetServerNetwork extends IWidget {
	void addToDescription(XUPacketBuffer packet);

	@SideOnly(Side.CLIENT)
	void handleDescriptionPacket(XUPacketBuffer packet);
}
