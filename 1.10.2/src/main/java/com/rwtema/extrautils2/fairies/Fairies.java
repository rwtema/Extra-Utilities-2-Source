package com.rwtema.extrautils2.fairies;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Fairies {
	static final WeakHashMap<World, List<Fairy>> fairies = new WeakHashMap<>();

	static {
		MinecraftForge.EVENT_BUS.register(Fairies.class);
	}

	public static void register(World world, Fairy fairy) {
		fairies.computeIfAbsent(world, world1 -> new LinkedList<>()).add(fairy);
		fairy.joinedWorld = true;
	}

	@SubscribeEvent
	public static void tick(TickEvent.WorldTickEvent event) {
		List<Fairy> list = Fairies.fairies.get(event.world);
		if (list != null) {
			for (Iterator<Fairy> iterator = list.iterator(); iterator.hasNext(); ) {
				Fairy fairy = iterator.next();
				if (fairy.dead) {
					iterator.remove();
				} else {
					World world = event.world;
					if (world instanceof WorldServer) {
						((WorldServer) world).spawnParticle(EnumParticleTypes.REDSTONE, fairy.pos.x, fairy.pos.y, fairy.pos.z, 1, 0D, 0D, 0D, 0.0D);
					}
				}
			}
		}
	}
}
