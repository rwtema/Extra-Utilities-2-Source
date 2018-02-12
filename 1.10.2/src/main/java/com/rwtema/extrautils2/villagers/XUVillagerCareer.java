package com.rwtema.extrautils2.villagers;

import com.google.common.collect.ImmutableList;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class XUVillagerCareer extends VillagerRegistry.VillagerCareer {
	private TIntObjectHashMap<List<EntityVillager.ITradeList>> randomLists = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<EntityVillager.ITradeList>> detLists = new TIntObjectHashMap<>();
	private EntityVillager.ITradeList[][] trades;

	public XUVillagerCareer(VillagerRegistry.VillagerProfession parent, String name) {
		super(parent, name);
	}

	public XUVillagerCareer addRandomTrade(int i, EntityVillager.ITradeList... tradeList) {
		trades = null;
		Collections.addAll(getList(i, this.randomLists), tradeList);
		return this;
	}

	public XUVillagerCareer addAdditionalTrade(int i, EntityVillager.ITradeList... tradeList) {
		trades = null;
		Collections.addAll(getList(i, this.detLists), tradeList);
		return this;
	}

	private List<EntityVillager.ITradeList> getList(int i, TIntObjectHashMap<List<EntityVillager.ITradeList>> listMap) {
		List<EntityVillager.ITradeList> list = listMap.get(i);
		if (list == null) listMap.put(i, list = new ArrayList<>());
		return list;
	}

	@Override
	public List<EntityVillager.ITradeList> getTrades(int level) {
		ImmutableList.Builder<EntityVillager.ITradeList> builder = ImmutableList.builder();
		List<EntityVillager.ITradeList> list1 = randomLists.get(level);
		List<EntityVillager.ITradeList> list2 = detLists.get(level);
		if(list1 != null){
			builder.add(new RandomTradeList(list1));
		}
		if(list2 != null){
			builder.addAll(list2);
		}
		return builder.build();
	}

}
