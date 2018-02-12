package com.rwtema.extrautils2.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class XUPacketClientToServer extends XUPacketBase {
	public XUPacketClientToServer() {
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public final Runnable doStuffClient() {
		throw new RuntimeException("Wrong Side");
	}

	@Override
	public final boolean isValidSenderSide(Side properSenderSide) {
		return properSenderSide.isClient();
	}
}
