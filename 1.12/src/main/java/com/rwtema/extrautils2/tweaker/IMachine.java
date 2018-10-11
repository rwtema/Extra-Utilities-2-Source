package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.api.machine.*;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.liquid.WeightedLiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "IMachine")
@ZenRegister
public class IMachine extends ObjWrapper<Machine> {
	public IMachine(Machine object) {
		super(object);
	}

	@ZenMethod
	public void addRecipe(Map<String, IIngredient> inputs, Map<String, Object> outputs, int energy, int time) {
		HashMap<String, IIngredient> outMap = new HashMap<>();
		HashMap<String, Float> probMap = new HashMap<>();
		outputs.forEach((slot, obj) -> {
			if (obj instanceof WeightedItemStack) {
				probMap.put(slot, ((WeightedItemStack) obj).getChance());
				outMap.put(slot, ((WeightedItemStack) obj).getStack());
			} else if (obj instanceof WeightedLiquidStack) {
				probMap.put(slot, ((WeightedLiquidStack) obj).getChance());
				outMap.put(slot, ((WeightedLiquidStack) obj).getStack());
			} else if (obj instanceof IIngredient) {
				outMap.put(slot, (IIngredient) obj);
			}
		});
		addRecipe(inputs, outMap, energy, time, probMap);
	}

	@ZenMethod
	public void addRecipe(Map<String, IIngredient> inputs, Map<String, IIngredient> outputs, int energy, int time, Map<String, Float> probabilities) {
		CrafttweakerMachineRecipe recipe = new CrafttweakerMachineRecipe(getInternal(), inputs, outputs, probabilities, energy, time);
		getInternal().recipes_registry.addRecipe(recipe);
	}

	@ZenMethod
	public void removeRecipe(Map<String, IIngredient> inputs) {
		Map<String, IItemStack> items = new HashMap<>();
		Map<String, ILiquidStack> fluids = new HashMap<>();
		inputs.forEach((s, ingredient) -> {
			List<ILiquidStack> liquids = ingredient.getLiquids();
			if (liquids != null && !liquids.isEmpty()) {
				fluids.put(s, liquids.iterator().next());
			}
			List<IItemStack> items1 = ingredient.getItems();
			if (items1 != null && !items1.isEmpty()) {
				items.put(s, items1.iterator().next());
			}
		});
		removeRecipe(items, fluids);
	}

	@ZenMethod
	public void removeRecipe(Map<String, IItemStack> items, Map<String, ILiquidStack> fluids) {
		Map<MachineSlotItem, ItemStack> itemMap = items.entrySet().stream().collect(Collectors.toMap(s -> object.itemInputs.stream().filter(t -> s.getKey().equals(t.name)).findFirst().orElse(null), s -> XUTweaker.createItemStack(s.getValue())));
		Map<MachineSlotFluid, FluidStack> fluidMap = fluids.entrySet().stream().collect(Collectors.toMap(s -> object.fluidInputs.stream().filter(t -> s.getKey().equals(t.name)).findFirst().orElse(null), s -> XUTweaker.createFluidStack(s.getValue())));

		for (IMachineRecipe recipe : object.recipes_registry) {
			if (recipe.matches(itemMap, fluidMap)) {
				object.recipes_registry.removeRecipe(recipe);
				break;
			}
		}
	}

	@ZenMethod
	public List<IMachineSlot> getInputSlots() {
		return Stream.concat(object.itemInputs.stream(), object.fluidInputs.stream()).map(IMachineSlot::new).collect(Collectors.toList());
	}

	@ZenMethod
	public List<IMachineSlot> getOutputSlots() {
		return Stream.concat(object.itemOutputs.stream(), object.fluidOutputs.stream()).map(IMachineSlot::new).collect(Collectors.toList());
	}

	@ZenMethod
	public IMachineSlot getSlot(String slotName) {
		for (MachineSlot slot : object.itemInputs) {
			if (slot.name.equals(slotName)) return new IMachineSlot(slot);
		}
		for (MachineSlot slot : object.itemOutputs) {
			if (slot.name.equals(slotName)) return new IMachineSlot(slot);
		}
		for (MachineSlot slot : object.fluidInputs) {
			if (slot.name.equals(slotName)) return new IMachineSlot(slot);
		}
		for (MachineSlot slot : object.fluidOutputs) {
			if (slot.name.equals(slotName)) return new IMachineSlot(slot);
		}
		return null;
	}
}
