package com.rwtema.extrautils2.tile.tesr;

import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.TESRCompat;
import com.rwtema.extrautils2.tile.XUTile;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public abstract class XUTESRBase<T extends XUTile> extends TESRCompat<T> {
	@Override
	public void render(@Nonnull T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer vertexBuffer = CompatClientHelper.wrap(tessellator.getBuffer());
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}


		preRender(te, destroyStage);
		vertexBuffer.begin(getDrawMode(te), getVertexFormat(te));
		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, vertexBuffer);
		vertexBuffer.setTranslation(0, 0, 0);
		tessellator.draw();
		postRender(te, destroyStage);
		RenderHelper.enableStandardItemLighting();
	}

	protected VertexFormat getVertexFormat(T te) {
		return DefaultVertexFormats.BLOCK;
	}

	protected int getDrawMode(T te) {
		return GL11.GL_QUADS;
	}

	public void preRender(T te, int destroyStage) {

	}

	public void postRender(T te, int destroyStage) {

	}

	public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer) {
		BlockPos pos = te.getPos();
		renderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
		render(te, x, y, z, partialTicks, destroyStage, renderer);
	}

	public abstract void render(T te, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer);
}
