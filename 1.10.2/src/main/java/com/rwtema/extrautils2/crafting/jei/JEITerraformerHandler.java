package com.rwtema.extrautils2.crafting.jei;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.blocks.BlockTerraformer;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.items.itemmatching.IMatcherMaker;
import com.rwtema.extrautils2.tile.TileTerraformer;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;

public class JEITerraformerHandler extends BlankRecipeCategory<JEITerraformerHandler.Wrapper> implements IRecipeHandler<JEITerraformerHandler.Holder> {
	public static final String uid = ExtraUtils2.MODID + ".terraformer";
	public static final int recipeWidth = 116;
	public static final int recipeheight = 18 + 17 * 2;
	public static final int arrowX = (recipeWidth - 22) / 2;
	public static final int slotX0 = arrowX - 4 - 18;
	public static final int slotX1 = arrowX + 4 + 22 + 2;

	public static final int arrowY = (recipeheight - 17) / 2;

	public static final int y0 = 0;
	public static final int y1 = 17;
	public static final int y2 = 17 + 18;

	IDrawable slotDrawable = XUJEIPlugin.jeiHelpers.getGuiHelper().getSlotDrawable();

	IDrawable background = XUJEIPlugin.jeiHelpers.getGuiHelper().createBlankDrawable(recipeWidth, recipeheight);
	IDrawable arrowBack = XUJEIPlugin.jeiHelpers.getGuiHelper().createDrawable(DynamicGui.texWidgets, 98, 0, 22, 16);


	@Nonnull
	@Override
	public String getUid() {
		return uid;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return TileTerraformer.getName(BlockTerraformer.Type.CONTROLLER);
	}


	public String getModName() {
		return ExtraUtils2.MODID;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull Wrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, slotX0, y0);
		guiItemStacks.set(0, TileTerraformer.getStack(BlockTerraformer.Type.ANTENNA));
		guiItemStacks.init(1, true, slotX0, y1);
		guiItemStacks.set(1, ImmutableList.copyOf(recipeWrapper.holder.stack.getSubItems()));
		guiItemStacks.init(2, true, slotX0, y2);
		guiItemStacks.set(2, TileTerraformer.getStack(recipeWrapper.holder.type));

	}

	@Nonnull
	@Override
	public Class<Holder> getRecipeClass() {
		return Holder.class;
	}

	@Nonnull
	public String getRecipeCategoryUid() {
		return uid;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid(@Nonnull Holder recipe) {
		return uid;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull Holder recipe) {
		return new JEITerraformerHandler.Wrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull Holder recipe) {
		return !recipe.stack.getSubItems().isEmpty();
	}

	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {
//		slotDrawable.draw(minecraft, slotX0, y0);
		slotDrawable.draw(minecraft, slotX0, y1);
//		slotDrawable.draw(minecraft, slotX0, y2);
		arrowBack.draw(minecraft, arrowX, arrowY);
	}


	public static class Holder {
		final BlockTerraformer.Type type;
		final IMatcherMaker stack;
		final int amount;

		public Holder(BlockTerraformer.Type type, IMatcherMaker stack, int amount) {
			this.type = type;
			this.stack = stack;
			this.amount = amount;
		}
	}

	public static class Wrapper extends BlankRecipeWrapper implements IRecipeWrapper {
		final Holder holder;

		public Wrapper(Holder holder) {
			this.holder = holder;
		}

		@Override
		public void getIngredients(@Nonnull IIngredients ingredients) {
			ingredients.setInputLists(ItemStack.class,
					ImmutableList.of(
							ImmutableList.of(TileTerraformer.getStack(BlockTerraformer.Type.ANTENNA)),
							ImmutableList.copyOf(holder.stack.getSubItems()),
							ImmutableList.of(TileTerraformer.getStack(holder.type))
					));
		}

		@Override
		public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			minecraft.fontRenderer.drawString(
					StringHelper.format(holder.amount) + " TF",
					slotX1, recipeheight / 2 - 4, Color.gray.getRGB());
		}
	}
}
