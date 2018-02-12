package com.rwtema.extrautils2.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.IMachineRecipe;
import com.rwtema.extrautils2.api.machine.MachineSlot;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

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
