package com.rwtema.extrautils2.network;


import com.rwtema.extrautils2.utils.LogHelper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ChannelHandler.Sharable
@SideOnly(Side.CLIENT)
public class PacketHandlerClient extends PacketHandler {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, XUPacketBase msg) throws Exception {

		Side side = FMLCommonHandler.instance().getEffectiveSide();
		LogHelper.oneTimeInfo("Handle Packet: " + msg.getClass().getName() + " : " + side);
		msg.loadAdditonalData(side, ctx);

		if (side == Side.SERVER) {
			Runnable runnableToSchedule = msg.doStuffServer();
			if (runnableToSchedule != null)
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnableToSchedule);
		} else {
			Runnable runnable = msg.doStuffClient();
			if (runnable != null)
				Minecraft.getMinecraft().addScheduledTask(runnable);
		}

	}
}

