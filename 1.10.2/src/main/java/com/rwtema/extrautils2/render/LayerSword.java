package com.rwtema.extrautils2.render;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.items.ItemLawSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class LayerSword implements LayerRenderer<AbstractClientPlayer> {
	public static final ResourceLocation temaSword = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/rwtema_sword.png");
	private final RenderPlayer renderPlayer;
	private int displayList = 0;

	public LayerSword(RenderPlayer renderPlayer) {
		this.renderPlayer = renderPlayer;
	}


	@Override
	public void doRenderLayer(@Nonnull AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		String name = entitylivingbaseIn.getGameProfile().getName();
		if (!ItemLawSword.EventHandlerSword.clientLawSwords.contains(name)) {
			if (!"RWTema".equals(name) || !entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE))
				return;
		}

		boolean holdingSword = false;
		ItemStack heldItem = entitylivingbaseIn.getHeldItemMainhand();
		if (StackHelper.isNonNull(heldItem) && heldItem.getItem() instanceof ItemLawSword) {
			holdingSword = true;
		}

		GL11.glPushMatrix();
		{
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlpha();
			ModelRenderer bipedBody = renderPlayer.getMainModel().bipedBody;
			bipedBody.postRender(0.0625F);
			float v = (bipedBody.cubeList.get(0).posZ2 - bipedBody.cubeList.get(0).posZ1) / 2;
			GL11.glTranslatef(0.0F, entitylivingbaseIn.isSneaking() ? 0.125F : 0, 0.0625F * v);


			Minecraft.getMinecraft().renderEngine.bindTexture(temaSword);
			if (displayList == 0) {
				this.displayList = GLAllocation.generateDisplayLists(2);
				GL11.glNewList(this.displayList, GL11.GL_COMPILE);
				renderSword(false);
				GL11.glEndList();
				GL11.glNewList(this.displayList + 1, GL11.GL_COMPILE);
				renderSword(true);
				GL11.glEndList();
			}

			GlStateManager.callList(this.displayList + (holdingSword ? 1 : 0));
		}
		GL11.glPopMatrix();


	}

	public void renderSword(boolean holdingSword) {

		GL11.glColor4f(1, 1, 1, 1);

		GL11.glTranslatef(0.0F, 0.3F - 0.0625F, 0);

		GL11.glRotatef(-20F, 0, 1, 0);
		GL11.glRotatef(-40F, 0, 0, 1);

		float h = 87;
		float h2 = holdingSword ? 20 : 0;
		float w = 18;
		float w2 = 5;
		float w3 = 13;
		double u = w2 / w;
		float h3 = h2 / h;

		GL11.glScalef(1.7F / h, 1.7F / h, 1.7F / h);
		GL11.glTranslatef(-w2 / 2, -h / 2, 0);


		IVertexBuffer t = CompatClientHelper.wrap(Tessellator.getInstance().getBuffer());
		t.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

		t.pos(0, h2, 0).tex(0, h3).normal(0, 0, -1).endVertex();
		t.pos(0, h, 0).tex(0, 1).normal(0, 0, -1).endVertex();
		t.pos(w2, h, 0).tex(u, 1).normal(0, 0, -1).endVertex();
		t.pos(w2, h2, 0).tex(u, h3).normal(0, 0, -1).endVertex();


		t.pos(w2, h2, w2).tex(u, h3).normal(0, 0, 1).endVertex();
		t.pos(w2, h, w2).tex(u, 1).normal(0, 0, 1).endVertex();
		t.pos(0, h, w2).tex(0, 1).normal(0, 0, 1).endVertex();
		t.pos(0, h2, w2).tex(0, h3).normal(0, 0, 1).endVertex();


		t.pos(w2, h2, w2).tex(u, h3).normal(1, 0, 0).endVertex();
		t.pos(w2, h, w2).tex(u, 1).normal(1, 0, 0).endVertex();
		t.pos(w2, h, 0).tex(0, 1).normal(1, 0, 0).endVertex();
		t.pos(w2, h2, 0).tex(0, h3).normal(1, 0, 0).endVertex();

		t.pos(0, h2, 0).tex(u, h3).normal(-1, 0, 0).endVertex();
		t.pos(0, h, 0).tex(u, 1).normal(-1, 0, 0).endVertex();
		t.pos(0, h, w2).tex(0, 1).normal(-1, 0, 0).endVertex();
		t.pos(0, h2, w2).tex(0, h3).normal(-1, 0, 0).endVertex();

		if (!holdingSword) {
			t.pos(0, 0, 0).tex(9 / w, 4 / h).normal(0, -1, 0).endVertex();
			t.pos(w2, 0, 0).tex(13 / w, 8 / h).normal(0, -1, 0).endVertex();
			t.pos(w2, 0, w2).tex(13 / w, 8 / h).normal(0, -1, 0).endVertex();
			t.pos(0, 0, w2).tex(9 / w, 4 / h).normal(0, -1, 0).endVertex();
		}


		t.pos(0, h, 0).tex(9 / w, 4 / h).normal(0, 1, 0).endVertex();
		t.pos(w2, h, 0).tex(13 / w, 8 / h).normal(0, 1, 0).endVertex();
		t.pos(w2, h, w2).tex(13 / w, 8 / h).normal(0, 1, 0).endVertex();
		t.pos(0, h, w2).tex(9 / w, 4 / h).normal(0, 1, 0).endVertex();

		if (!holdingSword) {
			t.pos(-3, 16, -3).tex(6 / w, 18 / h).normal(0, -1, 0).endVertex();
			t.pos(8, 16, -3).tex(17 / w, 18 / h).normal(0, -1, 0).endVertex();
			t.pos(8, 16, 8).tex(17 / w, 29 / h).normal(0, -1, 0).endVertex();
			t.pos(-3, 16, 8).tex(6 / w, 29 / h).normal(0, -1, 0).endVertex();


			t.pos(-3, 20, -3).tex(6 / w, 1 / h).normal(0, 1, 0).endVertex();
			t.pos(8, 20, -3).tex(17 / w, 1 / h).normal(0, 1, 0).endVertex();
			t.pos(8, 20, 8).tex(17 / w, 12 / h).normal(0, 1, 0).endVertex();
			t.pos(-3, 20, 8).tex(6 / w, 12 / h).normal(0, 1, 0).endVertex();


			t.pos(-3, 16, -3).tex(u, 12 / h).normal(0, 0, -1).endVertex();
			t.pos(-3, 20, -3).tex(u, 17 / h).normal(0, 0, -1).endVertex();
			t.pos(8, 20, -3).tex(1, 17 / h).normal(0, 0, -1).endVertex();
			t.pos(8, 16, -3).tex(1, 12 / h).normal(0, 0, -1).endVertex();


			t.pos(-3, 16, 8).tex(u, 12 / h).normal(0, 0, 1).endVertex();
			t.pos(-3, 20, 8).tex(u, 17 / h).normal(0, 0, 1).endVertex();
			t.pos(8, 20, 8).tex(1, 17 / h).normal(0, 0, 1).endVertex();
			t.pos(8, 16, 8).tex(1, 12 / h).normal(0, 0, 1).endVertex();


			t.pos(8, 16, 8).tex(u, 12 / h).normal(1, 0, 0).endVertex();
			t.pos(8, 20, 8).tex(u, 17 / h).normal(1, 0, 0).endVertex();
			t.pos(8, 20, -3).tex(1, 17 / h).normal(1, 0, 0).endVertex();
			t.pos(8, 16, -3).tex(1, 12 / h).normal(1, 0, 0).endVertex();


			t.pos(-3, 16, 8).tex(u, 12 / h).normal(-1, 0, 0).endVertex();
			t.pos(-3, 20, 8).tex(u, 17 / h).normal(-1, 0, 0).endVertex();
			t.pos(-3, 20, -3).tex(1, 17 / h).normal(-1, 0, 0).endVertex();
			t.pos(-3, 16, -3).tex(1, 12 / h).normal(-1, 0, 0).endVertex();
		}

		Tessellator.getInstance().draw();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
