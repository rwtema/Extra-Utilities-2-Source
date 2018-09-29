package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.api.machine.MachineSlot;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.api.machine.RecipeBuilder;
import crafttweaker.api.item.IIngredient;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class IRecipeBuilder extends ObjWrapper<RecipeBuilder> {

	HashMap<IMachineSlot, String> trackedOutputs = new HashMap<>();

	public IRecipeBuilder(RecipeBuilder object) {
		super(object);
	}

	@ZenMethod
	public IRecipeBuilder setEnergy(int amountToAddToBuffer) {
		object.setEnergy(amountToAddToBuffer);
		return this;
	}

	@ZenMethod
	public IRecipeBuilder setProcessingTime(int time) {
		object.setProcessingTime(time);
		return this;
	}

	@ZenMethod
	public IRecipeBuilder setRFRate(int energy, float rfRate) {
		object.setRFRate(energy, rfRate);
		return this;
	}


	@ZenMethod
	public IRecipeBuilder setInput(String slot, IIngredient ingredient) {
		IMachineSlot slot1 = getMachine().getSlot(slot);
		return setInput(slot1, ingredient);
	}

	@ZenMethod
	public IRecipeBuilder setInput(IMachineSlot slot, IIngredient ingredient) {
		MachineSlot machineSlot = slot.object;
		if (machineSlot instanceof MachineSlotItem) {
			object.setItemInput((MachineSlotItem) machineSlot, getItemStackList(ingredient), ingredient.getAmount());
		} else if (machineSlot instanceof MachineSlotFluid) {
			object.setFluidInputFluidStackList((MachineSlotFluid) machineSlot, getFluidList(ingredient), ingredient.getAmount());
		}
		return this;
	}

	private List<ItemStack> getItemStackList(IIngredient ingredient) {
		return ingredient.getItems().stream().map(XUTweaker::createItemStack).collect(Collectors.toList());
	}

	private List<FluidStack> getFluidList(IIngredient ingredient) {
		return ingredient.getLiquids().stream().map(XUTweaker::createFluidStack).collect(Collectors.toList());
	}

	@ZenMethod
	public IRecipeBuilder setOutput(String slot, IIngredient stack) {
		IMachineSlot slot1 = getMachine().getSlot(slot);
		return setOutput(slot1, stack);
	}

	private IRecipeBuilder setOutput(IMachineSlot slot, IIngredient stack) {
		MachineSlot machineSlot = slot.object;
		if (machineSlot instanceof MachineSlotItem) {
			List<ItemStack> itemStackList = getItemStackList(stack);
			itemStackList.stream().findAny().ifPresent(s -> trackedOutputs.put(slot, s.getDisplayName()));
			object.setItemOutput((MachineSlotItem) machineSlot, itemStackList, stack.getAmount());
		} else if (machineSlot instanceof MachineSlotFluid) {
			getFluidList(stack).stream().findAny().ifPresent(
					fluid -> {
						trackedOutputs.put(slot, fluid.getLocalizedName());
						object.setFluidOutput((MachineSlotFluid) machineSlot, fluid, stack.getAmount());
					}
			);
		}
		return this;
	}

	@ZenMethod
	public IRecipeBuilder setOutput(String slot, IIngredient stack, int probability) {
		setOutput(slot, stack);
		setProbability(slot, probability);
		return this;
	}

	@ZenMethod
	public IRecipeBuilder setProbability(String slot, float probability) {
		object.setProbability(getMachine().getSlot(slot).object, probability);
		return this;
	}

	@ZenMethod
	public IRecipeBuilder setOutput(IMachineSlot slot, IIngredient stack, int probability) {
		setOutput(slot, stack);
		setProbability(slot, probability);
		return this;
	}

	@ZenMethod
	public IRecipeBuilder setProbability(IMachineSlot slot, float probability) {
		object.setProbability(slot.object, probability);
		return this;
	}

	@ZenMethod
	public void register() {
		GenericAction.run(() -> object.getMachine().recipes_registry.addRecipe(object.build()), "Adding recipe for [" + String.join("+", trackedOutputs.values()) + "]  to " + object.getMachine().name);
	}

	@Nonnull
	@ZenGetter("machine")
	public IMachine getMachine() {
		return new IMachine(object.getMachine());
	}
}
