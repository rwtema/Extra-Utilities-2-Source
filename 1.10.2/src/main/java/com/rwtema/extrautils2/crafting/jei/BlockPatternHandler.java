package com.rwtema.extrautils2.crafting.jei;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.structure.PatternRecipe;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.MCTimer;
import com.rwtema.extrautils2.utils.client.GLStateAttributes;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockPatternHandler {
	public static final String uid = ExtraUtils2.MODID + ".blockPatterns";

	public static IRecipeCategory category = new BlankRecipeCategory() {
		public static final int recipeWidth = 160;
		public static final int recipeHeight = 160;
		IDrawable background = XUJEIPlugin.jeiHelpers.getGuiHelper().createBlankDrawable(recipeWidth, recipeHeight);

		@Nonnull
		@Override
		public String getUid() {
			return uid;
		}

		@Nonnull
		@Override
		public String getTitle() {
			return Lang.translate("Blocks");
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
		}


		@Override
		public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {

		}
	};
	public static IRecipeHandler<PatternRecipe> handler = new IRecipeHandler<PatternRecipe>() {

		@Nonnull
		@Override
		public Class<PatternRecipe> getRecipeClass() {
			return PatternRecipe.class;
		}

		@Nonnull
		public String getRecipeCategoryUid() {
			return uid;
		}

		@Nonnull
		@Override
		public String getRecipeCategoryUid(@Nonnull PatternRecipe recipe) {
			return uid;
		}

		@Nonnull
		@Override
		public IRecipeWrapper getRecipeWrapper(@Nonnull final PatternRecipe recipe) {


			return new BlankRecipeWrapper() {
				@Override
				public void getIngredients(@Nonnull IIngredients ingredients) {
					ingredients.setInputs(ItemStack.class, recipe.stacks);
					ingredients.setOutputs(ItemStack.class, recipe.stacks);
				}

				@Override
				public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
//
//				}
//
//
//
//				@Override
//				public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
					List<BlockPos> toRender = new ArrayList<>(recipe.toRender);
					if (toRender.isEmpty()) return;

					float angle = MCTimer.renderTimer * 45 / 12;
					double c = MathHelper.cos((float) (Math.PI * angle / 180.0));
					double s = MathHelper.sin((float) (Math.PI * angle / 180.0));

					toRender.sort((o1, o2) ->
							-Double.compare(
									o1.getZ() * c - o1.getX() * s,
									o2.getX() * c - o2.getX() * s
							));


					BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
					GLStateAttributes states = GLStateAttributes.loadStates();

					TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
					textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					ITextureObject texture = textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					texture.setBlurMipmap(false, false);

					GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
					GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
					GlStateManager.enableAlpha();
					GlStateManager.alphaFunc(516, 0.1F);
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.disableFog();
					GlStateManager.disableLighting();
					RenderHelper.disableStandardItemLighting();
					GlStateManager.enableBlend();
					GlStateManager.enableCull();
					GlStateManager.enableAlpha();

					if (Minecraft.isAmbientOcclusionEnabled()) {
						GlStateManager.shadeModel(7425);
					} else {
						GlStateManager.shadeModel(7424);
					}


					GlStateManager.pushMatrix();
					{
						GlStateManager.translate(recipeWidth / 2, recipeHeight / 2, 100);


						GlStateManager.rotate(-20, 1, 0, 0);

						GlStateManager.rotate(angle, 0, 1, 0);

						GlStateManager.scale(16, -16, 16);

						Tessellator tessellator = Tessellator.getInstance();
						IVertexBuffer buffer = IVertexBuffer.getVertexBuffer(tessellator);


						BlockPos mn = recipe.min;
						BlockPos mx = recipe.max;

						GlStateManager.enableCull();

						GlStateManager.translate(
								-(mn.getX() + mx.getX() + 1) / 2.0,
								-(mn.getY() + mx.getY() + 1) / 2.0,
								-(mn.getZ() + mx.getZ() + 1) / 2.0);

						buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
						GlStateManager.disableAlpha();
						renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.SOLID, toRender);
						GlStateManager.enableAlpha();
						renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT_MIPPED, toRender);
						renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT, toRender);
						GlStateManager.shadeModel(0x1d01);
						renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.TRANSLUCENT, toRender);
						tessellator.draw();
					}
					GlStateManager.popMatrix();
					states.restore();
					texture.restoreLastBlurMipmap();
				}

				public void renderLayer(BlockRendererDispatcher blockrendererdispatcher, IVertexBuffer buffer, BlockRenderLayer renderLayer, List<BlockPos> toRender) {
					for (BlockPos pos : toRender) {
						IBlockState state = recipe.posMap.get(pos);

						if (!state.getBlock().canRenderInLayer(state, renderLayer)) continue;
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(renderLayer);
						try {
							blockrendererdispatcher.renderBlock(state, pos, recipe.blockAccess, CompatClientHelper.unwrap(buffer));
						} catch (Exception err) {
							err.printStackTrace();
						}
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
					}
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
			};
		}

		@Override
		public boolean isRecipeValid(@Nonnull PatternRecipe recipe) {
			return true;
		}
	};

}
