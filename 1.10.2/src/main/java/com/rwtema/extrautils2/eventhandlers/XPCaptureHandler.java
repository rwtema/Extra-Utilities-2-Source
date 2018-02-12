package com.rwtema.extrautils2.eventhandlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;

public class XPCaptureHandler {
	static XPCaptureHandler INSTANCE = new XPCaptureHandler();
	static ThreadLocal<Integer> capturing = new ThreadLocal<>();

	static {
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	public static void startCapturing() {
		if (capturing.get() != null) {
			throw new IllegalStateException();
		}
		capturing.set(0);
	}

	public static int stopCapturing() {
		Integer list = capturing.get();
		if (list == null)
			throw new IllegalStateException();
		capturing.set(null);
		return list;
	}

	@SubscribeEvent
	public void onItemJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof EntityXPOrb) {
			Integer curXP = capturing.get();
			if (curXP == null) return;

			int xpValue = ((EntityXPOrb) entity).xpValue;
			capturing.set(curXP + xpValue);
			event.setCanceled(true);
		}
	}
}
