package com.rwtema.extrautils2.villagers;

import java.util.Random;
import javax.annotation.Nonnull;

import com.rwtema.extrautils2.compatibility.VillagerTradeCompat;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipeList;

public class EmeraldTrades implements VillagerTradeCompat {
	final ItemStack base;
	final float cost;

	public EmeraldTrades(ItemStack base, float cost) {
		this.base = base;
		this.cost = cost;
	}


	@Override
	public void addMerchantRecipeBase(MerchantRecipeList recipeList, Random random) {
		int emeraldNo;
		int baseNo;
		if (cost < 1) {
			float dCost = 1 / cost;
			if (Math.ceil(dCost) > base.getMaxStackSize())
				return;
			emeraldNo = 1;
			baseNo = (int) Math.ceil(dCost);
		} else {
			int maxEmerald = (new ItemStack(Items.EMERALD)).getMaxStackSize();
			baseNo = 1;
			emeraldNo = (int) Math.ceil(cost);
			if (emeraldNo > maxEmerald)
				return;
		}


	}

	public float randMult(Random rand) {
		return 1 / (1 + rand.nextFloat());
	}
}
