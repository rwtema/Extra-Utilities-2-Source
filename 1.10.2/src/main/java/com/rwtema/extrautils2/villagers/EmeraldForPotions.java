package com.rwtema.extrautils2.villagers;

import com.google.common.collect.HashMultimap;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.VillagerTradeCompat;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.common.brewing.AbstractBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EmeraldForPotions implements VillagerTradeCompat {
	private static final TObjectIntHashMap<Item> potionItemCost;
	private static final Item[] potionItems = new Item[]{
			Items.POTIONITEM,
			Items.POTIONITEM,
			Items.POTIONITEM,
			Items.POTIONITEM,
			Items.SPLASH_POTION,
			Items.SPLASH_POTION,
			Items.LINGERING_POTION
	};
	static TObjectIntHashMap<PotionType> potionLevels;
	static int size = 0;

	static {
		potionItemCost = new TObjectIntHashMap<>();
		potionItemCost.put(Items.POTIONITEM, 0);
		potionItemCost.put(Items.SPLASH_POTION, 2);
		potionItemCost.put(Items.LINGERING_POTION, 8);
	}


	final boolean buyingPotions;

	public EmeraldForPotions(boolean buyingPotions) {
		this.buyingPotions = buyingPotions;
	}

	public static TObjectIntHashMap<PotionType> getCraftLevels() {
		List<IBrewingRecipe> recipes = BrewingRecipeRegistry.getRecipes();

		TObjectIntHashMap<PotionType> potionLevels = EmeraldForPotions.potionLevels;

		if (potionLevels != null && size == recipes.size()) return potionLevels;

		potionLevels = new TObjectIntHashMap<>();

		potionLevels.put(PotionTypes.EMPTY, 0);
		potionLevels.put(PotionTypes.WATER, 0);


		HashMultimap<PotionType, PotionType> potionChildren = HashMultimap.create();


		for (PotionHelper.MixPredicate<PotionType> predicate : PotionHelper.POTION_TYPE_CONVERSIONS) {
			potionChildren.put(CompatHelper112.getPotionInput(predicate), CompatHelper112.getPotionOutput(predicate));
		}

		for (IBrewingRecipe recipe : recipes) {
			if (recipe instanceof AbstractBrewingRecipe) {
				AbstractBrewingRecipe brewingRecipe = (AbstractBrewingRecipe) recipe;
				if (!PotionHelper.IS_POTION_ITEM.apply(brewingRecipe.getInput())) {
					continue;
				}
				PotionType typeInput = PotionUtils.getPotionFromItem(brewingRecipe.getInput());

				if (!PotionHelper.IS_POTION_ITEM.apply(brewingRecipe.getOutput())) {
					continue;
				}

				PotionType typeOutput = PotionUtils.getPotionFromItem(brewingRecipe.getInput());

				potionChildren.put(typeInput, typeOutput);
			}
		}

		LinkedList<PotionType> toProcess = new LinkedList<>();
		toProcess.add(PotionTypes.WATER);
		PotionType type;
		while ((type = toProcess.poll()) != null) {
			int i = potionLevels.get(type);
			for (PotionType child : potionChildren.get(type)) {
				if (potionLevels.containsKey(child))
					continue;
				potionLevels.put(child, i + 1);
				toProcess.add(child);
			}
		}

		potionLevels.retainEntries(new TObjectIntProcedure<PotionType>() {
			@Override
			public boolean execute(PotionType a, int b) {
				return b > 0;
			}
		});

		EmeraldForPotions.potionLevels = potionLevels;
		size = recipes.size();

		return potionLevels;
	}

	@Override
	public void addMerchantRecipeBase(MerchantRecipeList recipeList, Random random) {
		TObjectIntHashMap<PotionType> craftLevels = getCraftLevels();
		PotionType potionType = CollectionHelper.getRandomElement(craftLevels.keySet(), random);

		Item potionItem = potionItems[random.nextInt(potionItems.length)];
		ItemStack result = PotionUtils.addPotionToItemStack(new ItemStack(potionItem), potionType);

		int level = craftLevels.get(potionType);
		if (buyingPotions) {
			int i = (level >> 1);
			if (i == 0) return;
			i = i + random.nextInt(i) + (potionItemCost.get(potionItem) >> 1);
			if (i > 0) {
				recipeList.add(new MerchantRecipe(result, new ItemStack(Items.EMERALD, i)));
			}
		} else {
			int i = 1 + level;
			i = i + random.nextInt(i) + potionItemCost.get(potionItem);
			recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, i), result));
		}
	}
}
