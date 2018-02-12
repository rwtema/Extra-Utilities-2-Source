package com.rwtema.extrautils2.crafting.jei;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.transfernodes.TileIndexer;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ItemRef;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class JEIIndexerTransfer {

	static ContainerIndexerSubContainer INSTANCE = new ContainerIndexerSubContainer();
	private static ContainerIndexerIRecipeTransferHandler recipeTransferHandler;

	public static void init(IModRegistry registry, IRecipeTransferRegistry recipeTransferRegistry) {
		recipeTransferHandler = new ContainerIndexerIRecipeTransferHandler();
		recipeTransferRegistry.addUniversalRecipeTransferHandler(recipeTransferHandler);
		recipeTransferRegistry.addRecipeTransferHandler(ContainerIndexerSubContainer.class, VanillaRecipeCategoryUid.CRAFTING, 0, 9, 10, 9 * 4 + 9 * 2);
		recipeTransferRegistry.addRecipeTransferHandler(new CraftingWrapper(), VanillaRecipeCategoryUid.CRAFTING);

	}

	private static class CraftingWrapper implements IRecipeTransferHandler<TileIndexer.ContainerIndexer> {


		private CraftingWrapper() {

		}

		@Nonnull
		@Override
		public Class<TileIndexer.ContainerIndexer> getContainerClass() {
			return TileIndexer.ContainerIndexer.class;
		}

		@Nonnull
		public String getRecipeCategoryUid() {
			return VanillaRecipeCategoryUid.CRAFTING;
		}


		@Nullable
		@Override
		public IRecipeTransferError transferRecipe(@Nonnull TileIndexer.ContainerIndexer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {

			IRecipeTransferHandler handler = XUJEIPlugin.recipeRegistry.getRecipeTransferHandler(INSTANCE, XUJEIPlugin.recipeRegistry.getRecipeCategories(ImmutableList.of(VanillaRecipeCategoryUid.CRAFTING)).get(0));
			IRecipeTransferError result = handler.transferRecipe(container, recipeLayout, player, maxTransfer, false);
			if (result == null) {
				if (doTransfer) {
					return handler.transferRecipe(container, recipeLayout, player, maxTransfer, true);
				}
				return null;
			}

			return JEIIndexerTransfer.recipeTransferHandler.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
		}
	}

	private static class ContainerIndexerIRecipeTransferHandler implements IRecipeTransferHandler<TileIndexer.ContainerIndexer> {

		@Nonnull
		@Override
		public Class<TileIndexer.ContainerIndexer> getContainerClass() {
			return TileIndexer.ContainerIndexer.class;
		}

		@Nonnull
		public String getRecipeCategoryUid() {
			return "universal recipe transfer handler";
		}

		@Nullable
		@Override
		public IRecipeTransferError transferRecipe(@Nonnull TileIndexer.ContainerIndexer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
			IRecipeTransferHandlerHelper handlerHelper = XUJEIPlugin.jeiHelpers.recipeTransferHandlerHelper();

			List<Set<ItemRef>> toFind = new ArrayList<>();

			IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

			Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = itemStackGroup.getGuiIngredients();


			for (IGuiIngredient<ItemStack> ingredient : guiIngredients.values()) {
				if (ingredient.isInput()) {
					Set<ItemRef> collect = ingredient.getAllIngredients().stream().map(ItemRef::wrapCrafting).collect(Collectors.toSet());
					if (collect.isEmpty()) continue;
					toFind.add(collect);
				}
			}

			ArrayList<ItemRef> toRequest = new ArrayList<>();
			ArrayList<ItemRef> list = container.list;
			for (ItemRef itemRef : list) {
				ItemRef craftingRef = itemRef.toCraftingVersion();
				ItemRef wildRef = craftingRef.toNoMetaVersion();
				for (Iterator<Set<ItemRef>> iterator = toFind.iterator(); iterator.hasNext(); ) {
					Set<ItemRef> itemRefs = iterator.next();
					if (itemRefs.contains(craftingRef) || itemRefs.contains(wildRef)) {
						toRequest.add(itemRef);
						iterator.remove();
					}
				}
				if (toFind.isEmpty())
					break;
			}

			if (toRequest.isEmpty()) {
				return handlerHelper.createUserErrorWithTooltip(Lang.translate(
						"Unable to find any ingredients"
				));
			}


			if (doTransfer) {
				for (ItemRef itemRef : toRequest) {
					XUPacketBuffer pkt = new XUPacketBuffer();
					if (itemRef == ItemRef.NULL) continue;
					itemRef.write(pkt);
					pkt.writeBoolean(maxTransfer);
					container.sendInputPacket(container.mainButton, pkt);
				}
			}

			return null;
		}
	}

	public static class ContainerIndexerSubContainer extends TileIndexer.ContainerIndexer {


		public ContainerIndexerSubContainer() {
			super();
		}
	}
}
