package com.rwtema.extrautils2.dimensions;

import com.rwtema.extrautils2.utils.XURandom;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

// Sledgehammer to smash a very annoying acorn
public class TeleportCapabilitiesUpdateHandler {
	public static final int TIMER = 20;
	static int timer = 0;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && timer > 0 && XURandom.rand.nextInt(TIMER) < timer) {
			timer--;

			PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
			for (EntityPlayerMP player : playerList.getPlayers()) {
				player.sendPlayerAbilities();
			}
		}
	}


	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone clone) {
		timer = TIMER;
	}

	@SubscribeEvent
	public static void onClone(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
		timer = TIMER;
	}

	@SubscribeEvent
	public static void onClone(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
		timer = TIMER;
	}

	@SubscribeEvent
	public static void onLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		timer = TIMER;
	}

}
