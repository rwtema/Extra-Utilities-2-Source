package com.rwtema.extrautils2.api.machine;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;

public class XUMachineFurnace {
	public final static MachineSlotItem INPUT = new MachineSlotItem("input");
	public final static MachineSlotItem OUTPUT = new MachineSlotItem("output");
	public final static Machine INSTANCE = new Machine("extrautils2:furnace", 10000, 80, ImmutableList.of(INPUT), ImmutableList.of(), ImmutableList.of(OUTPUT), ImmutableList.of(), "extrautils2:machine/furnace_off", "extrautils2:machine/furnace_on", Machine.EnergyMode.USES_ENERGY, 0xffffff, null, null, null, null).setDefaults(2000, 100);

	public static void addRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output) {
		RecipeBuilder recipeBuilder = RecipeBuilder.newbuilder(INSTANCE);
		recipeBuilder.setItemInput(INPUT, input.copy());
		recipeBuilder.setItemOutput(OUTPUT, output.copy());
		recipeBuilder.setEnergy(2000);
		recipeBuilder.setProcessingTime(200);
		INSTANCE.recipes_registry.addRecipe(recipeBuilder.build());
	}
}

