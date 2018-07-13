package com.rwtema.extrautils2.eventhandlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;

public class ItemCaptureHandler {
	static ItemCaptureHandler INSTANCE = new ItemCaptureHandler();
	static ThreadLocal<LinkedList<ItemStack>> capturing = new ThreadLocal<>();

	static {
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	public static void startCapturing() {
		if (capturing.get() != null) {
			throw new IllegalStateException();
		}
		capturing.set(new LinkedList<>());
	}

	public static LinkedList<ItemStack> stopCapturing() {
		LinkedList<ItemStack> list = capturing.get();
		if (list == null)
			throw new IllegalStateException();
		capturing.set(null);
		return list;
	}

	@SubscribeEvent
	public void onItemJoin(EntityJoinWorldEvent event) {
		LinkedList<ItemStack> list = capturing.get();
		if (list == null) return;

		Entity entity = event.getEntity();
		if (entity instanceof EntityItem) {
			ItemStack stack = ((EntityItem) entity).getItem();
			list.add(stack);
			event.setCanceled(true);
		}
	}
}
