package com.rwtema.extrautils2.network;

import net.minecraftforge.fml.relauncher.Side;

public abstract class XUPacketServerToClient extends XUPacketBase {
	@SuppressWarnings("unused")
	public XUPacketServerToClient() {

	}

	@Override
	public final Runnable doStuffServer() {
		throw new RuntimeException("Wrong Side");
	}

	@Override
	public final boolean isValidSenderSide(Side properSenderSide) {
		return properSenderSide.isServer();
	}
}
