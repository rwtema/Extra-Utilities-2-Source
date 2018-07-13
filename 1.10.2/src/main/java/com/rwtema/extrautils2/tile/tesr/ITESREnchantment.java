package com.rwtema.extrautils2.tile.tesr;

import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ITESREnchantment<T extends XUTile> extends ITESRHookSimple<T> {

	static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	public static void renderModel(List<BakedQuad> listQuads, BlockPos pos, int color, double dx, double dy, double dz) {
		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer vertexbuffer = IVertexBuffer.getVertexBuffer(tessellator);
		int i = 0;

		for (int j = listQuads.size(); i < j; ++i) {
			BakedQuad bakedquad = listQuads.get(i);

			int[] data = Arrays.copyOf(bakedquad.getVertexData(), 28);

			Vec3d[] vec3ds = new Vec3d[4];
			float cx = 0, cy = 0, cz = 0;
			for (int k = 0; k < 4; ++k) {
				float x = Float.intBitsToFloat(data[k * 7]);
				cx += x / 4;
				float y = Float.intBitsToFloat(data[k * 7 + 1]);
				cy += y / 4;
				float z = Float.intBitsToFloat(data[k * 7 + 2]);
				cz += z / 4;
				vec3ds[k] = new Vec3d(x, y, z);
			}

			Vec3d center = new Vec3d(cx, cy, cz);

			Vec3d a = null;
			for (int l = 0; l < 4; l++) {
				a = vec3ds[l].subtract(center);
				if (a.equals(Vec3d.ZERO)) {
					a = null;
				} else {
					break;
				}
			}
			if (a == null) continue;

			Vec3d b = null;
			for (int l = 1; l < 4; l++) {
				b = vec3ds[l].subtract(center);
				if (b.equals(Vec3d.ZERO) || b.squareDistanceTo(a) < 0.01) {
					b = null;
				} else {
					break;
				}
			}
			if (b == null) continue;

			Vec3d normal = a.crossProduct(b).normalize();


			float v = 1 / 256F;

			for (int k = 0; k < 4; ++k) {
				Vec3d offset = vec3ds[k].subtract(center).normalize();
				data[k * 7] = Float.floatToRawIntBits((float) (Float.intBitsToFloat(data[k * 7]) + (offset.x + normal.x) * v));
				data[k * 7 + 1] = Float.floatToRawIntBits((float) (Float.intBitsToFloat(data[k * 7 + 1]) + (offset.x + normal.y) * v));
				data[k * 7 + 2] = Float.floatToRawIntBits((float) (Float.intBitsToFloat(data[k * 7 + 2]) + (offset.x + normal.z) * v));
			}

			vertexbuffer.setTranslation(0, 0, 0);
			vertexbuffer.begin(7, DefaultVertexFormats.ITEM);

			vertexbuffer.addVertexData(data);
			vertexbuffer.putColor4(color);
			vertexbuffer.putPosition(dx, dy, dz);

			vertexbuffer.putNormal((float) normal.x, (float) normal.y, (float) normal.z);
			tessellator.draw();
			vertexbuffer.setTranslation(0, 0, 0);

			GlStateManager.translate(-22.21232123, 0.0F, 0.0F);
			GlStateManager.rotate(76.2345123F, 0.0F, 0.0F, 1.0F);
		}
	}

	default boolean shouldShowEnchantment() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	default void renderTileEntityAt(T tile, double x, double y, double z, float partialTicks, int destroyStage) {
		if (!shouldShowEnchantment()) return;
		if (destroyStage != -1) return;
		IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(tile.getWorld(), tile.getPos());
		BlockPos pos = tile.getPos();

		IBlockState state = world.getBlockState(pos);
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		state = state.getBlock().getExtendedState(state, world, pos);
		long positionRandom = MathHelper.getPositionRandom(pos);

		ArrayList<BakedQuad> quads = new ArrayList<>();

		for (EnumFacing enumFacing : FacingHelper.facingPlusNull) {
			quads.addAll(model.getQuads(state, enumFacing, positionRandom));
		}

		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		GlStateManager.depthMask(false);

		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.pushMatrix();
		{
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			{
				int color = 0x8040ccff;

				long systemTime = Minecraft.getSystemTime() + 31232343 * MathHelper.getPositionRandom(tile.getPos());

				GlStateManager.pushMatrix();
				{
					GlStateManager.loadIdentity();
					GlStateManager.scale(8.0F, 8.0F, 8.0F);
					float f = (float) (systemTime % 3000L) / 3000.0F / 8.0F;
					GlStateManager.translate(f, 0.0F, 0.0F);
					GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);

					renderModel(quads, pos, color, x, y, z);
				}
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				{
					GlStateManager.loadIdentity();
					GlStateManager.scale(8.0F, 8.0F, 8.0F);
					float f1 = (float) (systemTime % 4873L) / 4873.0F / 8.0F;
					GlStateManager.translate(-f1, 0.0F, 0.0F);
					GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
					renderModel(quads, pos, color, x, y, z);
				}
				GlStateManager.popMatrix();

			}
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
		GlStateManager.popMatrix();

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.enableStandardItemLighting();
	}
}
