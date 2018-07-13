package com.rwtema.extrautils2.villagers;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.VillagerTradeCompat;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import javax.annotation.Nonnull;
import java.util.Random;

public class GenericTrade implements VillagerTradeCompat {
	public static final ItemStack EMERALDS = new ItemStack(Items.EMERALD);
	final public ItemStack toBuy;
	final public ItemStack toBuySecond;
	final public ItemStack toSell;
	final int toBuyL, toBuyU;
	final int toBuySecondL, toBuySecondU;
	final int toSellL, toSellU;
	public int numToolUses = 0;
	public int maxTrades = 7;
	private boolean rewardsExp;

	public GenericTrade(ItemStack toBuy, ItemStack toSell) {
		this(toBuy, StackHelper.getStacksize(toBuy), StackHelper.getStacksize(toBuy), toSell);
	}

	public GenericTrade(ItemStack toBuy, int toBuyNo, ItemStack toSell) {
		this(toBuy, toBuyNo, toBuyNo, toSell);
	}

	public GenericTrade(ItemStack toBuy, int toBuyL, int toBuyU, ItemStack toSell) {
		this(toBuy, toBuyL, toBuyU, toSell, StackHelper.getStacksize(toSell), StackHelper.getStacksize(toSell));
	}


	public GenericTrade(ItemStack toBuy, ItemStack toSell, int toSellNo) {
		this(toBuy, toSell, toSellNo, toSellNo);
	}

	public GenericTrade(ItemStack toBuy, ItemStack toSell, int toSellL, int toSellU) {
		this(toBuy, StackHelper.getStacksize(toBuy), StackHelper.getStacksize(toBuy), toSell, toSellL, toSellU);
	}

	public GenericTrade(ItemStack toBuy, int toBuyNo, ItemStack toSell, int toSellNo) {
		this(toBuy, toBuyNo, toBuyNo, toSell, toSellNo, toSellNo);
	}

	public GenericTrade(ItemStack toBuy, int toBuyL, int toBuyU, ItemStack toSell, int toSellL, int toSellU) {
		this(toBuy, toBuyL, toBuyU, null, 0, 0, toSell, toSellL, toSellU);
	}

	public GenericTrade(ItemStack toBuy, int toBuyL, int toBuyU, ItemStack toBuySecond, int toBuySecondL, int toBuySecondU, ItemStack toSell, int toSellL, int toSellU) {
		this.toBuy = toBuy;
		this.toBuyL = toBuyL;
		this.toBuyU = toBuyU;
		this.toBuySecond = toBuySecond;
		this.toBuySecondL = toBuySecondL;
		this.toBuySecondU = toBuySecondU;
		this.toSell = toSell;
		this.toSellL = toSellL;
		this.toSellU = toSellU;
	}

	public GenericTrade setMaxTrades(int maxTrades) {
		this.maxTrades = maxTrades;
		return this;
	}

	@Override
	public void addMerchantRecipeBase(@Nonnull MerchantRecipeList recipeList, @Nonnull Random random) {
		ItemStack buy = generate(toBuy, toBuyL, toBuyU, random);
		ItemStack buySecond = generate(toBuySecond, toBuySecondL, toBuySecondU, random);
		ItemStack sell = generate(toSell, toSellL, toSellU, random);
		MerchantRecipe recipe = createRecipe(buy, buySecond, sell, numToolUses, maxTrades);
		recipeList.add(recipe);
	}

	protected MerchantRecipe createRecipe(ItemStack buy, ItemStack buySecond, ItemStack sell, int numToolUses, int maxTrades) {
		MerchantRecipe merchantRecipe = new MerchantRecipe(buy, buySecond, sell, numToolUses, maxTrades);
		if (!this.rewardsExp) {
			NBTTagCompound tags = merchantRecipe.writeToTags();
			tags.setBoolean("rewardExp", false);
			merchantRecipe.readFromTags(tags);
		}
		return merchantRecipe;
	}

	protected ItemStack generate(ItemStack stack, int lower, int upper, Random rand) {
		if (StackHelper.isNull(stack)) return StackHelper.empty();
		int n;
		if (lower == upper) {
			n = lower;
		} else {
			n = lower + rand.nextInt(upper - lower);
		}
		ItemStack copy = stack.copy();
		StackHelper.setStackSize(copy, n);
		return copy;
	}

}
