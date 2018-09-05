package com.rwtema.extrautils2.crafting.jei;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.gui.backend.DynamicGui;
import com.rwtema.extrautils2.tile.TileResonator;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class JEIResonatorHandler extends BlankRecipeCategory<JEIResonatorHandler.ResonatorWrapper> implements IRecipeHandler<TileResonator.ResonatorRecipe>, IRecipeCategory<JEIResonatorHandler.ResonatorWrapper> {


	public static final String uid = ExtraUtils2.MODID + ".resonator";
	public static final int recipeWidth = 116;
	public static final int BETWEEN_DIST = 60;
	public static final int slotX1 = (recipeWidth - 18 + BETWEEN_DIST) / 2;
	public static final int slotX0 = (recipeWidth - 18 - BETWEEN_DIST) / 2;
	public static final int arrowX = (recipeWidth - 22) / 2;
	IDrawable slotDrawable = XUJEIPlugin.jeiHelpers.getGuiHelper().getSlotDrawable();
	IDrawable background = XUJEIPlugin.jeiHelpers.getGuiHelper().createBlankDrawable(recipeWidth, 54);
	IDrawable arrowBack = XUJEIPlugin.jeiHelpers.getGuiHelper().createDrawable(DynamicGui.texWidgets, 98, 0, 22, 16);

	@Nonnull
	@Override
	public Class<TileResonator.ResonatorRecipe> getRecipeClass() {
		return TileResonator.ResonatorRecipe.class;
	}

	@Nonnull
	public String getRecipeCategoryUid() {
		return uid;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid(@Nonnull TileResonator.ResonatorRecipe recipe) {
		return uid;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull TileResonator.ResonatorRecipe recipe) {
		return new ResonatorWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(@Nonnull TileResonator.ResonatorRecipe recipe) {
		return true;
	}

	@Nonnull
	@Override
	public String getUid() {
		return uid;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return Lang.getItemName(XU2Entries.resonator.value);
	}


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
		slotDrawable.draw(minecraft, slotX0, 14);
		slotDrawable.draw(minecraft, slotX1, 14);
		arrowBack.draw(minecraft, arrowX, 14);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ResonatorWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, slotX0, 14);
		guiItemStacks.set(0, recipeWrapper.resonatorRecipe.input);

		guiItemStacks.init(1, false, slotX1, 14);
		guiItemStacks.set(1, recipeWrapper.resonatorRecipe.output);
	}


	public static class ResonatorWrapper extends BlankRecipeWrapper {
		private final TileResonator.ResonatorRecipe resonatorRecipe;
		String energyString;
		String txtString;

		public ResonatorWrapper(TileResonator.ResonatorRecipe resonatorRecipe) {
			this.resonatorRecipe = resonatorRecipe;
			energyString = Lang.translateArgs("%s GP", StringHelper.niceFormat(resonatorRecipe.energy / 100.0));
			txtString = resonatorRecipe.getRequirementText();
		}

		@Override
		public void getIngredients(@Nonnull IIngredients ingredients) {
			ingredients.setInputs(ItemStack.class, ImmutableList.of(resonatorRecipe.input));
			ingredients.setOutput(ItemStack.class, resonatorRecipe.output);
		}

//			@Override
//			public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
//				int stringWidth = minecraft.fontRenderer.getStringWidth(energyString);
//				minecraft.fontRenderer.drawSplitString(energyString, Math.max(slotX0, (recipeWidth - stringWidth) / 2), (18 - 9) / 2, BETWEEN_DIST, Color.gray.getRGB());
//			}

		@Override
		public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			int stringWidth = minecraft.fontRenderer.getStringWidth(energyString);
			minecraft.fontRenderer.drawSplitString(energyString, Math.max(slotX0, (recipeWidth - stringWidth) / 2), (18 - 9) / 2, BETWEEN_DIST, Color.gray.getRGB());
			stringWidth = minecraft.fontRenderer.getStringWidth(txtString);
			GlStateManager.pushMatrix();
			GlStateManager.translate(recipeWidth / 2F ,(18 - 9) / 2 + 18 + 12 ,0 );
			GlStateManager.scale(0.75,0.75, 1);
			minecraft.fontRenderer.drawString(txtString,
					 - stringWidth / 2, 0, Color.gray.getRGB());
			GlStateManager.popMatrix();
		}

		@Override
		public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
			return false;
		}
	}
}
