package com.rwtema.extrautils2.utils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MCTimer {
	public static int clientTimer;
	public static int frameTimer;
	public static float renderTimer;
	public static float renderPartialTickTime;
	public static int serverTimer;
	public static float deltaTime = 1;
	private static long lastServerTickTime = -1;

	static {
		MinecraftForge.EVENT_BUS.register(new MCTimer());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void serverTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START) {
			return;
		}
		serverTimer++;
//
//		if (lastServerTickTime != -1) {
//lastServerTickTime = MinecraftServer.getCurrentTimeMillis();
//		} else
//			lastServerTickTime = MinecraftServer.getCurrentTimeMillis();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) clientTimer++;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderTick(TickEvent.RenderTickEvent event) {
		renderPartialTickTime = event.renderTickTime;
		renderTimer = clientTimer + renderPartialTickTime;
		if (event.phase == TickEvent.Phase.START) {
			frameTimer++;
		}
	}


}
