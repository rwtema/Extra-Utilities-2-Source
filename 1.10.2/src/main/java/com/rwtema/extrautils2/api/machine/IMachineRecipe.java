package com.rwtema.extrautils2.api.machine;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.compatibility.StackHelper;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IMachineRecipe {
	static Map<MachineSlotItem, ItemStack> getVanillaContainerItems(Map<MachineSlotItem, ItemStack> inputItems) {
		ImmutableMap.Builder<MachineSlotItem, ItemStack> builder = ImmutableMap.builder();
		for (Map.Entry<MachineSlotItem, ItemStack> entry : inputItems.entrySet()) {
			ItemStack container = ForgeHooks.getContainerItem(entry.getValue());
			if (StackHelper.isNonNull(container))
				builder.put(entry.getKey(), container);
		}
		return builder.build();
	}

	List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples();

	boolean allowInputItem(MachineSlotItem slot, ItemStack stack,
						   Map<MachineSlotItem, ItemStack> existingItems,
						   Map<MachineSlotFluid, FluidStack> existingFluids);

	boolean allowInputFluid(MachineSlotFluid slot, FluidStack stack,
							Map<MachineSlotItem, ItemStack> existingItems,
							Map<MachineSlotFluid, FluidStack> existingFluids
	);

	boolean matches(
			Map<MachineSlotItem, ItemStack> inputItems,
			Map<MachineSlotFluid, FluidStack> inputFluids);

	Map<MachineSlotItem, ItemStack> getItemOutputs(
			Map<MachineSlotItem, ItemStack> inputItems,
			Map<MachineSlotFluid, FluidStack> inputFluids);

	Map<MachineSlotFluid, FluidStack> getFluidOutputs(
			Map<MachineSlotItem, ItemStack> inputItems,
			Map<MachineSlotFluid, FluidStack> inputFluids);

	int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems,
						Map<MachineSlotFluid, FluidStack> inputFluids);

	int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems,
						  Map<MachineSlotFluid, FluidStack> inputFluids);

	default float getEnergyRate(Map<MachineSlotItem, ItemStack> inputItems,
								Map<MachineSlotFluid, FluidStack> inputFluids) {
		return getEnergyOutput(inputItems, inputFluids) / (float) getProcessingTime(inputItems, inputFluids);
	}

	TObjectIntMap<MachineSlot> getAmountToConsume(Map<MachineSlotItem, ItemStack> inputItems,
												  Map<MachineSlotFluid, FluidStack> inputFluids);

	default Map<MachineSlotItem, ItemStack> getContainerItems(Map<MachineSlotItem, ItemStack> inputItems,
															  Map<MachineSlotFluid, FluidStack> inputFluids) {
		Map<MachineSlotItem, ItemStack> map = new HashMap<>();
		for (Map.Entry<MachineSlotItem, ItemStack> entry : inputItems.entrySet()) {
			map.put(entry.getKey(), ForgeHooks.getContainerItem(entry.getValue()));
		}
		return map;
	}

	@Nullable
	default TObjectFloatMap<MachineSlot> getProbabilityModifier(Map<MachineSlotItem, ItemStack> inputItems,
																Map<MachineSlotFluid, FluidStack> inputFluids) {
		return null;
	}

	default Map<MachineSlotItem, ItemStack> getItemOutputsJEI(
			Map<MachineSlotItem, ItemStack> inputItems,
			Map<MachineSlotFluid, FluidStack> inputFluids) {
		return getItemOutputs(inputItems, inputFluids);
	}

	default Map<MachineSlotFluid, FluidStack> getFluidOutputsJEI(
			Map<MachineSlotItem, ItemStack> inputItems,
			Map<MachineSlotFluid, FluidStack> inputFluids) {
		return getFluidOutputs(inputItems, inputFluids);
	}
}
