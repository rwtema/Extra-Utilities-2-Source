package com.rwtema.extrautils2.render;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.items.ItemAngelRing;
import com.rwtema.extrautils2.utils.MCTimer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class LayerWings implements LayerRenderer<AbstractClientPlayer> {
	private static final ResourceLocation[] wing_textures = new ResourceLocation[]{
			null,
			new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/wing_feather.png"),
			new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/wing_butterfly.png"),
			new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/wing_demon.png"),
			new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/wing_golden.png"),
			new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/wing_bat.png"),
			new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/wing_chicken.png")
	};

	private final RenderPlayer renderPlayer;
	private int displayList = 0;

	public LayerWings(RenderPlayer renderPlayer) {
		this.renderPlayer = renderPlayer;
	}

	@Override
	public void doRenderLayer(@Nonnull AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		String name = entitylivingbaseIn.getGameProfile().getName();
		if (ItemAngelRing.clientFlyingPlayers.containsKey(name)) {
			int tex = ItemAngelRing.clientFlyingPlayers.get(name);
			if (tex <= 0 || tex >= wing_textures.length)
				return;

			GL11.glColor4f(1, 1, 1, 1);

			GL11.glPushMatrix();
			{
				ModelRenderer bipedBody = renderPlayer.getMainModel().bipedBody;
				bipedBody.postRender(0.0625F);
				Minecraft.getMinecraft().renderEngine.bindTexture(wing_textures[tex]);

				float v = (bipedBody.cubeList.get(0).posZ2 - bipedBody.cubeList.get(0).posZ1) / 2;

				GL11.glTranslatef(0.0F, entitylivingbaseIn.isSneaking() ? 0.125F : 0, 0.0625F * v);

				float a;


				boolean isFlying = entitylivingbaseIn.capabilities.isFlying;
				a = (1 + (float) Math.cos(MCTimer.renderTimer / 4)) * (isFlying ? 20 : 2) + 25;

				if (displayList == 0) {
					Tessellator instance = Tessellator.getInstance();
					IVertexBuffer t = CompatClientHelper.wrap(instance.getBuffer());
					this.displayList = GLAllocation.generateDisplayLists(2);
					GL11.glNewList(this.displayList, GL11.GL_COMPILE);
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glTranslatef(0.0F, -0.25F - 0.0625F, 0);
					t.begin(7, DefaultVertexFormats.POSITION_TEX);
					t.pos(0, 0, 0).tex(0, 0).endVertex();
					t.pos(0, 1, 0).tex(0, 1).endVertex();
					t.pos(1, 1, 0).tex(1, 1).endVertex();
					t.pos(1, 0, 0).tex(1, 0).endVertex();
					instance.draw();
					GL11.glEndList();

					GL11.glNewList(this.displayList + 1, GL11.GL_COMPILE);
					t.begin(7, DefaultVertexFormats.POSITION_TEX);
					GL11.glTranslatef(0.0F, -0.25F - 0.0625F, 0);
					t.pos(0, 0, 0).tex(0, 0).endVertex();
					t.pos(0, 1, 0).tex(0, 1).endVertex();
					t.pos(-1, 1, 0).tex(1, 1).endVertex();
					t.pos(-1, 0, 0).tex(1, 0).endVertex();
					instance.draw();
					GL11.glEndList();
				}

				GL11.glPushMatrix();
				{
					GL11.glRotatef(-a, 0, 1, 0);
					GlStateManager.callList(this.displayList);

				}
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				{
					GL11.glRotatef(a, 0, 1, 0);
					GlStateManager.callList(this.displayList + 1);
				}
				GL11.glPopMatrix();
			}
			GL11.glPopMatrix();
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
