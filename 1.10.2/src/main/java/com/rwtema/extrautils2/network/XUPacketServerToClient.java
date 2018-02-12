package com.rwtema.extrautils2.network;

import com.rwtema.extrautils2.asm.ModAdv;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.OverridingMethodsMustInvokeSuper;

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
