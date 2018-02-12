package com.rwtema.extrautils2.gui.backend;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.utils.helpers.StringHelper;

import java.text.NumberFormat;
import java.util.List;

public abstract class WidgetProgressArrowTimer extends WidgetProgressArrowNetworkBase {
	float clientTime;
	float clientMaxTime;

	public WidgetProgressArrowTimer(int x, int y) {
		super(x, y);
	}

	protected abstract float getTime();

	protected abstract float getMaxTime();

	@Override
	protected byte readProgressData(XUPacketBuffer packet) {
		clientTime = packet.readFloat();
		clientMaxTime = packet.readFloat();
		if (clientMaxTime < 0) return -1;
		if (clientTime == 0) return 0;
		return getAdjustedWidth(clientTime / clientMaxTime);
	}

	@Override
	protected void writeProgressData(XUPacketBuffer packet) {
		packet.writeFloat(getTime());
		packet.writeFloat(getMaxTime());
	}

	@Override
	public List<String> getToolTip() {
		if (curWidth == -1) {
			return getErrorMessage();
		}
		int clientMaxTime = (int) this.clientMaxTime;
		if (clientMaxTime == 0) return null;
		return ImmutableList.of(
				String.format("%s / %s ", StringHelper.formatDurationSeconds((int) clientTime, true), StringHelper.formatDurationSeconds(clientMaxTime, false)),
				ChatFormatting.GRAY + NumberFormat.getPercentInstance().format(clientTime / this.clientMaxTime) + ChatFormatting.RESET
		);
	}
}
