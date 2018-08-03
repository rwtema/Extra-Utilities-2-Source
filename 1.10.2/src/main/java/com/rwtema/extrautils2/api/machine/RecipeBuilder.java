package com.rwtema.extrautils2.api.machine;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public interface RecipeBuilder {
	static RecipeBuilder newbuilder(Machine machine) {
		Function<Machine, RecipeBuilder> builder = Builder.builder;
		if (builder == null)
			throw new RuntimeException("ExtraUtils2 not present and available.");

		return builder.apply(machine);
	}

	RecipeBuilder setEnergy(int amountToAddToBuffer);

	RecipeBuilder setProcessingTime(int time);

	RecipeBuilder setRFRate(int energy, float rfRate);

	RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull ItemStack input);

	RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull ItemStack input, int n);

	RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull String string, int n);

	RecipeBuilder setItemInput(@Nonnull MachineSlotItem slot, @Nonnull List<ItemStack> stacks, int n);

	RecipeBuilder setItemOutput(@Nonnull MachineSlotItem slot, ItemStack stack);

	RecipeBuilder setItemOutput(MachineSlotItem slot, ItemStack stack, int amount);

	RecipeBuilder setItemOutput(@Nonnull MachineSlotItem slot, String oreName, int amount);

	RecipeBuilder setItemOutput(@Nonnull MachineSlotItem slot, List<ItemStack> stacks, int amount);

	RecipeBuilder setFluidInputFluidName(@Nonnull MachineSlotFluid slot, @Nonnull String fluidName, int n);

	RecipeBuilder setFluidInputFluidStack(@Nonnull MachineSlotFluid slot, @Nonnull FluidStack input);

	RecipeBuilder setFluidInputFluidStack(@Nonnull MachineSlotFluid slot, @Nonnull FluidStack input, int n);

	RecipeBuilder setFluidInputFluidNameList(@Nonnull MachineSlotFluid slot, @Nonnull List<String> fluidNames, int n);

	RecipeBuilder setFluidInputFluidStackList(@Nonnull MachineSlotFluid slot, @Nonnull List<FluidStack> stacks, int n);

	RecipeBuilder setFluidOutput(@Nonnull MachineSlotFluid slot, FluidStack stack);

	RecipeBuilder setFluidOutput(@Nonnull MachineSlotFluid slot, FluidStack stack, int amount);

	RecipeBuilder setFluidOutput(@Nonnull MachineSlotFluid slot, String name, int amount);

	RecipeBuilder setProbability(@Nonnull MachineSlot outputSlot, float probability);

	IMachineRecipe build();

	@Nonnull
	Machine getMachine();


	class Builder {
		@Nullable
		public static Function<Machine, RecipeBuilder> builder = null;
	}
}
