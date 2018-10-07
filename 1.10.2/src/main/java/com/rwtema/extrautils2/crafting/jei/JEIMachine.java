package com.rwtema.extrautils2.crafting.jei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.machine.BlockMachine;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import gnu.trove.map.TObjectFloatMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.rwtema.extrautils2.gui.backend.WidgetFluidBase.*;
import static com.rwtema.extrautils2.machine.TileMachine.SHAPE;

public class JEIMachine extends BlankRecipeCategory<JEIMachine.JEIMachineRecipe.Wrapper> implements IRecipeCategory<JEIMachine.JEIMachineRecipe.Wrapper> {

	public static final int height;
	public static final int sloty;
	public static final int texty;
	public static final int padding;
	public static final int arrowy;
	static IIngredientHelper<ItemStack> helper;
	static IIngredientRenderer<ItemStack> renderer;

	static {
		padding = 4;
		height = padding + 18 + 4 + 9 + 9 + padding;
		sloty = padding;
		texty = padding + 18 + 4;
		arrowy = sloty;
	}

	public final int width;
	public final IDrawableStatic arrowBack;
	final Machine machine;
	private final IDrawableAnimated arrow;
	IDrawable slotDrawable = XUJEIPlugin.jeiHelpers.getGuiHelper().getSlotDrawable();
	IDrawable background;
	IDrawable fluidTank;
	IDrawable fluidTankTicks;

	public JEIMachine(Machine machine) {
		this.machine = machine;
		width = (machine.itemInputs.size() + machine.itemOutputs.size()) * (4 + 18) + 4 + 22;


		IGuiHelper guiHelper = XUJEIPlugin.jeiHelpers.getGuiHelper();
		background = guiHelper.createBlankDrawable(width, height);

		arrowBack = guiHelper.createDrawable(DynamicGui.texWidgets, 98, 0, 22, 16);

		IDrawableStatic arrowDrawable = guiHelper.createDrawable(DynamicGui.texWidgets, 98, 16, 22, 16);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.LEFT, false);

