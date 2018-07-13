package com.rwtema.extrautils2.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.IMachineRecipe;
import com.rwtema.extrautils2.api.machine.MachineSlot;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.compatibility.StackHelper;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class SingleInputStackMatchRecipeBase implements IMachineRecipe {
	public final MachineSlotItem inputSlot;
	final TObjectIntHashMap<MachineSlot> amount;

	public SingleInputStackMatchRecipeBase(MachineSlotItem inputSlot) {
		this.inputSlot = inputSlot;
		this.amount = new TObjectIntHashMap<>();
		this.amount.put(this.inputSlot, 1);
	}

	@Override
	public List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples() {
		List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> list = new ArrayList<>();
		Collection<ItemStack> values = getInputValues();
		values.stream().filter(StackHelper::isNonNull).forEach(stack -> {
			list.add(Pair.of(ImmutableMap.of(inputSlot, ImmutableList.of(stack)), ImmutableMap.of()));
		});

		return list;
	}

	@Nonnull
	public abstract Collection<ItemStack> getInputValues();

	public abstract boolean matches(ItemStack stack);

	@Override
	public boolean allowInputFluid(MachineSlotFluid slot, FluidStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return false;
	}


	@Override
	public TObjectIntMap<MachineSlot> getAmountToConsume(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return amount;
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getContainerItems(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		Map<MachineSlotItem, ItemStack> map = new HashMap<>();
		map.put(inputSlot, getContainer(inputItems.get(inputSlot)));
		return map;
	}

	@Nullable
	public ItemStack getContainer(ItemStack stack) {
		return ForgeHooks.getContainerItem(stack);
	}


	@Override
	public boolean allowInputItem(MachineSlotItem slot, ItemStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return matches(stack);
	}

	@Override
	public boolean matches(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(inputSlot);
		return matches(stack);
	}

}
