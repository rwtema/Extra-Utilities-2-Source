package com.rwtema.extrautils2.compatibility;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipeList;

import javax.annotation.Nonnull;
import java.util.Random;

public interface VillagerTradeCompat extends EntityVillager.ITradeList {

	static void invokeAddMerchantRecipeBase(EntityVillager.ITradeList tradeList, MerchantRecipeList recipeList, Random random) {
		tradeList.func_179401_a(recipeList, random);
	}

	void addMerchantRecipeBase(MerchantRecipeList recipeList, Random random);

	@Override
	default void func_179401_a(@Nonnull MerchantRecipeList merchantRecipes, @Nonnull Random random) {
		addMerchantRecipeBase(merchantRecipes, random);
	}
}
