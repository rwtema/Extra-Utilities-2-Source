package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class WidgetScrollBarServer extends WidgetScrollBar implements IWidgetServerNetwork, IWidgetClientNetwork {
	int updateLevel;
	private int minValue;
	private int maxValue;

	public WidgetScrollBarServer(int x, int y, int h, int minValue, int maxValue) {
		super(x, y, h, minValue, maxValue);


		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	protected void onChange() {
		updateLevel++;
		XUPacketBuffer packetBuffer = new XUPacketBuffer();
		packetBuffer.writeInt(scrollValue);
		container.sendInputPacket(this, packetBuffer);
	}

	public abstract int getValueServer();

	public abstract void setValueServer(int level);

	@Override
	public void receiveClientPacket(XUPacketBuffer buffer) {
		int newLevel = MathHelper.clamp(buffer.readInt(), minValue, maxValue);

		if (newLevel != getValueServer()) {
			updateLevel++;
			setValueServer(newLevel);
		}
	}

	@Override
	public void addToDescription(XUPacketBuffer packet) {
		packet.writeInt(getValueServer());
		packet.writeInt(updateLevel);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		int newLevel = packet.readInt();
		int newUpdateLevel = packet.readInt();

		if (newUpdateLevel >= updateLevel) {
			setValue(newLevel);
		}
	}


}
