package com.rwtema.extrautils2.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class RenderEntityAngel extends RenderGhast {
	private static final ResourceLocation GHAST_TEXTURES = new ResourceLocation("textures/entity/ghast/ghast.png");
	private static final ResourceLocation GHAST_SHOOTING_TEXTURES = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

	public RenderEntityAngel(RenderManager renderManagerIn) {
		super(renderManagerIn);


	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGhast entity) {
		return entity.isAttacking() ? GHAST_SHOOTING_TEXTURES : GHAST_TEXTURES;
	}

	public static class ModelAngel extends ModelBase {
		ModelRenderer body;
		ModelRenderer[] tentacles = new ModelRenderer[9];

		public ModelAngel() {
			int i = -16;
			this.body = new ModelRenderer(this, 0, 0).setTextureSize(128, 64);
			this.body.addBox(-16.0F, -16.0F, -16.0F, 32, 32, 32);
			this.body.rotationPointY += 16.0F;
			Random random = new Random(1660L);

			for (int j = 0; j < this.tentacles.length; ++j) {
				this.tentacles[j] = new ModelRenderer(this, 0, 0).setTextureSize(128, 64);
				float f = (((float) (j % 3) - (float) (j / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
				float f1 = ((float) (j / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
				int k = random.nextInt(7) + 8;
				this.tentacles[j].addBox(-2.0F, 0.0F, -2.0F, 4, k * 2, 4);
				this.tentacles[j].rotationPointX = f * 2;
				this.tentacles[j].rotationPointZ = f1 * 2;
				this.tentacles[j].rotationPointY = 15.0F * 2;
			}
		}


		/**
		 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
		 * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
		 * "far" arms and legs can swing at most.
		 */
		public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
			for (int i = 0; i < this.tentacles.length; ++i) {
				this.tentacles[i].rotateAngleX = 0.2F * MathHelper.sin(ageInTicks * 0.3F + (float) i) + 0.4F;
			}
		}

		/**
		 * Sets the models various rotation angles then renders the model.
		 */
		public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.6F, 0.0F);
			this.body.render(scale);

			for (ModelRenderer modelrenderer : this.tentacles) {
				modelrenderer.render(scale);
			}

			GlStateManager.popMatrix();
		}
	}


}
