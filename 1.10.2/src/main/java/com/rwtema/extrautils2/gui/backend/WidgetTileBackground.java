package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class WidgetTileBackground extends WidgetBase {


	private final World world;
	private final BlockPos pos;

	public WidgetTileBackground(World world, BlockPos pos, int y, int size) {
		super((170 - size) / 2, y, size, size);
		this.world = world;
		this.pos = pos;
	}

	@Override
	public void addToContainer(DynamicContainer container) {
		super.addToContainer(container);
		x = (container.width - w) / 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

//		GlStateManager.enableBlend();
//		GlStateManager.disableAlpha();
//		GlStateManager.disableTexture2D();
//		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//		GlStateManager.color(0, 0, 0, 0.2F);
//
//		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelFromBlockState(world.getBlockState(pos), world, pos);
//		Tessellator instance = Tessellator.getInstance();
//		VertexBuffer t = instance.getBuffer();
//		t.begin(7, DefaultVertexFormats.POSITION);
//
//		renderQuads(t, model.getGeneralQuads(), guiLeft + x, guiTop + y, 0);
//		for (EnumFacing enumFacing : EnumFacing.values()) {
//			renderQuads(t, model.getFaceQuads(enumFacing), guiLeft + x, guiTop + y, 0);
//		}
//
//		instance.draw();
//
//		GlStateManager.enableTexture2D();
//		GlStateManager.enableAlpha();
//		GlStateManager.disableBlend();
	}

	public void renderQuads(IVertexBuffer t, List<BakedQuad> quadList, float x, float y, float z) {
		for (BakedQuad bakedQuad : quadList) {
			renderQuad(t, bakedQuad, x, y, z);
		}
	}

	private void renderQuad(IVertexBuffer t, BakedQuad bakedQuad, float x, float y, float z) {
		int[] vertexData = bakedQuad.getVertexData();
		for (int i = 0; i < 4; i++) {
			t.pos(
					x + w * (Float.intBitsToFloat(vertexData[i * 7])),
					y + h * (1 - Float.intBitsToFloat(vertexData[i * 7 + 1])),
					z).endVertex();
		}
	}

	@Override
	public List<String> getToolTip() {
		return null;
	}
}
