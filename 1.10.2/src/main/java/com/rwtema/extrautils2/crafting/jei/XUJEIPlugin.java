package com.rwtema.extrautils2.crafting.jei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.blocks.BlockTerraformer;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.EnchantRecipe;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.items.ItemFakeCopy;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.items.ItemUnstableIngots;
import com.rwtema.extrautils2.items.itemmatching.IMatcherMaker;
import com.rwtema.extrautils2.machine.ItemBlockMachine;
import com.rwtema.extrautils2.network.NetworkHandler;
import com.rwtema.extrautils2.structure.PatternRecipe;
import com.rwtema.extrautils2.tile.TileCrafter;
import com.rwtema.extrautils2.tile.TileResonator;
import com.rwtema.extrautils2.tile.TileTerraformer;
import com.rwtema.extrautils2.tile.TileTerraformerClimograph;
import com.rwtema.extrautils2.transfernodes.TileIndexer;
import com.rwtema.extrautils2.utils.ItemStackNonNull;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.LogHelper;
import mezz.jei.api.*;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@JEIPlugin
public class XUJEIPlugin extends BlankModPlugin implements IModPlugin {
	public static IJeiHelpers jeiHelpers;
	public static IRecipeRegistry recipeRegistry;
	public static IGuiHelper guiHelper;

	public XUJEIPlugin() {
		LogHelper.info("Extra Utilities 2 JEI Plugin - ACTIVATE!");
	}

	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
		if (XU2Entries.machineEntry.isActive()) {
			subtypeRegistry.registerSubtypeInterpreter(Validate.notNull(Item.getItemFromBlock(XU2Entries.machineEntry.value)), new MachineSubtypeInterpreter());
		}

