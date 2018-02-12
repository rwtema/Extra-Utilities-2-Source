package com.rwtema.extrautils2.villagers;

import com.rwtema.extrautils2.compatibility.VillagerTradeCompat;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipeList;
import org.apache.commons.lang3.Validate;

import java.util.*;

public class RandomTradeList implements VillagerTradeCompat {

	List<EntityVillager.ITradeList> tradeList = new ArrayList<>();

	public RandomTradeList(EntityVillager.ITradeList... trades) {
		Validate.noNullElements(trades);
		Collections.addAll(tradeList, trades);
	}

	public RandomTradeList(Collection<EntityVillager.ITradeList> trades) {
		Validate.noNullElements(trades);
		tradeList.addAll(trades);
	}

	@Override
	public void addMerchantRecipeBase(MerchantRecipeList recipeList, Random random) {
		if (tradeList.isEmpty()) return;
		int i = random.nextInt(tradeList.size());
		VillagerTradeCompat.invokeAddMerchantRecipeBase(tradeList.get(i), recipeList, random);
	}
}
