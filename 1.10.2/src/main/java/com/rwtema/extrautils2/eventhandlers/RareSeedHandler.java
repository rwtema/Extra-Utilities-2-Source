package com.rwtema.extrautils2.eventhandlers;

import com.rwtema.extrautils2.utils.XURandom;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class RareSeedHandler {

	private static final List<WeightedRandom.Item> seedEntries = ObfuscationReflectionHelper.getPrivateValue(ForgeHooks.class, null, "seedList");

	private static final TObjectDoubleHashMap<WeightedRandom.Item> probabilities = new TObjectDoubleHashMap<>();

	static {
		MinecraftForge.EVENT_BUS.register(new RareSeedHandler());
	}

	public static void register(ItemStack seed, double probability) {
		int p = -Math.abs(XURandom.rand.nextInt() | 22511);
		MinecraftForge.addGrassSeed(seed, p);

		WeightedRandom.Item item = null;
		for (WeightedRandom.Item seedEntry : seedEntries) {
			if (seedEntry.itemWeight == p) {
				if (item != null) throw new RuntimeException("Unable to register seed entry");
				item = seedEntry;
			}
		}
		if (item == null) throw new RuntimeException("Unable to register seed entry");

		item.itemWeight = 0;

		probabilities.put(item, probability);
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		recalcProb();
	}

	@SubscribeEvent
	public void onDrops(BlockEvent.HarvestDropsEvent event) {
		recalcProb();
	}

	public void recalcProb() {
		probabilities.forEachEntry(new TObjectDoubleProcedure<WeightedRandom.Item>() {
			@Override
			public boolean execute(WeightedRandom.Item a, double b) {
				a.itemWeight = 0;
				return true;
			}
		});

		int i = 0;

		//noinspection ForLoopReplaceableByForEach
		for (int i1 = 0; i1 < seedEntries.size(); i1++) {
			WeightedRandom.Item entry = seedEntries.get(i1);
			i += entry.itemWeight;
		}

		final int totalWeight = i;

		probabilities.forEachEntry(new TObjectDoubleProcedure<WeightedRandom.Item>() {
			@Override
			public boolean execute(WeightedRandom.Item a, double b) {

				double k = b * totalWeight;
				int w = (int) Math.floor(k);
				double p = k - w;
				if (XURandom.rand.nextDouble() < p) {
					w++;
				}
				a.itemWeight = w;
				return true;
			}
		});
	}
}