		if (XU2Entries.unstableIngots.isActive()) {
			subtypeRegistry.registerSubtypeInterpreter(XU2Entries.unstableIngots.value, new UnstableIIngotInterpreter());
		}
	}

	@Override
	public void register(@Nonnull IModRegistry registry) {
		jeiHelpers = registry.getJeiHelpers();

		registry.addRecipeCategories(BlockPatternHandler.category);
		registry.addRecipeHandlers(BlockPatternHandler.handler);
		registry.addRecipes(PatternRecipe.recipeList);


		if (XU2Entries.itemFakeCopy.isActive() && !ItemFakeCopy.showAllItems()) {
			jeiHelpers.getItemBlacklist().addItemToBlacklist(XU2Entries.itemFakeCopy.newStack(1, OreDictionary.WILDCARD_VALUE));
		}

		if (XU2Entries.blockSpotlight.isActive()) {
			jeiHelpers.getItemBlacklist().addItemToBlacklist(XU2Entries.blockSpotlight.newStack(1, OreDictionary.WILDCARD_VALUE));
		}

		if (XU2Entries.sunCrystal.isActive())
			registry.addDescription(XU2Entries.sunCrystal.newStack(1), Lang.translate("Craft an empty Sun Crystal first, then throw the crystal on the ground in direct sunlight."));
		if (XU2Entries.blockEnderLilly.isActive())
			registry.addDescription(XU2Entries.blockEnderLilly.newStack(1), Lang.translate("Found in dungeon chests."));
		if (XU2Entries.blockRedOrchid.isActive())
			registry.addDescription(XU2Entries.blockRedOrchid.newStack(1), Lang.translate("Found in dungeon chests."));
		if (XU2Entries.itemIngredients.isActive()) {
			registry.addDescription(ItemIngredients.Type.EVIL_DROP.newStack(), Lang.translate("Rare drop from wither skeletons"));
			registry.addDescription(ItemIngredients.Type.DEMON_INGOT.newStack(),
					Lang.translate("extrautils2.text.demon_ingot_lore",
							"Legends tell of ancient creatures from before the dawn of humans, whose bodies were deformed by the heavens. " +
									"They fled to a strange realm beneath the earth and built elaborate temples of worship to their new homeland. " +
									"They infused the bones of their dead with dark energy and weaved constructs of living flame. " +
									"But their most powerful creations were lava wells where they would throw special metals, hoping that their tribute would be answered with riches."));
		}
		if (XU2Entries.cursedEarth.isActive())
			registry.addDescription(XU2Entries.cursedEarth.newStack(), Lang.translate("Right click soil with a drop of evil."));

		if (XU2Entries.terraformer.isActive()) {
			JEITerraformerHandler terraformer = new JEITerraformerHandler();
			registry.addRecipeCategoryCraftingItem(TileTerraformer.getStack(BlockTerraformer.Type.CONTROLLER), JEITerraformerHandler.uid);
			registry.addRecipeCategories(terraformer);
			registry.addRecipeHandlers(terraformer);
			List<JEITerraformerHandler.Holder> list = new ArrayList<>();
			for (Map.Entry<BlockTerraformer.Type, Pair<IMatcherMaker, Integer>> entry : TileTerraformerClimograph.inputTypes.entries()) {
				list.add(new JEITerraformerHandler.Holder(entry.getKey(), entry.getValue().getKey(), TileTerraformerClimograph.INCREASE_MULTIPLIER * entry.getValue().getValue()));
			}
			registry.addRecipes(list);
		}

		if (XU2Entries.resonator.isActive()) {
			JEIResonatorHandler resonator = new JEIResonatorHandler();
			registry.addRecipeCategoryCraftingItem(XU2Entries.resonator.newStack(), JEIResonatorHandler.uid);
			registry.addRecipeCategories(resonator);
			registry.addRecipeHandlers(resonator);
			registry.addRecipes(TileResonator.resonatorRecipes);
		}

		if (XU2Entries.machineEntry.isActive()) {
			JEIMachine.helper = registry.getIngredientRegistry().getIngredientHelper(ItemStack.class);
			JEIMachine.renderer = registry.getIngredientRegistry().getIngredientRenderer(ItemStack.class);

			registry.addRecipeHandlers(JEIMachine.Handler.INSTANCE);

			for (Machine machine : MachineRegistry.getMachineValues()) {
				JEIMachine handler = new JEIMachine(machine);
				registry.addRecipeCategoryCraftingItem(XU2Entries.machineEntry.value.createStack(machine), handler.getUid());
				registry.addRecipeCategories(handler);

				for (IMachineRecipe recipe : machine.recipes_registry) {
					List<Pair<Map<MachineSlotItem, List<ItemStack>>, Map<MachineSlotFluid, List<FluidStack>>>> jeiInputItemExamples = recipe.getJEIInputItemExamples();
					List<JEIMachine.JEIMachineRecipe> collect = jeiInputItemExamples.stream().map(mapMapPair -> new JEIMachine.JEIMachineRecipe(machine, recipe, mapMapPair.getLeft(), mapMapPair.getRight())).collect(Collectors.toList());
					if (!collect.isEmpty()) {
						registry.addRecipes(collect);
					} else {
						long t = 0;
					}


				}
			}

		}

		registry.addRecipeHandlers(new JEIVanillaCraftingRecipeHandler<>(EnchantRecipe.class));

		if (XU2Entries.unstableIngots.isActive()) {
			registry.addRecipeHandlers(new JEIVanillaCraftingRecipeHandler<>(ItemUnstableIngots.UnstableIngotRecipe.class));
		}

		registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<DynamicGui>() {
			@Nonnull
			@Override
			public Class<DynamicGui> getGuiContainerClass() {
				return DynamicGui.class;
			}

			@Nullable
			@Override
			public List<Rectangle> getGuiExtraAreas(@Nonnull DynamicGui gui) {
				ArrayList<Rectangle> rectangles = Lists.newArrayList();
				for (DynamicWindow window : gui.container.getWindows()) {
					rectangles.add(new Rectangle(gui.guiLeft + window.x, gui.guiTop + window.y, window.w, window.h));
				}

				return rectangles;
			}

			@Nullable
			@Override
			public Object getIngredientUnderMouse(@Nonnull DynamicGui guiContainer, int mouseX, int mouseY) {
				for (IWidget widget : guiContainer.container.getWidgets()) {
					if (widget instanceof IWidgetCustomJEIIngredient && guiContainer.isInArea(mouseX, mouseY, widget)) {
						Object currentHeldStack = ((IWidgetCustomJEIIngredient) widget).getJEIIngredient();
						if (currentHeldStack != null) {
							return currentHeldStack;
						}
					}
				}
				return null;
			}
		});

		if (XU2Entries.indexer.enabled) {
			JEIIndexerTransfer.init(registry, registry.getRecipeTransferRegistry());
		}

		if (XU2Entries.blockCrafter.enabled) {
			registry.addRecipeCategoryCraftingItem(XU2Entries.blockCrafter.newStack(), VanillaRecipeCategoryUid.CRAFTING);
			IRecipeTransferHandler<TileCrafter.CrafterContainer> handler = new IRecipeTransferHandler<TileCrafter.CrafterContainer>() {
				@Nonnull
				@Override
				public Class<TileCrafter.CrafterContainer> getContainerClass() {
					return TileCrafter.CrafterContainer.class;
				}

				@Nonnull
				public String getRecipeCategoryUid() {
					return VanillaRecipeCategoryUid.CRAFTING;
				}

				@Nullable
				@Override
				public IRecipeTransferError transferRecipe(@Nonnull TileCrafter.CrafterContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
					if (!doTransfer) return null;
					Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();
					for (int i = 0; i < 9; i++) {
						IGuiIngredient<ItemStack> value = guiIngredients.get(1 + i);
						@ItemStackNonNull
						ItemStack stack;
						if (value != null) {
							List<ItemStack> allIngredients = value.getAllIngredients();

							if (!allIngredients.isEmpty()) {
								stack = allIngredients.get(0);
							} else {
								stack = StackHelper.empty();
							}
						} else {
							stack = StackHelper.empty();
						}

						WidgetSlotGhost ghostSlot = container.ghostSlots[i];
						int widgetID = container.getWidgets().indexOf(ghostSlot);
						NetworkHandler.sendPacketToServer(new DynamicContainer.PacketSetGhost(container.windowId, widgetID, stack));
					}
					return null;
				}
			};
			registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);

			guiHelper = registry.getJeiHelpers().getGuiHelper();

