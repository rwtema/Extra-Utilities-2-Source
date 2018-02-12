package com.rwtema.extrautils2.render;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.blocks.BlockScreen;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.TESRCompat;
import com.rwtema.extrautils2.textures.ImgurTextureHolder;
import com.rwtema.extrautils2.tile.TileScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class TileScreenRenderer extends TESRCompat<TileScreen> {
	public static TileScreenRenderer instance = new TileScreenRenderer();

	static float[][] offsets = new float[][]{
			null, null,
			{0, 0, BlockScreen.SIZE},
			{0, 0, 1 - BlockScreen.SIZE},
			{BlockScreen.SIZE, 0, 0},
			{1 - BlockScreen.SIZE, 0, 0},
	};

	static float[][][] vecs = new float[][][]{
			null,
			null,
			{//north
					{0, 0, 0},
					{0, 1, 0},
					{1, 1, 0},
					{1, 0, 0},
			},
			{//south
					{1, 0, 0},
					{1, 1, 0},
					{0, 1, 0},
					{0, 0, 0},
			},
			{//west
					{0, 0, 1},
					{0, 1, 1},
					{0, 1, 0},
					{0, 0, 0},
			},
			{//east
					{0, 0, 0},
					{0, 1, 0},
					{0, 1, 1},
					{0, 0, 1},
			},
	};

	EnumFacing[] right = new EnumFacing[]{
			null, null,
			EnumFacing.EAST,
			EnumFacing.EAST,
			EnumFacing.SOUTH,
			EnumFacing.SOUTH,
	};

	@Override
	public void render(@Nonnull TileScreen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (!te.active) return;
		EnumFacing side = te.getBlockState().getValue(XUBlockStateCreator.ROTATION_HORIZONTAL);

		int s = side.ordinal();
		EnumFacing right = this.right[s];

		if (te.canJoinWith(te.getPos().down()) || te.canJoinWith(te.getPos().offset(right, -1))) return;

		int w = 1;
		int h = 1;

		while (w < 8) {
			if (te.canJoinWith(te.getPos().offset(right, w)) && !te.canJoinWith(te.getPos().offset(right, w).down())) {
				w++;
			} else
				break;
		}


		heightLoop:
		while (h < 8) {
			for (int i = 0; i < w; i++) {
				if (!te.canJoinWith(te.getPos().up(h).offset(right, i)))
					break heightLoop;
			}
			h++;
		}


		ImgurTextureHolder tex = ImgurTextureHolder.getTex(te.id);

		if (tex.width == 0 || tex.height == 0) return;

		bindTexture(tex.getResourceLocationForBinding());


		GlStateManager.pushMatrix();

		GlStateManager.translate(x, y, z);
		GlStateManager.depthMask(true);
		GlStateManager.disableLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer worldrenderer = CompatClientHelper.wrap(tessellator.getBuffer());
		float[][] uvs = new float[][]{
				{tex.u0, tex.v1},
				{tex.u0, tex.v0},
				{tex.u1, tex.v0},
				{tex.u1, tex.v1},
		};

		float[] offset = offsets[s];
		GlStateManager.translate(offset[0], offset[1], offset[2]);

		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

		float scale = Math.min((float) w / tex.width, (float) h / tex.height);

		float dx, dy;

		dx = tex.width * scale - 0.03125f;
		dy = tex.height * scale - 0.03125f;


		float x0 = (w - dx) / 2;
		float y0 = (h - dy) / 2;

		for (int i = 3; i >= 0; i--) {
			float[] vec = vecs[s][i];
			float[] uv = uvs[i];
			worldrenderer.pos(s >= 4 ? 0 : (x0 + vec[0] * dx), y0 + vec[1] * dy, s < 4 ? 0 : (x0 + vec[2] * dx)).tex(uv[0], uv[1]).endVertex();
		}

		tessellator.draw();

		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}
}
