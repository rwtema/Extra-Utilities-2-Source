package com.rwtema.extrautils2.recipes;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public abstract class SingleInputStackToStackRecipeCached extends SingleInputStackMatchRecipeCached {

	HashMap<ItemRef, ItemStack> cache = new HashMap<>();
	public final MachineSlotItem slotOutput;

	protected SingleInputStackToStackRecipeCached(MachineSlotItem inputSlot, MachineSlotItem slotOutput) {
		super(inputSlot);

		this.slotOutput = slotOutput;
	}

	@Override
	public boolean matches(ItemStack stack) {
		return StackHelper.isNonNull(getCachedResult(stack));
	}

	public ItemStack getCachedResult(ItemStack stack) {
		if (StackHelper.isNull(stack) || stack.getItem() == StackHelper.nullItem()) return StackHelper.empty();
		ItemRef wrap = ItemRef.wrapCrafting(stack);
		return cache.computeIfAbsent(wrap, key -> getResult(key.createItemStack(1)));
	}

	public abstract ItemStack getResult(@Nonnull ItemStack stack);

	@Override
	public Map<MachineSlotItem, ItemStack> getItemOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(inputSlot);
		ItemStack itemstack = getCachedResult(stack);
		if (StackHelper.isNull(itemstack)) return ImmutableMap.of();

		return ImmutableMap.of(slotOutput, itemstack.copy());
	}

	@Override
	public Map<MachineSlotFluid, FluidStack> getFluidOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return ImmutableMap.of();
	}
}
