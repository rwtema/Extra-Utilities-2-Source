package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.machine.IMachineRecipe;
import com.rwtema.extrautils2.api.machine.MachineSlotFluid;
import com.rwtema.extrautils2.api.machine.MachineSlotItem;
import com.rwtema.extrautils2.api.machine.XUMachineGenerators;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.items.itemmatching.IMatcherMaker;
import com.rwtema.extrautils2.recipes.SingleInputStackMatchRecipeBase;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public abstract class EnergyBaseRecipe extends SingleInputStackMatchRecipeBase {
	static {
		MinecraftForge.EVENT_BUS.register(EnergyBaseRecipe.class);
	}

	public EnergyBaseRecipe(MachineSlotItem inputSlot) {
		super(inputSlot);
	}

	public EnergyBaseRecipe() {
		this(XUMachineGenerators.INPUT_ITEM);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void tooltip(ItemTooltipEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player == null) return;

		Container container = player.openContainer;
		if (container instanceof TileMachine.ContainerMachine) {
			TileMachine machine = ((TileMachine.ContainerMachine) container).machine;
			if (machine == null || machine.machine == null) return;

			ItemStack itemStack = event.getItemStack();
			for (IMachineRecipe recipe : machine.machine.recipes_registry) {
				if (recipe instanceof EnergyBaseRecipe) {
					int energyOutput = ((EnergyBaseRecipe) recipe).getEnergyOutput(itemStack);
					if (energyOutput > 0) {
						float energyRate = ((EnergyBaseRecipe) recipe).getEnergyRate(itemStack);

						event.getToolTip().add(Lang.translateArgs("Generates %s RF at %s RF/Tick", StringHelper.format(energyOutput), StringHelper.format(energyRate)));
						break;
					}
				}
			}
		}
	}


	public static Collection<ItemStack> getCreativeStacks(@Nullable Predicate<ItemStack> predicateItemStack, @Nullable Predicate<Item> predicateItem) {

		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
		for (Item item : Item.REGISTRY) {
			if (predicateItem == null || predicateItem.test(item)) {
				for (ItemStack stack : ExtraUtils2.proxy.getSubItems(item)) {
					if (predicateItemStack == null || predicateItemStack.test(stack)) {
						builder.add(stack);
					}
				}
			}
		}
		return builder.build();
	}

	public static Collection<ItemStack> createItemList(Item... items) {
		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
		for (Item item : items) {
			builder.addAll(ExtraUtils2.proxy.getSubItems(item));
		}
		return builder.build();
	}

	@Override
	public boolean matches(ItemStack stack) {
		return StackHelper.isNonNull(stack) && getEnergyOutput(stack) > 0;
	}

	@Override
	public Map<MachineSlotItem, ItemStack> getItemOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return ImmutableMap.of();
	}

	@Override
	public Map<MachineSlotFluid, FluidStack> getFluidOutputs(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return ImmutableMap.of();
	}

	public abstract int getEnergyOutput(@Nonnull ItemStack stack);

	@Override
	public int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(inputSlot);
		return StackHelper.isNonNull(stack) ? getEnergyOutput(stack) : 0;
	}

	@Override
	public int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		return Math.round((float) getEnergyOutput(inputItems, inputFluids) / getEnergyRate(inputItems, inputFluids));
	}

	public float getEnergyRate(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
		ItemStack stack = inputItems.get(inputSlot);
		return StackHelper.isNonNull(stack) ? getEnergyRate(stack) : 0;
	}

	protected abstract float getEnergyRate(@Nonnull ItemStack stack);

	@Nullable
	public ItemStack getContainer(ItemStack stack) {
		return ForgeHooks.getContainerItem(stack);
	}

	public static class EnergyBaseItem extends EnergyBaseRecipe {
		final IMatcherMaker itemRef;
		final int energy;
		final int rate;

		public EnergyBaseItem(IMatcherMaker itemRef, int energy) {
			this.itemRef = itemRef;
			this.energy = energy;
			this.rate = energy / (20 * 20);
		}

		public EnergyBaseItem(IMatcherMaker itemRef, int energy, int rate) {
			this.itemRef = itemRef;
			this.energy = energy;
			this.rate = rate;
		}


		@Nonnull
		@Override
		public Collection<ItemStack> getInputValues() {
			return itemRef.getSubItems();
		}

		@Override
		public int getEnergyOutput(@Nonnull ItemStack stack) {
			return itemRef.matchesItemStack(stack) ? energy : 0;
		}

		@Override
		protected float getEnergyRate(@Nonnull ItemStack stack) {
			return rate;
		}
	}
}
