package com.rwtema.extrautils2.tweaker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CrafttweakerMachineRecipe implements IMachineRecipe {

	public final Machine machine;
	public final Map<String, IIngredient> inputs;
	public final Map<String, IIngredient> outputs;
	public final Map<String, Float> probabilities;
	public final int processingTime;
	public final int energy;

	public CrafttweakerMachineRecipe(Machine machine, Map<String, IIngredient> inputs, Map<String, IIngredient> outputs, Map<String, Float> probabilities, int energy, int processingTime) {
		this.machine = machine;
		this.inputs = inputs;
		this.outputs = outputs;
		this.probabilities = probabilities;
		this.processingTime = processingTime;
		this.energy = energy;
	}

	public <S extends MachineSlot> S getSlot(String s, Iterable<S> slotList) {
		for (S s1 : slotList) {
			if (s1.name.equals(s)) {
				return s1;
			}
		}
		throw new IllegalStateException();
	}

	public MachineSlot getInputSlot(String s) {
		return getSlot(s, Iterables.concat(machine.itemInputs, machine.fluidInputs));
	}

	public MachineSlot getOutputSlot(String s) {
		return getSlot(s, Iterables.concat(machine.itemOutputs, machine.fluidOutputs));
	}

	@Override
	public List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples() {
		ImmutableMap.Builder<MachineSlotFluid, List<FluidStack>> fluidBuilder = ImmutableMap.builder();

		ImmutableMap.Builder<MachineSlotItem, List<ItemStack>> itemBuilder = ImmutableMap.builder();
		inputs.forEach((slot, ingredient) -> {
			MachineSlot inputSlot = getInputSlot(slot);
			if (inputSlot instanceof MachineSlotItem) {
				List<IItemStack> items = ingredient.getItems();
				if (items != null)
					itemBuilder.put(((MachineSlotItem) inputSlot), items.stream().map(XUTweaker::createItemStack).collect(Collectors.toList()));
			} else if (inputSlot instanceof MachineSlotFluid) {
				List<ILiquidStack> liquids = ingredient.getLiquids();
				if (liquids != null)
					fluidBuilder.put(((MachineSlotFluid) inputSlot), liquids.stream().map(XUTweaker::createFluidStack).collect(Collectors.toList()));
			}
		});

		return ImmutableList.of(Pair.of(itemBuilder.build(), fluidBuilder.build()));
	}

	@Override
	public boolean allowInputItem(MachineSlotItem slot, ItemStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return matchesSlotItem(slot, stack) && checkExistingItems(existingItems, existingFluids);
	}

	@Override
	public boolean allowInputFluid(MachineSlotFluid slot, FluidStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return matchesSlotFluid(slot, stack) && checkExistingItems(existingItems, existingFluids);
	}


	public boolean checkExistingItems(Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		for (Map.Entry<MachineSlotItem, ItemStack> entry : existingItems.entrySet()) {
			if (StackHelper.isNonNull(entry.getValue()) && !matchesSlotItem(entry.getKey(), entry.getValue()))
				return false;
		}
		for (Map.Entry<MachineSlotFluid, FluidStack> entry : existingFluids.entrySet()) {
			if (!matchesSlotFluid(entry.getKey(), entry.getValue()))
				return false;
		}
		return true;
	}

	public boolean matchesSlotFluid(MachineSlotFluid slot, FluidStack stack) {
		if (stack == null || !inputs.containsKey(slot.name)) return false;
		IIngredient iIngredient = inputs.get(slot.name);
		return iIngredient.matches(XUTweaker.getILiquidStack(stack));
	}

	public boolean matchesSlotItem(MachineSlotItem slot, ItemStack stack) {
		if (stack == null || !inputs.containsKey(slot.name)) return false;
		IIngredient iIngredient = inputs.get(slot.name);
		return iIngredient.matches(XUTweaker.getIItemStack(stack));
	}

	@Override
	public boolean matches(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		for (MachineSlotItem slotItem : machine.itemInputs) {
			ItemStack stack = inputItems.get(slotItem);
			if (StackHelper.isNull(stack)) {
				if (!slotItem.optional || inputs.containsKey(slotItem.name)) return false;
			} else if (!matchesSlotItem(slotItem, stack) || getAmountToConsume(slotItem) > StackHelper.getStacksize(stack))
				return false;

		}

		for (MachineSlotFluid slotFluid : machine.fluidInputs) {
			FluidStack stack = inputFluids.get(slotFluid);
			if (stack == null) {
				if (!slotFluid.optional || inputs.containsKey(slotFluid.name)) return false;
			} else if (!matchesSlotFluid(slotFluid, stack) || getAmountToConsume(slotFluid) > stack.amount)
				return false;

		}

		return true;
	}

	private int getAmountToConsume(MachineSlot slot) {
		IIngredient iIngredient = inputs.get(slot.name);
		if (iIngredient == null) return 1;
		return iIngredient.getAmount();
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getItemOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		HashMap<MachineSlotItem, ItemStack> map = new HashMap<>();
		outputs.forEach((s, ingredient) -> {
			MachineSlot outputSlot = getOutputSlot(s);
			if (outputSlot instanceof MachineSlotItem) {
				List<IItemStack> items = ingredient.getItems();
				if (items != null && !items.isEmpty()) {
					ItemStack stack;
					if (items.size() == 1) {
						stack = XUTweaker.createItemStack(items.iterator().next());
					} else {
						stack = ItemStack.EMPTY;
						Set<String> collect = inputItems.values().stream()
								.map(ItemStack::getItem)
								.map(Item::getRegistryName)
								.filter(Objects::nonNull)
								.map(ResourceLocation::getResourceDomain)
								.collect(Collectors.toSet());
						for (IItemStack item : items) {
							stack = XUTweaker.createItemStack(item);
							if (collect.contains(Objects.requireNonNull(stack.getItem().getRegistryName()).getResourceDomain())) {
								break;
							}
						}
					}
					map.put((MachineSlotItem) outputSlot, Objects.requireNonNull(stack));
				}
			}
		});

		return map;
	}

	@Override
	public Map<MachineSlotFluid, FluidStack> getFluidOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		HashMap<MachineSlotFluid, FluidStack> map = new HashMap<>();
		outputs.forEach((s, ingredient) -> {
			MachineSlot outputSlot = getOutputSlot(s);
			if (outputSlot instanceof MachineSlotFluid) {
				List<ILiquidStack> liquids = ingredient.getLiquids();
				if (liquids != null && !liquids.isEmpty()) {
					FluidStack stack = XUTweaker.createFluidStack(liquids.iterator().next());
					map.put((MachineSlotFluid) outputSlot, stack);
				}
			}
		});
		return map;
	}

	@Override
	public int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return energy;
	}

	@Override
	public int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return processingTime;
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getContainerItems(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		Map<MachineSlotItem, ItemStack> map = new HashMap<>();
		machine.itemInputs.forEach(slot -> map.put(slot, ItemStack.EMPTY));
		for (Map.Entry<MachineSlotItem, ItemStack> entry : inputItems.entrySet()) {
			IIngredient iIngredient = inputs.get(entry.getKey().name);
			if (iIngredient != null && iIngredient.hasTransformers() && iIngredient instanceof IItemStack) {
				IItemStack iItemStack = iIngredient.applyTransform((IItemStack) iIngredient, null);
				if (iItemStack != null) {
					map.put(entry.getKey(), XUTweaker.createItemStack(iItemStack));
				}
			}
		}

		return map;
	}

	@Nullable
	@Override
	public TObjectFloatMap<MachineSlot> getProbabilityModifier(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		TObjectFloatMap<MachineSlot> map = new TObjectFloatHashMap<>();
		probabilities.forEach((name, chance) -> map.put(getOutputSlot(name), chance));
		return map;
	}

	@Override
	public TObjectIntMap<MachineSlot> getAmountToConsume(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		TObjectIntMap<MachineSlot> map = new TObjectIntHashMap<>();
		inputItems.keySet().forEach(s -> map.put(s, 1));
		inputFluids.keySet().forEach(s -> map.put(s, 1));
		inputs.forEach(
				(k, i) -> {
					MachineSlot inputSlot = getInputSlot(k);
					map.put(inputSlot, i.getAmount());
				}
		);
		return map;
	}
}
