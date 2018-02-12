package com.rwtema.extrautils2.eventhandlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SlimeSpawnHandler {
	public static void init() {
		MinecraftForge.EVENT_BUS.register(new SlimeSpawnHandler());
	}

	@SubscribeEvent
	public void preventSlime(LivingSpawnEvent.CheckSpawn event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof EntitySlime)) return;

		World worldObj = entity.world;
		if (worldObj != null && worldObj.getWorldType() == WorldType.FLAT) {
			event.setResult(Event.Result.DENY);
		}
	}
}
