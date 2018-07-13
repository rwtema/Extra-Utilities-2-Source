package com.rwtema.extrautils2.entity;

import com.rwtema.extrautils2.backend.model.CachedRenderers;
import com.rwtema.extrautils2.backend.model.IClientClearCache;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.items.ItemBoomerang;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.utils.MCTimer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import java.util.List;

public class RenderEntityBoomerang extends Render<EntityBoomerang> implements IClientClearCache {
	TextureAtlasSprite sprite;
	List<BakedQuad> model;

	{
		CachedRenderers.register(this);
	}

	public RenderEntityBoomerang(RenderManager renderManager) {
		super(renderManager);
	}

	public static void renderModel(List<BakedQuad> bakedQuads) {
		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer worldrenderer = CompatClientHelper.wrap(tessellator.getBuffer());
		worldrenderer.begin(7, DefaultVertexFormats.ITEM);

		for (BakedQuad bakedquad : bakedQuads) {
			LightUtil.renderQuadColor(CompatClientHelper.unwrap(worldrenderer), bakedquad, -1);
		}

		tessellator.draw();
	}

	@Override
	public void doRender(@Nonnull EntityBoomerang entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (sprite == null) {
			sprite = Textures.sprites.get(ItemBoomerang.TEXTURE_NAME);
			if (sprite == null) sprite = Textures.MISSING_SPRITE;
			model = ItemLayerModel.getQuadsForSprite(-1, sprite, DefaultVertexFormats.ITEM, CompatHelper112.optionalOf(TRSRTransformation.identity()));
		}

		boolean flag = false;

		if (this.bindEntityTexture(entity)) {
			this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
			flag = true;
		}

		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.pushMatrix();


		GlStateManager.translate((float) x, (float) y, (float) z);

		GlStateManager.translate(0, 0.25F / 2, 0);
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 90.0F, 0.0F, -1.0F, 0.0F);

		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, -1.0F);
//		GlStateManager.rotate(180F+45.0F , 0, 1, 0);

		GlStateManager.rotate(-45F, 0, 0, 1);
		GlStateManager.rotate(90.0F, 1, 1, 0);

		GlStateManager.rotate(90.0F + MCTimer.renderTimer * 16, 0, 0, 1);


		GlStateManager.scale(0.5F, 0.5F, 0.5F);
////		GlStateManager.scale(0.5F, 0.5F, 0.5F);

		GlStateManager.translate(-0.5F, -0.5F, -0.5F);


		renderModel(model);

		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		this.bindEntityTexture(entity);

		if (flag) {
			this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Nonnull
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityBoomerang entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	@Override
	public void clientClear() {
		sprite = null;
		model = null;
	}
}
