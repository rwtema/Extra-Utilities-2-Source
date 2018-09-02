package com.rwtema.extrautils2.quarry;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.tile.TilePower;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class TileTreasure extends TilePower implements ITickable {
	public static final ResourceLocation CHESTS_RECORDS = new ResourceLocation(ExtraUtils2.MODID, "chests/records");
	public static final Map<String, Integer> LOCATIONS_AND_WEIGHTS = ImmutableMap.<String, Integer>builder()
			.put("chests/simple_dungeon", 20)
			.put("chests/village_blacksmith", 10)
			.put("chests/abandoned_mineshaft", 10)

			.put("chests/spawn_bonus_chest", 5)
			.put("chests/desert_pyramid", 5)
			.put("chests/igloo_chest", 5)
			.put("chests/jungle_temple", 5)

			.put("chests/stronghold_library", 2)
			.put("chests/stronghold_crossing", 2)
			.put("chests/stronghold_corridor", 2)
			.put("chests/woodland_mansion", 2)

			.put("chests/end_city_treasure", 1)
			.put("chests/nether_bridge", 1)
			.put(CHESTS_RECORDS.toString(), 1)
			.build();


	static {
		MinecraftForge.EVENT_BUS
				.register(new Object() {
					@SubscribeEvent
					public void registerLoot(LootTableLoadEvent event) {
						if (CHESTS_RECORDS.equals(event.getName())) {
							LootEntry[] lootEntries = OreDictionary.getOres("record").stream()
									.map(ItemStack::getItem)
									.map(i -> new LootEntryItem(i, 1, 0, new LootFunction[0], new LootCondition[0], i.getUnlocalizedName()))
									.toArray(LootEntry[]::new);

							event.getTable().addPool(new LootPool(lootEntries, new LootCondition[0], new RandomValueRange(1, 3), new RandomValueRange(0, 0), "records"));
						}
					}
				});
	}


	public static <T> T getRandomValueFromWeightedMap(Random rand, Map<T, Integer> map) {
		int totalWeight = map.values().stream().mapToInt(Integer::intValue).sum();
		int n = rand.nextInt(totalWeight);
		T s = null;
		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			s = entry.getKey();
			n -= entry.getValue();
			if (n <= 0) break;
		}
		return s;
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float getPower() {
		return 0;
	}

	@Override
	public void update() {
		String s = getRandomValueFromWeightedMap(world.rand, LOCATIONS_AND_WEIGHTS);
		LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(s));
		LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer)this.world);
		lootcontext$builder.withLuck(0);
		lootTable.generateLootForPools(world.rand, lootcontext$builder.build() );
	}
}