//			registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter();\
		}
	}

	@Override
	public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
		WidgetProgressArrowNetworkBase.setCategory = t -> jeiRuntime.getRecipesGui().showCategories(ImmutableList.of(t));
		TileTerraformerClimograph.lookUpRecipes = t -> jeiRuntime.getRecipesGui().show(jeiRuntime.getRecipeRegistry().createFocus(IFocus.Mode.INPUT, t));


		final IItemListOverlay itemListOverlay = jeiRuntime.getItemListOverlay();
//		ICloseable closable = (ItemListOverlay) itemListOverlay;
//		Radar.register(itemListOverlay, jeiRuntime);
		if (XU2Entries.indexer.enabled) {
			TileIndexer.ContainerIndexer.textInterface = new TileIndexer.ContainerIndexer.JeiTextInterface() {
				@Nonnull
				@Override
				public String getFilterText() {
					return itemListOverlay.getFilterText();
				}

				@Override
				public void setFilterText(@Nonnull String text) {
//					if(closable.isOpen()){
					itemListOverlay.setFilterText(text);
//					}
				}
			};
		}
		recipeRegistry = jeiRuntime.getRecipeRegistry();
	}

	private static class MachineSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter, Function<ItemStack, String> {
		@Nonnull
		@Override
		public String apply(@Nonnull ItemStack itemStack) {
			String subtypeInfo = getSubtypeInfo(itemStack);
			return subtypeInfo == null ? "" : subtypeInfo;
		}

		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
			if (!itemStack.hasTagCompound()) return "machine[blank]";
			Machine machineType = ItemBlockMachine.getMachineType(itemStack);
			return "machine[" + (machineType != null ? machineType.name : "unknown") + "]";
		}
	}

	private static class UnstableIIngotInterpreter implements ISubtypeRegistry.ISubtypeInterpreter, Function<ItemStack, String> {
		@Nonnull
		@Override
		public String apply(@Nonnull ItemStack itemStack) {
			String subtypeInfo = getSubtypeInfo(itemStack);
			return subtypeInfo == null ? "" : subtypeInfo;
		}

		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
			NBTTagCompound tagCompound = itemStack.getTagCompound();
			if (tagCompound != null && tagCompound.hasKey("time", Constants.NBT.TAG_ANY_NUMERIC)) {
				return "primed";
			}

			return "";
		}
	}

	public class SubType implements ISubtypeRegistry.ISubtypeInterpreter, Function<ItemStack, String> {
		HashSet<String> types;

		@Nonnull
		@Override
		public String apply(@Nonnull ItemStack itemStack) {
			String subtypeInfo = getSubtypeInfo(itemStack);
			return subtypeInfo == null ? "" : subtypeInfo;
		}

		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
			NBTTagCompound tagCompound = itemStack.getTagCompound();
			if (tagCompound == null) return null;
			return null;
		}
	}


}
