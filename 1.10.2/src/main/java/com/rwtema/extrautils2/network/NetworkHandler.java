package com.rwtema.extrautils2.network;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumMap;
import java.util.Map;

public class NetworkHandler {

	public static EnumMap<Side, FMLEmbeddedChannel> channels;

	static {
		LogHelper.oneTimeInfo("Network Handler Static Init");
	}

	public static void init(ASMDataTable asmData) {
		for (ASMDataTable.ASMData data : asmData.getAll(XUPacket.class.getName())) {
			registerPacket(data.getClassName());
		}

		channels = NetworkRegistry.INSTANCE.newChannel("XU2", new PacketCodec(), ExtraUtils2.proxy.getNewPacketHandler());

		LogHelper.oneTimeInfo("Network Init");
		LogHelper.oneTimeInfo("Start " + channels);
		for (Map.Entry<Side, FMLEmbeddedChannel> entry : channels.entrySet()) {
			FMLEmbeddedChannel value = entry.getValue();
			if (value != null) {
				Side side = entry.getKey();
				LogHelper.oneTimeInfo("Start Side:  " + side + " ");
				Map.Entry[] array = Iterables.toArray(value.pipeline(), Map.Entry.class);
				for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
					Map.Entry handlerEntry = array[i];
					LogHelper.oneTimeInfo("Start Channel Handler: (" + side + ")(" + i + ") " + handlerEntry.getKey() + " - " + handlerEntry.getValue());
				}
			}
		}
	}

	public static void checkPacket(XUPacketBase packet, Side properSenderSide) {
		LogHelper.oneTimeInfo("Check Packet " + properSenderSide);
		if (!packet.isValidSenderSide(properSenderSide))
			throw new RuntimeException("Sending packet class" + packet.getClass().getSimpleName() + " from wrong side");
	}

	public static void sendToAllPlayers(XUPacketBase packet) {
		checkPacket(packet, Side.SERVER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeOutbound(packet);
	}


	public static void sendPacketToPlayer(XUPacketBase packet, EntityPlayer player) {
		checkPacket(packet, Side.SERVER);
		if (!PlayerHelper.isPlayerReal(player)) return;
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeOutbound(packet);
	}

	public static void sendToAllAround(XUPacketBase packet, int dimension, double x, double y, double z, double range) {
		sendToAllAround(packet, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
	}

	public static void sendToAllAround(XUPacketBase packet, NetworkRegistry.TargetPoint point) {
		checkPacket(packet, Side.SERVER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
		channels.get(Side.SERVER).writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}


	public static void sendPacketToServer(XUPacketBase packet) {
		checkPacket(packet, Side.CLIENT);
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(packet);
	}

	private static void registerPacket(String s) {
		try {
			Class<?> clazz = Class.forName(s);
			if (XUPacketBase.class.isAssignableFrom(clazz)) {
				LogHelper.oneTimeInfo("Register Packet");
				try {
					clazz.newInstance();
				} catch (Throwable throwable) {
					throw Throwables.propagate(throwable);
				}
				PacketCodec.addClass(clazz);
			} else
				throw new RuntimeException("Invalid Class for packet: " + s);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Presented class (" + s + ") missing, FML Bug?", e);
		} catch (NoClassDefFoundError e) {
			throw new RuntimeException(s + " can't be created", e);
		}
	}

	public static void sendCrash(Throwable throwable, EntityPlayer callback) {
		LogHelper.oneTimeInfo("Sending Crash");
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			final String s = sw.toString();
			sendPacketToPlayer(new PacketCrashLog(s), callback);
		} catch (Throwable ignore) {

		}
	}

	public @interface XUPacket {

	}

	@XUPacket
	public static class PacketCrashLog extends XUPacketServerToClient {
		static Logger logger = LogManager.getLogger(ExtraUtils2.MODID + "_Crash");
		String string;

		public PacketCrashLog() {
			super();
		}

		public PacketCrashLog(String string) {
			this.string = string;
		}

		@Override
		public void writeData() throws Exception {
			writeString(string);
		}

		@Override
		public void readData(EntityPlayer player) {
			string = readString();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Runnable doStuffClient() {
			logger.info("Server Packet Crash");
			logger.info(string);
			return null;
		}
	}
}
