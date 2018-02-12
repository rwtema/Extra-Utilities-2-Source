package com.rwtema.extrautils2.eventhandlers;

import com.google.common.collect.HashMultimap;
import com.rwtema.extrautils2.utils.XURandom;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class DropsHandler {
	public static HashMultimap<IBlockState, Pair<ItemStack, Double>> drops2add = HashMultimap.create();

	static {
		MinecraftForge.EVENT_BUS.register(new DropsHandler());
	}

	public static void registerDrops(IBlockState state, ItemStack stack, double propability) {
		DropsHandler.drops2add.put(state, Pair.of(stack, propability));
	}

	@SubscribeEvent
	public void onDrop(BlockEvent.HarvestDropsEvent event) {
		if (event.isSilkTouching()) return;
		Set<Pair<ItemStack, Double>> pairs = drops2add.get(event.getState());
		if (pairs != null) {
			for (Pair<ItemStack, Double> pair : pairs) {
				double probability = pair.getRight() * event.getDropChance();
				while (XURandom.rand.nextDouble() < probability) {
					event.getDrops().add(pair.getLeft().copy());
				}
			}
		}
	}

	public static HashMultimap<ResourceLocation,LootPool> lootDrops = HashMultimap.create();

	@SubscribeEvent
	public void registerLoot(LootTableLoadEvent event){
		Set<LootPool> stacks = lootDrops.get(event.getName());
		if(stacks.isEmpty()) return;
		for (LootPool pool : stacks) {
			event.getTable().addPool(pool);
		}
	}
}
