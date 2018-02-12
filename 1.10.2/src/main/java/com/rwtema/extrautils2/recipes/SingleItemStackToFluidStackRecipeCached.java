package com.rwtema.extrautils2.recipes;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class SingleItemStackToFluidStackRecipeCached extends SingleInputStackMatchRecipeCached {
	public SingleItemStackToFluidStackRecipeCached(MachineSlotItem inputSlot) {
		super(inputSlot);
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getItemOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return ImmutableMap.of();
	}

	@Override
	public Map<MachineSlotFluid, FluidStack> getFluidOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return null;
	}

	@Nonnull
	@Override
	public Collection<ItemStack> getInputValues() {
		return Collections.emptyList();
	}

	@Override
	public boolean matches(ItemStack stack) {
		return false;
	}
}
