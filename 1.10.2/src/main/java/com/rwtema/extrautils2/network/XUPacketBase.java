package com.rwtema.extrautils2.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class XUPacketBase extends XUPacketBuffer {
	EntityPlayer callback;

	@SuppressWarnings("unused")
	public XUPacketBase() {
		super(null);
	}

	public abstract void writeData() throws
			Exception;

	public abstract void readData(EntityPlayer player);

	public abstract Runnable doStuffServer();

	@SideOnly(Side.CLIENT)
	public abstract Runnable doStuffClient();

	public abstract boolean isValidSenderSide(Side properSenderSide);

	public void loadAdditonalData(Side server, ChannelHandlerContext ctx) {

	}
}
