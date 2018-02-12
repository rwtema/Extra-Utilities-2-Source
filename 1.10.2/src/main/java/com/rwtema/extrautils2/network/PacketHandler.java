package com.rwtema.extrautils2.network;

import com.google.common.base.Throwables;
import com.rwtema.extrautils2.utils.LogHelper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

@ChannelHandler.Sharable
public class PacketHandler extends SimpleChannelInboundHandler<XUPacketBase> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, XUPacketBase msg) throws Exception {

		Side effectiveSide = FMLCommonHandler.instance().getEffectiveSide();
		LogHelper.oneTimeInfo("Handle Packet: " + msg.getClass().getName() + " : " + effectiveSide);

		msg.loadAdditonalData(Side.SERVER, ctx);

		Runnable runnableToSchedule = null;
		final EntityPlayer callback = msg.callback;
		try {
			runnableToSchedule = msg.doStuffServer();
		} catch (Throwable throwable) {
			LogHelper.oneTimeInfo("Error Packet: " + msg.getClass().getName() + " : " + effectiveSide);
			if (callback != null)
				NetworkHandler.sendCrash(throwable, callback);
			throw Throwables.propagate(throwable);
		}

		if (runnableToSchedule != null) {
			LogHelper.oneTimeInfo("Scheduling Packet: " + msg.getClass().getName() + " : " + effectiveSide);

			if (callback == null) {
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnableToSchedule);
			} else {
				LogHelper.oneTimeInfo("Scheduling Callback Packet: " + msg.getClass().getName() + " : " + effectiveSide);
				final Runnable finalRunnableToSchedule = runnableToSchedule;
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						try {
							finalRunnableToSchedule.run();
						} catch (Throwable throwable) {
							NetworkHandler.sendCrash(throwable, callback);
							throw Throwables.propagate(throwable);
						}
					}
				});
			}
		}
	}
}

