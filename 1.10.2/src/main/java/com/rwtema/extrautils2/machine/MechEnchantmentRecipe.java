package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.XURandom;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class MechEnchantmentRecipe implements IMachineRecipe {
	public final int processing_time;

	final EnchantType enchantType;
	final MachineSlotItem input, input_lapis, output;
	final List<ItemStack> ores;

	public MechEnchantmentRecipe(int processing_time, List<ItemStack> ores, EnchantType enchantType) {
		this(enchantType, XUMachineEnchanter.INPUT, XUMachineEnchanter.INPUT_LAPIS, XUMachineEnchanter.OUTPUT, processing_time, ores);
	}

	public MechEnchantmentRecipe(EnchantType enchantType, MachineSlotItem input, MachineSlotItem input_lapis, MachineSlotItem output, int processing_time, List<ItemStack> ores) {
		this.enchantType = enchantType;
		this.input = input;
		this.input_lapis = input_lapis;
		this.output = output;
		this.processing_time = processing_time;
		this.ores = ores;
	}


	@Override
	public List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples() {

		return ImmutableList.of(
				Pair.of(
						ImmutableMap.<MachineSlotItem, List<ItemStack>>builder()
								.put(input, ImmutableList.of(
										Items.WOODEN_SWORD, Items.WOODEN_SHOVEL, Items.WOODEN_AXE,
										Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_CHESTPLATE, Items.IRON_HELMET, Items.IRON_BOOTS, Items.IRON_LEGGINGS,
										Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS,
										Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS
								).stream().map(ItemStack::new).collect(Collectors.toList()))

								.put(input_lapis, ores)

								.build(),
						ImmutableMap.of()
				)
		);
	}

	@Override
	public boolean allowInputItem(MachineSlotItem slot, ItemStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return isValidStack(slot, stack);
	}

	private boolean isValidStack(MachineSlotItem slot, ItemStack stack) {
		if (StackHelper.isNull(stack)) return false;
		if (slot == input_lapis) {
			for (ItemStack ore : ores) {
				if (OreDictionary.itemMatches(ore, stack, false)) {
					return true;
				}
			}
		} else if (slot == input) {
			if (stack.getItem() == Items.BOOK) {
				return true;
			}

			if (!stack.isItemEnchantable() || stack.getItem().getItemEnchantability(stack) <= 0) {
				return false;
			} else {
				for (Enchantment enchantment : Enchantment.REGISTRY) {
					if (!enchantment.isTreasureEnchantment() && enchantment.canApplyAtEnchantingTable(stack)) {
						return true;
					}
				}

				return false;
			}
		}
		return false;
	}

	@Override
	public boolean allowInputFluid(MachineSlotFluid slot, FluidStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return false;
	}

	@Override
	public boolean matches(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return isValidStack(input_lapis, inputItems.get(input_lapis)) && isValidStack(input, inputItems.get(input));
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getItemOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(input).copy();
		int level = 1;
		stack = enchant(stack, level, XURandom.rand);
		return ImmutableMap.of(output, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Map<MachineSlotItem, ItemStack> getItemOutputsJEI(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(input).copy();
		long currentTimeMillis = System.currentTimeMillis() / 20000 + inputItems.get(input).hashCode();
		stack = enchant(stack, 1, new Random(currentTimeMillis));
		return ImmutableMap.of(output, stack);
	}

	@Nonnull
	private ItemStack enchant(ItemStack stack, int level, Random random) {
		List<Enchantment> list = buildList(stack, level, random);
		boolean flag = stack.getItem() == Items.BOOK;

		if (flag) {
			stack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		for (Enchantment enchantment : list) {
			if (flag) {
				//noinspection AccessStaticViaInstance
				((ItemEnchantedBook)Items.ENCHANTED_BOOK).addEnchantment(stack, new EnchantmentData(enchantment, enchantType.getLevel.applyAsInt(enchantment)));
			} else {
				stack.addEnchantment(enchantment, enchantType.getLevel.applyAsInt(enchantment));
			}
		}

		return stack;
	}

	@Nonnull
	private List<Enchantment> buildList(ItemStack stack, int level, Random random) {
		List<Enchantment> list = Lists.newArrayList();
		Item item = stack.getItem();
		int i = item.getItemEnchantability(stack);

		if (i <= 0) return list;

		level = level + 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
		float mult = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
		level = MathHelper.clamp(Math.round((float) level + (float) level * mult), 1, Integer.MAX_VALUE);
		List<EnchantmentEntry> list1 = getEnchantmentDatas(stack);

		if (!list1.isEmpty()) {
			Enchantment enchantment = WeightedRandom.getRandomItem(random, list1).enchantment;
			list.add(enchantment);

			while (random.nextInt(50) <= level) {

				CompatHelper.removeIncompatibleEnchantments(list1, enchantment);

				if (list1.isEmpty()) {
					break;
				}

				enchantment = WeightedRandom.getRandomItem(random, list1).enchantment;
				list.add(enchantment);
				level /= 2;
			}
		}

		return list;
	}

	@Nonnull
	private List<EnchantmentEntry> getEnchantmentDatas(ItemStack stack) {
		List<EnchantmentEntry> list = Lists.newArrayList();
		boolean flag = stack.getItem() == Items.BOOK;

		for (Enchantment enchantment : Enchantment.REGISTRY) {
			if ((!enchantment.isTreasureEnchantment()) && (enchantment.canApplyAtEnchantingTable(stack) || (flag && enchantment.isAllowedOnBooks()))) {
				list.add(new EnchantmentEntry(enchantment));
			}
		}

		return list;
	}

	@Override
	public Map<MachineSlotFluid, FluidStack> getFluidOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return ImmutableMap.of();
	}

	@Override
	public int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return processing_time * 2;
	}

	@Override
	public int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return processing_time;
	}

	@Override
	public TObjectIntMap<MachineSlot> getAmountToConsume(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		TObjectIntHashMap<MachineSlot> toConsume = new TObjectIntHashMap<>();
		toConsume.put(input, 1);
		toConsume.put(input_lapis, 1);
		return toConsume;
	}

	public enum EnchantType {
		LOWEST(Enchantment::getMinLevel),
		HIGHEST(Enchantment::getMaxLevel);

		final ToIntFunction<Enchantment> getLevel;

		EnchantType(ToIntFunction<Enchantment> getLevel) {
			this.getLevel = getLevel;
		}
	}

	public static class EnchantmentEntry extends WeightedRandom.Item {
		public final Enchantment enchantment;

		public EnchantmentEntry(Enchantment enchantment) {
			super(getRarity(enchantment));
			this.enchantment = enchantment;
		}

		private static int getRarity(Enchantment enchantment) {
			float weight = 100 * enchantment.getRarity().getWeight();
			float v = 1 - MathHelper.clamp((enchantment.getMinEnchantability(0) - 1) / 30F, 0, 1);
			return Math.max(1, (int) (v * weight));
		}
	}
}
