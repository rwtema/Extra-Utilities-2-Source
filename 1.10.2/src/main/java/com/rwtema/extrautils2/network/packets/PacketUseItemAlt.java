package com.rwtema.extrautils2.network.packets;

import com.rwtema.extrautils2.keyhandler.KeyAlt;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.network.XUPacketClientToServer;
import net.minecraft.entity.player.EntityPlayer;

@NetworkHandler.XUPacket
public class PacketUseItemAlt extends XUPacketClientToServer {
	private boolean sprint;
	private EntityPlayer player;

	public PacketUseItemAlt() {
		super();
	}

	public PacketUseItemAlt(boolean sprint) {
		super();
		this.sprint = sprint;
	}

	@Override
	public void writeData() throws Exception {
		writeBoolean(sprint);
	}

	@Override
	public void readData(EntityPlayer player) {
		sprint = readBoolean();
		this.player = player;
	}

	@Override
	public Runnable doStuffServer() {
		return new Runnable() {
			@Override
			public void run() {
				KeyAlt.setValue(player, sprint);
			}
		};
	}
}
