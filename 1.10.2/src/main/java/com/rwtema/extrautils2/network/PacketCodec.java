package com.rwtema.extrautils2.network;


import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@ChannelHandler.Sharable
public class PacketCodec extends FMLIndexedMessageToMessageCodec<XUPacketBase> {
	public static HashMap<String, Class<? extends XUPacketBase>> classes = new HashMap<>();

	public PacketCodec() {
		ArrayList<String> t = new ArrayList<>(classes.keySet());

		Collections.sort(t);

		for (int i = 0; i < t.size(); i++) {
			LogHelper.fine("Registering Packet class " + t.get(i) + " with discriminator: " + i);
			addDiscriminator(i, classes.get(t.get(i)));
		}
	}


	@SuppressWarnings("unchecked")
	public static void addClass(Class clazz) {
		classes.put(clazz.getSimpleName(), clazz);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, XUPacketBase msg, ByteBuf target) throws Exception {
		LogHelper.oneTimeInfo("Encode Packet: " + msg.getClass().getName());
		INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		msg.data = target;
		msg.writeData();
		msg.data = null;
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, XUPacketBase msg) {
		LogHelper.oneTimeInfo("Decode Packet: " + msg.getClass().getName());
		INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		EntityPlayer player = ExtraUtils2.proxy.getPlayerFromNetHandler(netHandler);
		msg.data = source;
		if (PlayerHelper.isThisPlayerACheatyBastardOfCheatBastardness(player))
			msg.callback = player;
		msg.readData(player);
		msg.data = null;
	}
}
