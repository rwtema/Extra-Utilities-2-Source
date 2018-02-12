package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;

import java.util.List;

public abstract class WidgetProgressArrowNetworkBase extends WidgetProgressArrowBase implements IWidgetServerNetwork {

	public WidgetProgressArrowNetworkBase(int x, int y) {
		super(x, y);
	}

	public static byte getAdjustedWidth(float t) {
		int a = t <= 0 ? 0 : t >= 1 ? ARROW_WIDTH : 1 + Math.round(t * (ARROW_WIDTH - 1));
		return (byte) a;
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		curWidth = readProgressData(packet);
	}

	protected abstract byte readProgressData(XUPacketBuffer packet);

	@Override
	public void addToDescription(XUPacketBuffer packet) {
		writeProgressData(packet);
	}

	protected abstract void writeProgressData(XUPacketBuffer packet);


	@Override
	public List<String> getToolTip() {
		if (curWidth == -1) return getErrorMessage();
		return null;
	}

}
