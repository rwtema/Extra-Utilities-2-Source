package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public class CompatClientHelper {

	public static EnumActionResult processRightClick(Minecraft mc, EntityPlayerSP thePlayer, BlockPos blockpos, EnumHand enumhand, ItemStack itemstack) {
		return mc.playerController.processRightClickBlock(thePlayer, mc.world, blockpos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, enumhand);
	}


	public static IVertexBuffer wrap(BufferBuilder buffer) {
		return new Wrapper(buffer);
	}

	public static BufferBuilder unwrap(IVertexBuffer buffer) {
		return ((Wrapper) buffer).input;
	}

	public static class Wrapper implements IVertexBuffer {
		public final BufferBuilder input;

		public Wrapper(BufferBuilder input) {
			this.input = input;
		}

		@Override
		public void sortVertexData(float p_181674_1_, float p_181674_2_, float p_181674_3_) {
			input.sortVertexData(p_181674_1_, p_181674_2_, p_181674_3_);
		}

		@Override
		public void reset() {
			input.reset();
		}

		@Override
		public void begin(int glMode, VertexFormat format) {
			input.begin(glMode, format);
		}

		@Override
		public IVertexBuffer tex(double u, double v) {
			return checkWrap(input.tex(u, v));
		}

		@Override
		public IVertexBuffer lightmap(int p_187314_1_, int p_187314_2_) {
			return checkWrap(input.lightmap(p_187314_1_, p_187314_2_));
		}

		private IVertexBuffer checkWrap(BufferBuilder lightmap) {
			if (lightmap == input) return this;
			return wrap(lightmap);
		}

		@Override
		public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_) {
			input.putBrightness4(p_178962_1_, p_178962_2_, p_178962_3_, p_178962_4_);
		}

		@Override
		public void putPosition(double x, double y, double z) {
			input.putPosition(x, y, z);
		}

		@Override
		public int getColorIndex(int vertexIndex) {
			return input.getColorIndex(vertexIndex);
		}

		@Override
		public void putColorMultiplier(float red, float green, float blue, int vertexIndex) {
			input.putColorMultiplier(red, green, blue, vertexIndex);
		}

		@Override
		public void putColorRGB_F(float red, float green, float blue, int vertexIndex) {
			input.putColorRGB_F(red, green, blue, vertexIndex);
		}

		@Override
		public void putColorRGBA(int index, int red, int green, int blue) {
			input.putColorRGBA(index, red, green, blue, 255);
		}

		@Override
		public void putColorRGBA(int index, int red, int green, int blue, int p_178972_5_) {
			input.putColorRGBA(index, red, green, blue, p_178972_5_);
		}

		@Override
		public void noColor() {
			input.noColor();
		}

		@Override
		public IVertexBuffer color(float red, float green, float blue, float alpha) {
			return checkWrap(input.color(red, green, blue, alpha));
		}

		@Override
		public IVertexBuffer color(int red, int green, int blue, int alpha) {
			return checkWrap(input.color(red, green, blue, alpha));
		}

		@Override
		public void addVertexData(int[] vertexData) {
			input.addVertexData(vertexData);
		}

		@Override
		public void endVertex() {
			input.endVertex();
		}

		@Override
		public IVertexBuffer pos(double x, double y, double z) {
			return checkWrap(input.pos(x, y, z));
		}

		@Override
		public void putNormal(float x, float y, float z) {
			input.putNormal(x, y, z);
		}

		@Override
		public IVertexBuffer normal(float x, float y, float z) {
			return checkWrap(input.normal(x, y, z));
		}

		@Override
		public void setTranslation(double x, double y, double z) {
			input.setTranslation(x, y, z);
		}

		@Override
		public void finishDrawing() {
			input.finishDrawing();
		}

		@Override
		public ByteBuffer getByteBuffer() {
			return input.getByteBuffer();
		}

		@Override
		public VertexFormat getVertexFormat() {
			return input.getVertexFormat();
		}

		@Override
		public int getVertexCount() {
			return input.getVertexCount();
		}

		@Override
		public int getDrawMode() {
			return input.getDrawMode();
		}

		@Override
		public void putColor4(int argb) {
			input.putColor4(argb);
		}

		@Override
		public void putColorRGB_F4(float red, float green, float blue) {
			input.putColorRGB_F4(red, green, blue);
		}

		@Override
		public boolean isColorDisabled() {
			return input.isColorDisabled();
		}
	}
}
