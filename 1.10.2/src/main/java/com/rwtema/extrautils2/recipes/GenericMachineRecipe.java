package com.rwtema.extrautils2.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericMachineRecipe implements IMachineRecipe {
	protected final Machine machine;
	protected final Map<MachineSlotItem, List<ItemStack>> inputItemStackMap;
	protected final Map<MachineSlotFluid, List<String>> inputFluidStackMap;
	protected final Map<MachineSlotItem, List<ItemStack>> outputItemStackMap;
	protected final Map<MachineSlotFluid, String> outputFluidStackMap;
	protected final TObjectIntMap<MachineSlot> inputAmountMap;
	protected final TObjectIntMap<MachineSlot> outputAmountMap;
	protected final TObjectFloatHashMap<MachineSlot> outputProbabilities;
	protected boolean hasMultipleOutputs;
	protected int energyOutput;
	protected int processingTime;

	public GenericMachineRecipe(Machine machine,
								Map<MachineSlotItem, List<ItemStack>> inputItemStackMap,
								Map<MachineSlotFluid, List<String>> inputFluidStackMap,
								Map<MachineSlotItem, List<ItemStack>> outputItemStackMap,
								Map<MachineSlotFluid, String> outputFluidStackMap,
								TObjectIntMap<MachineSlot> inputAmountMap, TObjectIntMap<MachineSlot> outputAmountMap, int processingTime, int energyOutput, TObjectFloatHashMap<MachineSlot> outputProbabilities) {
		this.machine = machine;
		this.inputItemStackMap = ImmutableMap.copyOf(inputItemStackMap);
		this.inputFluidStackMap = ImmutableMap.copyOf(inputFluidStackMap);
		this.outputItemStackMap = ImmutableMap.copyOf(outputItemStackMap);
		this.outputFluidStackMap = ImmutableMap.copyOf(outputFluidStackMap);
		this.outputAmountMap = outputAmountMap;
		this.processingTime = processingTime;
		this.energyOutput = energyOutput;
		this.inputAmountMap = inputAmountMap;
		this.hasMultipleOutputs = !outputItemStackMap.values().stream().allMatch((itemStacks) -> itemStacks.size() <= 1 && (itemStacks instanceof ImmutableList));
		this.outputProbabilities = outputProbabilities;
	}

	@Override
	public List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples() {
		if (!inputItemStackMap.isEmpty() && inputItemStackMap.values().stream().noneMatch((itemStacks) -> itemStacks != null && !itemStacks.isEmpty())) {
			return ImmutableList.of();
		}

		if (!outputItemStackMap.isEmpty() && outputItemStackMap.values().stream().noneMatch((itemStacks) -> itemStacks != null && !itemStacks.isEmpty())) {
			return ImmutableList.of();
		}

		if (!inputFluidStackMap.isEmpty() && inputFluidStackMap.values().stream().noneMatch(s -> s != null && s.stream().anyMatch(t -> t != null && FluidRegistry.isFluidRegistered(t)))) {
			return ImmutableList.of();
		}

		if (!outputFluidStackMap.isEmpty() && outputFluidStackMap.values().stream().noneMatch(s -> s != null && FluidRegistry.isFluidRegistered(s))) {
			return ImmutableList.of();
		}

		ImmutableMap.Builder<MachineSlotFluid, List<FluidStack>> builder = ImmutableMap.builder();

		inputFluidStackMap.forEach((machineSlotFluid, fluidStacks) -> {
			int amount = inputAmountMap.get(machineSlotFluid);
			builder.put(machineSlotFluid, fluidStacks.stream().map(
					t -> FluidRegistry.isFluidRegistered(t) ? FluidRegistry.getFluidStack(t, amount) : null).collect(Collectors.toList())
			);
		});

		ImmutableMap.Builder<MachineSlotItem, List<ItemStack>> builder1 = ImmutableMap.builder();
		inputItemStackMap.forEach((machineSlotItem, itemStacks) -> {
			int amount = inputAmountMap.get(machineSlotItem);
			List<ItemStack> collect = itemStacks.stream().map(stack -> ItemHandlerHelper.copyStackWithSize(stack, amount)).collect(Collectors.toList());
			builder1.put(machineSlotItem, collect);
		});

		return ImmutableList.of(Pair.of(builder1.build(), builder.build()));
	}

	@Override
	public boolean allowInputItem(MachineSlotItem slot, ItemStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return checkForValidOutput() && matchesSlotItem(slot, stack) && checkExistingItems(existingItems, existingFluids);

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

	public boolean matchesSlotItem(MachineSlotItem slot, ItemStack stack) {
		List<ItemStack> stackList = inputItemStackMap.get(slot);
		if (slot.optional && stackList == null) return true;
		for (ItemStack itemStack : stackList) {
			if (OreDictionary.itemMatches(stack, itemStack, false))
				return true;
		}
		return false;
	}

	@Override
	public boolean allowInputFluid(MachineSlotFluid slot, FluidStack stack, Map<MachineSlotItem, ItemStack> existingItems, Map<MachineSlotFluid, FluidStack> existingFluids) {
		return checkForValidOutput() && matchesSlotFluid(slot, stack) && checkExistingItems(existingItems, existingFluids);
	}

	public boolean matchesSlotFluid(MachineSlotFluid slot, FluidStack stack) {
		if (stack != null)
			for (String fluidName : inputFluidStackMap.get(slot)) {
				if (fluidName != null && fluidName.matches(stack.getFluid().getName())) {
					return true;
				}
			}
		return false;
	}

	@Override
	public boolean matches(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		if (!checkForValidOutput()
		) {
			return false;
		}
		for (MachineSlotItem slotItem : machine.itemInputs) {
			ItemStack stack = inputItems.get(slotItem);
			if (StackHelper.isNull(stack)) {
				if (!slotItem.optional || inputItemStackMap.get(slotItem) != null) return false;
			} else if (!matchesSlotItem(slotItem, stack) || inputAmountMap.get(slotItem) > StackHelper.getStacksize(stack))
				return false;

		}

		for (MachineSlotFluid slotFluid : machine.fluidInputs) {
			FluidStack stack = inputFluids.get(slotFluid);
			if (stack == null) {
				if (!slotFluid.optional || inputFluidStackMap.get(slotFluid) != null) return false;
			} else if (!matchesSlotFluid(slotFluid, stack) || inputAmountMap.get(slotFluid) > stack.amount)
				return false;

		}

		return true;
	}

	private boolean checkForValidOutput() {
		return (outputItemStackMap.isEmpty() || !outputItemStackMap.values().stream().noneMatch((itemStacks) -> itemStacks != null && !itemStacks.isEmpty())) &&
				(outputFluidStackMap.isEmpty() || (!outputFluidStackMap.values().stream().noneMatch(s -> s != null && FluidRegistry.isFluidRegistered(s))));
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getItemOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		HashMap<MachineSlotItem, ItemStack> map = new HashMap<>();
		if (hasMultipleOutputs) {
			Set<String> mods = inputItems.values().stream().filter(stack -> StackHelper.isNonNull(stack)).map(stack -> stack.getItem().getRegistryName().getResourceDomain()).collect(Collectors.toSet());
			outputItemStackMap.forEach(((machineSlotItem, itemStacks) -> {
				int size = outputAmountMap.get(machineSlotItem);
				if (size < 0) size = 0;

				ItemStack bestStack = StackHelper.empty();
				if (size > 0) {
					for (ItemStack stack : itemStacks) {
						if (StackHelper.isNull(bestStack)) {
							bestStack = stack;
							continue;
						}
						String resourceDomain = stack.getItem().getRegistryName().getResourceDomain();
						if ("minecraft".equals(resourceDomain) || mods.contains(resourceDomain)) {
							bestStack = stack;
							break;
						}
					}
				}

				map.put(machineSlotItem, makeSafe(ItemHandlerHelper.copyStackWithSize(bestStack, size)));
			}));
		} else {
			outputItemStackMap.forEach((machineSlotItem, stack) -> {
				int size = outputAmountMap.get(machineSlotItem);
				if (size < 0) {
					size = 0;
				}
				map.put(machineSlotItem, makeSafe(ItemHandlerHelper.copyStackWithSize(stack.stream().findAny().orElse(null), size)));
			});
		}
		return map;
	}

	private ItemStack makeSafe(ItemStack stack) {
		if (StackHelper.isNull(stack))
			return StackHelper.empty();
		if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
			stack.setItemDamage(0);
		}
		return stack;
	}

	@Override
	public Map<MachineSlotFluid, FluidStack> getFluidOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		HashMap<MachineSlotFluid, FluidStack> map = new HashMap<>();
		outputFluidStackMap.forEach((machineSlotFluid, s) ->
				map.put(machineSlotFluid, s != null && FluidRegistry.isFluidRegistered(s) ? FluidRegistry.getFluidStack(s, outputAmountMap.get(machineSlotFluid)) : null)
		);
		return map;
	}

	@Override
	public int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return energyOutput;
	}

	@Override
	public int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return processingTime;
	}

	@Override
	public TObjectIntMap<MachineSlot> getAmountToConsume(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return inputAmountMap;
	}

	@Nullable
	@Override
	public TObjectFloatMap<MachineSlot> getProbabilityModifier(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return outputProbabilities;
	}

	public static class Builder implements RecipeBuilder {
		protected final HashMap<MachineSlotItem, List<ItemStack>> inputItemStackMap = new HashMap<>();
		protected final HashMap<MachineSlotFluid, List<String>> inputFluidStackMap = new HashMap<>();
		protected final HashMap<MachineSlotItem, List<ItemStack>> outputItemStackMap = new HashMap<>();
		protected final HashMap<MachineSlotFluid, String> outputFluidStackMap = new HashMap<>();
		protected final TObjectIntHashMap<MachineSlot> inputAmountMap = new TObjectIntHashMap<>();
		protected final Machine machine;
		protected final TObjectIntMap<MachineSlot> outputAmountMap = new TObjectIntHashMap<>();
		protected final TObjectFloatHashMap<MachineSlot> outputProbabilities = new TObjectFloatHashMap<>();
		protected int energy;
		protected int processingTime;

		public Builder(Machine machine) {
			this.machine = machine;
			energy = machine.defaultEnergy;
			processingTime = machine.defaultProcessingTime;
		}

		@Override
		public RecipeBuilder setEnergy(int amount) {
			Validate.isTrue(amount >= 0);
			energy = amount;
			return this;
		}

		@Override
		public RecipeBuilder setProcessingTime(int time) {
			Validate.isTrue(time > 0, "Negative/Zero processing times are not allowed %s", time);
			processingTime = time;
			return this;
		}

		@Override
		public RecipeBuilder setRFRate(int energy, float rfRate) {
			setEnergy(energy);
			setProcessingTime((int) Math.ceil(energy / rfRate));
			return this;
		}

		@Override
		public RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull ItemStack input) {
			return setItemInput(slot, input, StackHelper.getStacksize(input));
		}

		@Override
		public RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull ItemStack input, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputItemStackMap.put(slot, ImmutableList.of(input.copy()));
			inputAmountMap.put(slot, n);
			return this;
		}

		@Override
		public RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull String string, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputItemStackMap.put(slot, OreDictionary.getOres(string));
			inputAmountMap.put(slot, n);
			return this;
		}

		@Override
		public RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull List<ItemStack> stacks, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputItemStackMap.put(slot, stacks);
			inputAmountMap.put(slot, n);
			return this;
		}

		@Override
		public RecipeBuilder setItemOutput(@Nonnull MachineSlotItem slot, ItemStack stack) {
			if (StackHelper.isNonNull(stack)) {
				outputItemStackMap.put(slot, ImmutableList.of(stack));
				outputAmountMap.put(slot, StackHelper.getStacksize(stack));
			} else
				outputAmountMap.put(slot, 0);
			return this;
		}

		@Override
		public RecipeBuilder setItemOutput(MachineSlotItem slot, ItemStack stack, int amount) {
			if (StackHelper.isNonNull(stack)) {
				outputItemStackMap.put(slot, ImmutableList.of(stack));
				outputAmountMap.put(slot, amount);
			} else
				outputAmountMap.put(slot, 0);
			return this;
		}

		@Override
		public RecipeBuilder setItemOutput(@Nonnull MachineSlotItem slot, @Nonnull String oreName, int amount) {
			outputItemStackMap.put(slot, OreDictionary.getOres(oreName));
			outputAmountMap.put(slot, amount);
			return this;
		}

		@Override
		public RecipeBuilder setItemOutput(@Nonnull MachineSlotItem slot, @Nonnull List<ItemStack> ores, int amount) {
			outputItemStackMap.put(slot, ores);
			outputAmountMap.put(slot, amount);
			return this;
		}

		@Override
		public RecipeBuilder setFluidInputFluidName(@Nonnull MachineSlotFluid slot, @Nonnull String fluidName, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputFluidStackMap.put(slot, ImmutableList.of(fluidName));
			inputAmountMap.put(slot, n);
			return this;
		}


		@Override
		public RecipeBuilder setFluidInputFluidStack(@Nonnull MachineSlotFluid slot, @Nonnull FluidStack input) {
			return setFluidInputFluidStack(slot, input, input.amount);
		}

		@Override
		public RecipeBuilder setFluidInputFluidStack(@Nonnull MachineSlotFluid slot, @Nonnull FluidStack input, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputFluidStackMap.put(slot, ImmutableList.of(input.getFluid().getName()));
			inputAmountMap.put(slot, n);
			return this;
		}

		@Override
		public RecipeBuilder setFluidInputFluidNameList(@Nonnull MachineSlotFluid slot, @Nonnull List<String> fluidNames, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputAmountMap.put(slot, n);
			inputFluidStackMap.put(slot, ImmutableList.copyOf(fluidNames));
			return this;
		}

		@Override
		public RecipeBuilder setFluidInputFluidStackList(@Nonnull MachineSlotFluid slot, @Nonnull List<FluidStack> stacks, int n) {
			Validate.isTrue(n > 0, "n=%s must be positive", n);
			inputFluidStackMap.put(slot, stacks.stream().map(t -> t.getFluid().getName()).collect(Collectors.toList()));
			inputAmountMap.put(slot, n);
			return this;
		}

		@Override
		public RecipeBuilder setFluidOutput(@Nonnull MachineSlotFluid slot, FluidStack stack) {
			return setFluidOutput(slot, stack, stack.amount);
		}

		@Override
		public RecipeBuilder setFluidOutput(@Nonnull MachineSlotFluid slot, FluidStack stack, int amount) {
			return setFluidOutput(slot, stack.getFluid().getName(), amount);
		}

		@Override
		public RecipeBuilder setFluidOutput(@Nonnull MachineSlotFluid slot, String name, int amount) {
			outputFluidStackMap.put(slot, name);
			outputAmountMap.put(slot, amount);
			return this;
		}

		@Override
		public RecipeBuilder setProbability(@Nonnull MachineSlot outputSlot, float probability) {
			outputProbabilities.put(outputSlot, probability);
			return this;
		}

		@Override
		public IMachineRecipe build() {
			if (processingTime < 0) throw new IllegalArgumentException("processing time not set and has no default");
			if (energy < 0) throw new IllegalArgumentException("energy not set and has no default");

			for (MachineSlotItem slotItem : machine.itemInputs) {
				if (slotItem.optional) continue;
				Validate.isTrue(inputItemStackMap.containsKey(slotItem), "Slot %s has no input", slotItem);
				Validate.isTrue(inputAmountMap.containsKey(slotItem), "Slot %s was not assigned an amount", slotItem);
			}
			for (MachineSlotFluid slotFluid : machine.fluidInputs) {
				if (slotFluid.optional) continue;
				Validate.isTrue(inputFluidStackMap.containsKey(slotFluid), "Slot %s has no input", slotFluid);
				Validate.isTrue(inputAmountMap.containsKey(slotFluid), "Slot %s was not assigned an amount", slotFluid);
			}
			for (MachineSlotItem slotItem : machine.itemOutputs) {
				if (slotItem.optional) continue;
				Validate.isTrue(outputItemStackMap.containsKey(slotItem), "Slot %s has no output", slotItem);
			}

			for (MachineSlotFluid slotFluid : machine.fluidOutputs) {
				if (slotFluid.optional) continue;
				Validate.isTrue(outputFluidStackMap.containsKey(slotFluid), "Slot %s has no output", slotFluid);
			}

			return getGenericMachineRecipe(machine, inputItemStackMap, inputFluidStackMap, outputItemStackMap, outputFluidStackMap, inputAmountMap, processingTime, energy, outputAmountMap, outputProbabilities);
		}

		@Override
		@Nonnull
		public Machine getMachine() {
			return machine;
		}

		@Nonnull
		protected GenericMachineRecipe getGenericMachineRecipe(Machine machine, HashMap<MachineSlotItem, List<ItemStack>> inputItemStackMap, HashMap<MachineSlotFluid, List<String>> inputFluidStackMap, Map<MachineSlotItem, List<ItemStack>> outputItemStackMap, HashMap<MachineSlotFluid, String> outputFluidStackMap, TObjectIntHashMap<MachineSlot> amount, int processingTime, int energyOutput, TObjectIntMap<MachineSlot> outputAmountMap, TObjectFloatHashMap<MachineSlot> outputProbabilities) {
			return new GenericMachineRecipe(machine, inputItemStackMap, inputFluidStackMap, outputItemStackMap, outputFluidStackMap, amount, outputAmountMap, processingTime, energyOutput, outputProbabilities);
		}
	}
}
