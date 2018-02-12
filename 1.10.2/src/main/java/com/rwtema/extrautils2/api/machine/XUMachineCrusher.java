package com.rwtema.extrautils2.api.machine;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XUMachineCrusher {
	public final static MachineSlotItem INPUT = new MachineSlotItem("input");
	public final static MachineSlotItem OUTPUT = new MachineSlotItem("output");
	public final static MachineSlotItem OUTPUT_SECONDARY = new MachineSlotItem("output_secondary", true, 64);
	public final static Machine INSTANCE = new Machine("extrautils2:crusher", 20000, 80, ImmutableList.of(INPUT), ImmutableList.of(), ImmutableList.of(OUTPUT, OUTPUT_SECONDARY), ImmutableList.of(), "extrautils2:machine/crusher_off", "extrautils2:machine/crusher_on", Machine.EnergyMode.USES_ENERGY, 0xffffff, null, null, null, null).setDefaults(4000, 200);

	public static void addRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output) {
		addRecipe(input, output, null, 0);
	}

	public static void addRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output, @Nullable ItemStack outputSecondary, float outputSecondaryProbability) {
		RecipeBuilder recipeBuilder = RecipeBuilder.newbuilder(INSTANCE);
		recipeBuilder.setItemInput(INPUT, input.copy());
		recipeBuilder.setItemOutput(OUTPUT, output.copy());
		if (StackHelper.isNonNull(outputSecondary)) {
			recipeBuilder.setItemOutput(OUTPUT_SECONDARY, outputSecondary.copy());
			recipeBuilder.setProbability(OUTPUT_SECONDARY, outputSecondaryProbability);
		}
		recipeBuilder.setEnergy(4000);
		recipeBuilder.setProcessingTime(200);
		INSTANCE.recipes_registry.addRecipe(recipeBuilder.build());
	}
}
