package com.rwtema.extrautils2.machine;


import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.AbstractBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BrewingEnergyRecipe extends EnergyBaseRecipe {
	private HashMap<PotionType, Integer> types = new HashMap<>();
	private HashMap<Item, Integer> potions = new HashMap<>();
	private int prevSize = -1;

	public void checkTypes() {
		int curSize = PotionType.REGISTRY.getKeys().size() + Potion.REGISTRY.getKeys().size() + BrewingRecipeRegistry.getRecipes().size();
		if (curSize != prevSize) {
			prevSize = curSize;
			types.clear();
			potions.clear();


			for (PotionHelper.MixPredicate<Item> predicate : PotionHelper.POTION_ITEM_CONVERSIONS) {
				potions.put(CompatHelper112.getPotionInput(predicate), 0);
			}
			for (PotionHelper.MixPredicate<Item> predicate : PotionHelper.POTION_ITEM_CONVERSIONS) {
				potions.remove(CompatHelper112.getPotionOutput(predicate));
			}
			potions.put(Items.POTIONITEM, 0);

			boolean flag;
			do {
				flag = false;
				for (PotionHelper.MixPredicate<Item> predicate : PotionHelper.POTION_ITEM_CONVERSIONS) {
					if (potions.containsKey(CompatHelper112.getPotionInput(predicate)) && !potions.containsKey(CompatHelper112.getPotionOutput(predicate))) {
						potions.put(CompatHelper112.getPotionOutput(predicate), potions.get(CompatHelper112.getPotionInput(predicate)) + 1);
						flag = true;
					}
				}
			} while (flag);

			List<Pair<PotionType, PotionType>> links = new ArrayList<>();
			links.addAll(PotionHelper.POTION_TYPE_CONVERSIONS.stream().map(CompatHelper112::createLink).collect(Collectors.toList()));
			links.addAll(BrewingRecipeRegistry.getRecipes().stream().filter(t -> t instanceof AbstractBrewingRecipe).map(t -> createLink((AbstractBrewingRecipe) t)).collect(Collectors.toList()));
			links = links.stream().filter(t -> t.getLeft() != null && t.getRight() != null && t.getLeft() != t.getRight()).collect(Collectors.toList());

			LinkedHashSet<PotionType> set = new LinkedHashSet<>();
			Iterables.addAll(set, PotionType.REGISTRY);
			set.addAll(links.stream().map(Pair::getLeft).collect(Collectors.toList()));
			set.addAll(links.stream().map(Pair::getRight).collect(Collectors.toList()));

			types.put(PotionTypes.WATER, 0);

			do {
				flag = false;
				for (Pair<PotionType, PotionType> predicate : links) {
					if (types.containsKey(predicate.getLeft()) && !types.containsKey(predicate.getRight())) {
						types.put(predicate.getRight(), types.get(predicate.getLeft()) + 1);
						flag = true;
					}
				}
			} while (flag);


			for (PotionType type : set) {
				LogHelper.info(PotionType.REGISTRY.getNameForObject(type) + " " + types.get(type));
			}
		}
	}

	public Pair<PotionType, PotionType> createLink(AbstractBrewingRecipe recipe) {
		return Pair.of(getPotionFromItem(recipe.getInput()), getPotionFromItem(recipe.getOutput()));
	}

	@Nullable
	private PotionType getPotionFromItem(ItemStack input) {
		NBTTagCompound tag = input.getTagCompound();
		if (tag == null) {
			if (input.getItem() == Items.POTIONITEM)
				return PotionTypes.WATER;

			return null;
		}
		ResourceLocation potion = new ResourceLocation(tag.getString("Potion"));
		if (!PotionType.REGISTRY.containsKey(potion)) {
			return null;
		}

		return PotionType.REGISTRY.getObject(potion);
	}

	@Override
	public int getEnergyOutput(@Nonnull ItemStack stack) {
		checkTypes();

		Integer j = potions.get(stack.getItem());
		if (j == null) return 0;
		PotionType type = getPotionFromItem(stack);
		if (type == null) return 0;

		Integer i = types.get(type);
		if (i == null)
			return 0;

		return (int) (100 * Math.pow(4, Math.min(6, i + j)));
	}

	@Override
	protected float getEnergyRate(@Nonnull ItemStack stack) {
		checkTypes();

		Integer j = potions.get(stack.getItem());
		if (j == null) return 0;
		PotionType type = getPotionFromItem(stack);
		if (type == null) return 0;

		Integer i = types.get(type);
		if (i == null)
			return 0;

		return (int) (Math.pow(2, i + j) * 10);
	}

	@Nonnull
	@Override
	public Collection<ItemStack> getInputValues() {
		checkTypes();
		return Stream.concat(Stream.concat(ExtraUtils2.proxy.getSubItems(Items.POTIONITEM).stream(), ExtraUtils2.proxy.getSubItems(Items.LINGERING_POTION).stream()), ExtraUtils2.proxy.getSubItems(Items.SPLASH_POTION).stream())
				.filter(t -> getEnergyOutput(t) > 0).collect(Collectors.toList());
	}

	@Nullable
	@Override
	public ItemStack getContainer(ItemStack stack) {
		if (StackHelper.isNull(stack)) return StackHelper.empty();
		Item item = stack.getItem();
		if (item == Items.POTIONITEM || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION)
			return new ItemStack(Items.GLASS_BOTTLE);
		return super.getContainer(stack);
	}
}
