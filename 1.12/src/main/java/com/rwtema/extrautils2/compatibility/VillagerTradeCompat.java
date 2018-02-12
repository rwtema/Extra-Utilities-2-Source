package com.rwtema.extrautils2.compatibility;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipeList;

import javax.annotation.Nonnull;
import java.util.Random;

public interface VillagerTradeCompat extends EntityVillager.ITradeList {

	static void invokeAddMerchantRecipeBase(EntityVillager.ITradeList tradeList, MerchantRecipeList recipeList, Random random) {
		tradeList.addMerchantRecipe(null, recipeList, random);
	}

	void addMerchantRecipeBase(MerchantRecipeList recipeList, Random random);

	@Override
	default void addMerchantRecipe(@Nonnull IMerchant merchant, @Nonnull MerchantRecipeList recipeList, @Nonnull Random random) {
		addMerchantRecipeBase(recipeList, random);
	}
}
