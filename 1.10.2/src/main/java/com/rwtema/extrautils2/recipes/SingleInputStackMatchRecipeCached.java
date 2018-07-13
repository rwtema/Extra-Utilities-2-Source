package com.rwtema.extrautils2.recipes;

import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public abstract class SingleInputStackMatchRecipeCached extends SingleInputStackMatchRecipeBase {

	HashMap<ItemRef, Boolean> matchingCache = new HashMap<>();

	public SingleInputStackMatchRecipeCached(MachineSlotItem inputSlot) {
		super(inputSlot);
	}


	@Override
	public boolean allowInputItem(MachineSlotItem slot, ItemStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return matchingCache.computeIfAbsent(ItemRef.wrapCrafting(stack),
				key -> matches(key.createItemStack(1))
		);
	}

	@Override
	public boolean matches(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(inputSlot);
		return matchingCache.computeIfAbsent(ItemRef.wrap(stack),
				key -> matches(key.createItemStack(1))
		);
	}


}
