package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class DischantEnergyRecipe extends EnergyBaseRecipe {
	@Override
	public int getEnergyOutput(@Nonnull ItemStack stack) {
		if (!stack.isItemEnchanted() && stack.getItem() != Items.ENCHANTED_BOOK) {
			return 0;
		}

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
		if (enchantments.isEmpty())
			return 0;

		double amount = 0;
		for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			if (enchantment == null) continue;
			int rawLevel = entry.getValue();
			int level = 1 + rawLevel;
			int weight = enchantment.getRarity().getWeight();
			double v = Math.sqrt(Math.min(level, enchantment.getMaxLevel()) / (double) enchantment.getMaxLevel()) * enchantment.getMaxLevel() * level * enchantment.getMaxLevel();
			amount += v / Math.sqrt(weight) * Math.max(1, enchantment.getMinEnchantability(rawLevel));
		}
		return (int) Math.ceil(amount) * 400;
	}

	@Override
	protected float getEnergyRate(@Nonnull ItemStack stack) {
		return 40;
	}

	@Nonnull
	@Override
	public Collection<ItemStack> getInputValues() {
		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
		for (Enchantment enchantment : Enchantment.REGISTRY) {
			for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {
				builder.add(((ItemEnchantedBook) Items.ENCHANTED_BOOK).getEnchantedItemStack(new EnchantmentData(enchantment, i)));
			}
		}
		return builder.build();
	}

	@Override
	public List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples() {
		List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> list = new ArrayList<>();
		for (Enchantment enchantment : Enchantment.REGISTRY) {
			ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
			for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {
				builder.add(((ItemEnchantedBook) Items.ENCHANTED_BOOK).getEnchantedItemStack(new EnchantmentData(enchantment, i)));
			}

			list.add(Pair.of(ImmutableMap.of(inputSlot, builder.build()), ImmutableMap.of()));
		}

		return list;
	}
}
