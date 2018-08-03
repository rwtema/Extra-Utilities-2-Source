package com.rwtema.extrautils2.crafting;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.recipes.IRecipeInfoWrapper;
import com.rwtema.extrautils2.backend.ISidedFunction;
import com.rwtema.extrautils2.compatibility.RecipeCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.WidgetCraftingMatrix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PlayerSpecificCrafting implements RecipeCompat, IRecipeInfoWrapper<IRecipe> {
	public final IRecipe recipe;
	public final IItemMatcher matcher;

	ResourceLocation location;

	public PlayerSpecificCrafting(ResourceLocation location, IRecipe recipe) {
		this.location = location;
		this.recipe = recipe;
		Validate.notNull(recipe.getRecipeOutput());
		matcher = recipe instanceof IItemMatcher ? (IItemMatcher) recipe : IItemMatcher.CRAFTING;
	}

	@Override
	public int getRecipeSize() {
		return RecipeCompat.getRecipeSize(recipe);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return recipe.getRecipeOutput();
	}

	@Nonnull
	@Override
	public ItemStack[] getRemainingItemsBase(@Nonnull InventoryCrafting inv) {
		ArrayList<ItemStack> stacks = new ArrayList<>();
		for (ItemStack itemStack : recipe.getRemainingItems(inv)) {
			stacks.add(itemStack);
		}
		return stacks.toArray(new ItemStack[stacks.size()]);
	}

	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
		if (!isGoodForCrafting(var1)) {
			return StackHelper.empty();
		}

		return recipe.getCraftingResult(var1);
	}

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
		return recipe.matches(inv, world);
	}

	public boolean isGoodForCrafting(final InventoryCrafting inv) {
		if (inv instanceof WidgetCraftingMatrix.XUInventoryCrafting) {
			EntityPlayer player = ((WidgetCraftingMatrix.XUInventoryCrafting) inv).player;
			if (player == null) return false;

			if (player instanceof EntityPlayerMP) {
				updatePlayer((EntityPlayerMP) player);
			}
			return isValidForCrafting(player);
		}

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			return ExtraUtils2.proxy.apply(new ClientIsGoodForCrafting(inv), null);
		} else {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (server != null) {
				PlayerList manager = server.getPlayerList();
				if (manager != null) {
					Container container = inv.eventHandler;
					if (container == null) return false;

					EntityPlayerMP foundPlayer = null;

					for (EntityPlayerMP entityPlayerMP : manager.getPlayers()) {
						if (entityPlayerMP.openContainer == container && container.canInteractWith(entityPlayerMP) && container.getCanCraft(entityPlayerMP)) {
							if (foundPlayer != null) return false;
							foundPlayer = entityPlayerMP;
						}
					}

					if (foundPlayer != null) {
						updatePlayer(foundPlayer);
						return isValidForCrafting(foundPlayer);
					}
				}
			}
			return false;
		}
	}

	protected abstract void updatePlayer(EntityPlayerMP foundPlayer);

	protected abstract boolean isValidForCrafting(EntityPlayer foundPlayer);

	@Override
	public IRecipe getOriginalRecipe() {
		return recipe;
	}

	@Override
	public abstract String info();

	protected abstract void finishedCrafting(EntityPlayer player, ItemStack recipeOutput);

	protected abstract void addTooltip(ItemTooltipEvent event, ItemStack itemStack);

	public IRecipe setRegistryName(ResourceLocation name) {
		location = name;
		return this;
	}

	@Nullable

	public ResourceLocation getRegistryName() {
		return location;
	}


	public Class<IRecipe> getRegistryType() {
		return IRecipe.class;
	}

	@Override
	public boolean canFit(int width, int height) {
		return ((RecipeCompat) recipe).canFit(width, height);
	}

	protected class EventHandler {
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void tooltip(ItemTooltipEvent event) {
			ItemStack itemStack = event.getItemStack();
			if (itemStack != recipe.getRecipeOutput()) return;
			addTooltip(event, itemStack);
		}

		@SubscribeEvent
		public void onCraft(PlayerEvent.ItemCraftedEvent event) {
			EntityPlayer player = event.player;
			if (player == null) {
				return;
			}

			ItemStack crafting = event.crafting;
			ItemStack recipeOutput = recipe.getRecipeOutput();
			if (StackHelper.isNull(crafting) || !matcher.itemsMatch(crafting, Validate.notNull(recipeOutput))) {
				return;
			}

			finishedCrafting(player, recipeOutput);
		}
	}

	private class ClientIsGoodForCrafting implements ISidedFunction<Void, Boolean> {
		private final InventoryCrafting inv;

		public ClientIsGoodForCrafting(InventoryCrafting inv) {
			this.inv = inv;
		}

		@Override
		@SideOnly(Side.SERVER)
		public Boolean applyServer(Void input) {
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Boolean applyClient(Void input) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			return player != null && isValidForCrafting(player) && player.openContainer == inv.eventHandler;
		}
	}
}
