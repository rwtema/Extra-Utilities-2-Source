package com.rwtema.extrautils2.dimensions;

import com.rwtema.extrautils2.power.PowerManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

// Sledgehammer to smash a very annoying acorn
public class TeleportCapabilitiesUpdateHandler {
	public static final int MAX_COUNTER = 20;
	static int counter = MAX_COUNTER;
	static int timer = 0;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && counter < MAX_COUNTER) {
			if (timer >= counter) {
				timer = 0;
				counter++;

				PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
				for (EntityPlayerMP player : playerList.getPlayers()) {
					player.sendPlayerAbilities();
				}

				synchronized (PowerManager.MUTEX) {
					for (PowerManager.PowerFreq powerFreq : PowerManager.instance.frequencyHolders.valueCollection()) {
						powerFreq.dirty = true;
						powerFreq.quickRefresh();
					}
				}
			} else {
				timer++;
			}
		}
	}


	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone clone) {
		setCounter();
	}

	@SubscribeEvent
	public static void onClone(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
		setCounter();
	}

	@SubscribeEvent
	public static void onClone(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
		setCounter();
	}

	@SubscribeEvent
	public static void onLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		setCounter();
	}

	private static void setCounter() {
		counter = 0;
	}
}
