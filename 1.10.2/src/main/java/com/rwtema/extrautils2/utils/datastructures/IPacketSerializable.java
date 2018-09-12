package com.rwtema.extrautils2.utils.datastructures;

import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketBuffer;

public interface IPacketSerializable {
	void writeToPacket(XUPacketBuffer t);
	void readFromPacket(XUPacketBuffer t);
}