		int i = SHAPE;
		fluidTank = guiHelper.createDrawable(DynamicGui.texWidgets, ux[i], uy[i], uw[i], uh[i]);
		fluidTankTicks = new DrawableConcat()
				.add(guiHelper.createDrawable(DynamicGui.texWidgets, ux2[i], uy2[i], uw2[i], uh[i] - 2), 0, 0)
				.add(guiHelper.createDrawable(DynamicGui.texWidgets, ux2[i] + uw2[i], uy2[i], uw2[i], uh[i] - 2), uw[i] - 2 - uw2[i], 0);
	}

	@Nonnull
	public static String getString(Machine machine) {
		return "xu2_machine_" + machine.name;
	}

	@Nonnull
	@Override
	public String getUid() {
		return getString(machine);
	}

	@Nonnull
	@Override
	public String getTitle() {
		return BlockMachine.getDisplayName(machine);
	}

	@Nonnull
	public String getModName() {
		return ExtraUtils2.MODID;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Nullable
	@Override
	public IDrawable getIcon() {
		return null;
	}


	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {
		int x = 0;
		for (MachineSlotFluid ignored : machine.fluidInputs) {
			fluidTank.draw(minecraft, x, sloty + 9 - uh[SHAPE] / 2);
			x += 4 + fluidTank.getWidth();
		}

		for (MachineSlotItem ignored : machine.itemInputs) {
			slotDrawable.draw(minecraft, x, sloty);
			x += 4 + 18;
		}

		arrowBack.draw(minecraft, x, arrowy);

		x += 4 + 22;

		for (MachineSlotItem ignored : machine.itemOutputs) {
			slotDrawable.draw(minecraft, x, sloty);
			x += 4 + 18;
		}

		for (MachineSlotFluid ignored : machine.fluidOutputs) {
			fluidTank.draw(minecraft, x, sloty + 9 - uh[SHAPE] / 2);
			x += 4 + fluidTank.getWidth();
		}

		arrow.draw(minecraft, machine.fluidInputs.size() * (4 + fluidTank.getWidth()) + machine.itemInputs.size() * (4 + 18), arrowy);
	}


	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull JEIMachineRecipe.Wrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		JEIMachineRecipe recipe = recipeWrapper.parentRecipe;

		IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

		final IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		int x = 0;

		int itemIndex = 0;
		int fluidIndex = 0;

		final HashMap<MachineSlotFluid, IGuiIngredient<FluidStack>> fluidinputStackGuiSlots = new HashMap<>();
		for (MachineSlotFluid fluidInput : machine.fluidInputs) {
			addFluidSlot(fluidStackGroup, x, fluidIndex, fluidInput);
			IGuiIngredient<FluidStack> iGuiIngredient = fluidStackGroup.getGuiIngredients().get(fluidIndex);
			fluidinputStackGuiSlots.put(fluidInput, iGuiIngredient);
			fluidStackGroup.set(fluidIndex, recipe.fluids.get(fluidInput));
			x += 4 + 18;
			fluidIndex++;
		}

		final HashMap<MachineSlotItem, IGuiIngredient<ItemStack>> inputStackGuiSlots = new HashMap<>();
		List<MachineSlotItem> itemInputs = machine.itemInputs;
		for (MachineSlotItem slot : itemInputs) {
			itemStackGroup.init(itemIndex, true, x, sloty);
			IGuiIngredient<ItemStack> iGuiIngredient = itemStackGroup.getGuiIngredients().get(itemIndex);
			inputStackGuiSlots.put(slot, iGuiIngredient);
			itemStackGroup.set(itemIndex, recipe.inputs.get(slot));
			x += 4 + 18;
			itemIndex++;
		}

		x += 4 + 22;

		HashMap<MachineSlotItem, Integer> itemOutputStacks = new HashMap<>();
		for (MachineSlotItem slot : machine.itemOutputs) {
			itemStackGroup.init(itemIndex, false, x, sloty);
			itemStackGroup.set(itemIndex, ImmutableList.of());
			itemOutputStacks.put(slot, itemIndex);

			itemIndex++;
			x += 4 + 18;
		}

		HashMap<MachineSlotFluid, Integer> fluidOutputStacks = new HashMap<>();
		for (MachineSlotFluid slot : machine.fluidOutputs) {
			addFluidSlot(fluidStackGroup, x, fluidIndex, slot);
			fluidStackGroup.set(fluidIndex, ImmutableList.of());
			fluidOutputStacks.put(slot, fluidIndex);

			fluidIndex++;
			x += 4 + 18;
		}

		recipeWrapper.localFluidInputSlots = fluidinputStackGuiSlots;
		recipeWrapper.localFluidOutputSlots = fluidOutputStacks;
		recipeWrapper.fluidGroupSlots = fluidStackGroup;

		recipeWrapper.localItemInputSlots = inputStackGuiSlots;
		recipeWrapper.localItemOutputSlots = itemOutputStacks;
		recipeWrapper.itemGroupSlots = itemStackGroup;
	}

	private void addFluidSlot(IGuiFluidStackGroup fluidStacks, int x, int itemIndex, MachineSlotFluid slot) {

		fluidStacks.init(itemIndex, false, x + 1, sloty + 1 + 9 - uh[SHAPE] / 2, fluidTank.getWidth() - 2, fluidTank.getHeight() - 2, slot.stackCapacity, true, fluidTankTicks);
	}


	public static class Handler implements IRecipeHandler<JEIMachineRecipe> {
		static Handler INSTANCE = new Handler();

		@Nonnull
		@Override
		public Class<JEIMachineRecipe> getRecipeClass() {
			return JEIMachineRecipe.class;
		}

		@Nonnull
		public String getRecipeCategoryUid() {
			throw new IllegalStateException();
		}

		@Nonnull
		@Override
		public String getRecipeCategoryUid(@Nonnull JEIMachineRecipe recipe) {
			return getString(recipe.machine);
		}

		@Nonnull
		@Override
		public IRecipeWrapper getRecipeWrapper(@Nonnull JEIMachineRecipe recipe) {
			return recipe.createWrapper();
		}

		@Override
		public boolean isRecipeValid(@Nonnull JEIMachineRecipe recipe) {
			return true;
		}
	}

	public static class JEIMachineRecipe {
		public Machine machine;
		public IMachineRecipe recipe;
		public Map<MachineSlotItem, List<ItemStack>> inputs;
		public Map<MachineSlotFluid, List<FluidStack>> fluids;

		public JEIMachineRecipe(Machine machine, IMachineRecipe recipe, Map<MachineSlotItem, List<ItemStack>> inputs, Map<MachineSlotFluid, List<FluidStack>> fluids) {
			this.machine = machine;
			this.recipe = recipe;
			this.inputs = inputs;
			this.fluids = fluids;
		}

		public Wrapper createWrapper() {
			return new Wrapper(this);
		}

		public static class Wrapper extends BlankRecipeWrapper implements IRecipeWrapper {
			public final JEIMachineRecipe parentRecipe;
			public HashMap<MachineSlotFluid, IGuiIngredient<FluidStack>> localFluidInputSlots;
			public HashMap<MachineSlotFluid, Integer> localFluidOutputSlots;
			public IGuiIngredientGroup<FluidStack> fluidGroupSlots;

			public HashMap<MachineSlotItem, IGuiIngredient<ItemStack>> localItemInputSlots;
			public HashMap<MachineSlotItem, Integer> localItemOutputSlots;
			public IGuiIngredientGroup<ItemStack> itemGroupSlots;

			public Wrapper(JEIMachineRecipe parentRecipe) {
				this.parentRecipe = parentRecipe;
			}

			@Override
			public void getIngredients(@Nonnull IIngredients ingredients) {
				ArrayList<List<ItemStack>> objects = new ArrayList<>();
				for (MachineSlotItem slot : parentRecipe.machine.itemInputs) {
					List<ItemStack> stacks = parentRecipe.inputs.get(slot);
					if(stacks != null) objects.add(stacks);
				}

				ingredients.setInputLists(ItemStack.class, objects);

				ArrayList<List<FluidStack>> fluidObjects = new ArrayList<>();
				for (MachineSlotFluid slot : parentRecipe.machine.fluidInputs) {
					List<FluidStack> list = parentRecipe.fluids.get(slot);
					if (list != null)
						fluidObjects.add(list);
				}

				ingredients.setInputLists(FluidStack.class, fluidObjects);

				Map<MachineSlotItem, ItemStack> items = buildMap(parentRecipe.machine.itemInputs, parentRecipe.inputs);
				Map<MachineSlotFluid, FluidStack> fluids = buildMap(parentRecipe.machine.fluidInputs, parentRecipe.fluids);

				Map<MachineSlotItem, ItemStack> itemOutputs = parentRecipe.recipe.getItemOutputs(items, fluids);
				List<ItemStack> collect = itemOutputs.values().stream().filter(StackHelper::isNonNull).collect(Collectors.toList());
				ingredients.setOutputs(ItemStack.class, collect);

				Map<MachineSlotFluid, FluidStack> fluidOutputs = parentRecipe.recipe.getFluidOutputs(items, fluids);
				List<FluidStack> collect1 = fluidOutputs.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
				ingredients.setOutputs(FluidStack.class, collect1);
			}

			public <K extends MachineSlot<V>, V> Map<K, V> buildMap(List<K> list, Map<K, List<V>> inputs) {
				ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
				for (K k : list) {
					List<V> objects = inputs.get(k);
					if (objects != null) {
						objects.stream().findAny().ifPresent(v -> builder.put(k, v));
					}
				}
				return builder.build();
			}

			@Override
			public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
				if (localItemInputSlots == null || localItemOutputSlots == null || itemGroupSlots == null) return;

				HashMap<MachineSlotItem, ItemStack> itemStacks = new HashMap<>();
				for (MachineSlotItem slot : parentRecipe.machine.itemInputs) {
					itemStacks.put(slot, localItemInputSlots.get(slot).getDisplayedIngredient());
				}

				HashMap<MachineSlotFluid, FluidStack> fluidStacks = new HashMap<>();
				for (MachineSlotFluid slot : parentRecipe.machine.fluidInputs) {
					fluidStacks.put(slot, localFluidInputSlots.get(slot).getDisplayedIngredient());
				}

				if (!parentRecipe.recipe.matches(itemStacks, fluidStacks)) {
					for (MachineSlotItem slotItem : parentRecipe.machine.itemOutputs) {
						itemGroupSlots.set(localItemOutputSlots.get(slotItem), ImmutableList.of());
					}
					for (MachineSlotFluid slot : parentRecipe.machine.fluidOutputs) {
						fluidGroupSlots.set(localFluidOutputSlots.get(slot), ImmutableList.of());
					}
					return;
				}

				Map<MachineSlotItem, ItemStack> itemOutputs = parentRecipe.recipe.getItemOutputsJEI(itemStacks, fluidStacks);
				for (MachineSlotItem slotItem : parentRecipe.machine.itemOutputs) {
					ItemStack itemStack = itemOutputs.get(slotItem);
					Integer slotIndex = localItemOutputSlots.get(slotItem);
					if (StackHelper.isNonNull(itemStack)) {
						itemGroupSlots.set(slotIndex, itemStack);
					} else {
						itemGroupSlots.set(slotIndex, ImmutableList.of());
					}
				}

				Map<MachineSlotFluid, FluidStack> fluidOutputs = parentRecipe.recipe.getFluidOutputsJEI(itemStacks, fluidStacks);
				for (MachineSlotFluid slotFluid : parentRecipe.machine.fluidOutputs) {
					FluidStack fluidStack = fluidOutputs.get(slotFluid);
					Integer slotIndex = localFluidOutputSlots.get(slotFluid);
					if (fluidStack != null) {
						fluidGroupSlots.set(slotIndex, fluidStack);
					} else {
						fluidGroupSlots.set(slotIndex, ImmutableList.of());
					}
				}

				int energyOutput = parentRecipe.recipe.getEnergyOutput(itemStacks, fluidStacks);
				int time = parentRecipe.recipe.getProcessingTime(itemStacks, fluidStacks);
				String format;
				switch (parentRecipe.machine.energyMode) {
					case USES_ENERGY:
						format = StringHelper.format(energyOutput) + "RF " + StringHelper.formatDurationSeconds(time, false);
						minecraft.fontRenderer.drawString(format, (recipeWidth - minecraft.fontRenderer.getStringWidth(format)) / 2, recipeHeight - 9, Color.gray.getRGB());
						break;
					case GENERATES_ENERGY:
						float rate = parentRecipe.recipe.getEnergyRate(itemStacks, fluidStacks);
						format = StringHelper.format(energyOutput) + "RF " + StringHelper.formatDurationSeconds(time, false) + " " + StringHelper.format(rate) + "RF/T";
						minecraft.fontRenderer.drawString(format, (recipeWidth - minecraft.fontRenderer.getStringWidth(format)) / 2, recipeHeight - 9, Color.gray.getRGB());
						break;
				}

				TObjectFloatMap<MachineSlot> probabilityModifier = parentRecipe.recipe.getProbabilityModifier(itemStacks, fluidStacks);
				if (probabilityModifier == null)
					return;

				int x = (parentRecipe.machine.itemInputs.size()) * (4 + 18);
				x += 4 + 22;

				for (MachineSlot<?> slot : Iterables.concat(parentRecipe.machine.itemOutputs, parentRecipe.machine.fluidOutputs)) {
					if (probabilityModifier.containsKey(slot)) {
						float v = probabilityModifier.get(slot);
						if (v <= 0.99) {
							minecraft.fontRenderer.drawString(toString(v), x, texty, Color.gray.getRGB());
						}
					}
					x += 4 + 18;
				}
			}

			public String toString(float v) {
				int round = Math.round(v * 100);
				return round + "%";
			}


			@Nullable
			@Override
			public List<String> getTooltipStrings(int mouseX, int mouseY) {
				return Collections.emptyList();
			}

			@Override
			public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
				return false;
			}
		}
	}

}
