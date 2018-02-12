package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.api.machine.XUMachineGenerators;
import com.rwtema.extrautils2.crafting.BurnList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class FurnaceRecipe extends EnergyBaseRecipe {
	public FurnaceRecipe() {
		super(XUMachineGenerators.INPUT_ITEM);
	}

	@Nonnull
	@Override
	public Collection<ItemStack> getInputValues() {
		return BurnList.getStacks(false);
	}

	@Override
	public List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> getJEIInputItemExamples() {
		List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> list = new ArrayList<>();
		list.add(Pair.of(ImmutableMap.of(inputSlot, BurnList.getStacks(false)), ImmutableMap.of()));
		return list;
	}

	@Override
	public int getEnergyOutput(@Nonnull ItemStack itemStack) {
		if (itemStack.getItem() == Items.LAVA_BUCKET) return 0;
		return TileEntityFurnace.getItemBurnTime(itemStack) * 10;
	}
}
