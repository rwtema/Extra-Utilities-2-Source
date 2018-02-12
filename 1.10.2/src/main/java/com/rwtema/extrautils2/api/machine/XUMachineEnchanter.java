package com.rwtema.extrautils2.api.machine;

import net.minecraft.item.ItemStack;

import java.util.List;

public class XUMachineEnchanter {
	public final static MachineSlotItem INPUT = new MachineSlotItem("input");
	public final static MachineSlotItem INPUT_LAPIS = new MachineSlotItem("input_lapis", true, 64);
	public final static MachineSlotItem OUTPUT = new MachineSlotItem("output");


	public static Machine INSTANCE;

	public static void addRecipe(ItemStack input, ItemStack output, int lapisNum, int energy, String gemLapis) {
		RecipeBuilder builder = RecipeBuilder.newbuilder(INSTANCE);
		builder.setItemInput(INPUT, input);
		builder.setItemOutput(OUTPUT, output);
		if (lapisNum > 0) {
			builder.setItemInput(INPUT_LAPIS, gemLapis, lapisNum);
		}
		builder.setRFRate(energy, 40F);
		INSTANCE.recipes_registry.addRecipe(builder.build());
	}

	public static void addRecipe(List<ItemStack> input, int input_amount, ItemStack output, int lapisNum, int energy, String gemLapis) {
		RecipeBuilder builder = RecipeBuilder.newbuilder(INSTANCE);
		builder.setItemInput(INPUT, input, input_amount);
		builder.setItemOutput(OUTPUT, output);
		if (lapisNum > 0) {
			builder.setItemInput(INPUT_LAPIS, gemLapis, lapisNum);
		}
		builder.setRFRate(energy, 40F);
		INSTANCE.recipes_registry.addRecipe(builder.build());
	}
}
