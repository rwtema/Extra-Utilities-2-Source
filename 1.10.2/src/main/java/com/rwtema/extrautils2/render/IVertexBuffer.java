package com.rwtema.extrautils2.render;

import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.nio.ByteBuffer;

public interface IVertexBuffer {


	static IVertexBuffer getVertexBuffer(Tessellator tessellator) {
		return CompatClientHelper.wrap(tessellator.getBuffer());
	}

	void sortVertexData(float p_181674_1_, float p_181674_2_, float p_181674_3_);

	void reset();

	void begin(int glMode, VertexFormat format);

	IVertexBuffer tex(double u, double v);

	IVertexBuffer lightmap(int p_187314_1_, int p_187314_2_);

	void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_);

	void putPosition(double x, double y, double z);

	/**
	 * Gets the position into the vertex data buffer at which the given vertex's color data can be found, in {@code
	 * int}s.
	 */
	int getColorIndex(int vertexIndex);

	/**
	 * Modify the color data of the given vertex with the given multipliers.
	 */
	void putColorMultiplier(float red, float green, float blue, int vertexIndex);

	void putColorRGB_F(float red, float green, float blue, int vertexIndex);

	/**
	 * Write the given color data of 4 bytes at the given index into the vertex data buffer, accounting for system
	 * endianness.
	 */
	void putColorRGBA(int index, int red, int green, int blue);

	/**
	 * Disables color processing.
	 */
	void noColor();

	IVertexBuffer color(float red, float green, float blue, float alpha);

	IVertexBuffer color(int red, int green, int blue, int alpha);

	void addVertexData(int[] vertexData);

	void endVertex();

	IVertexBuffer pos(double x, double y, double z);

	void putNormal(float x, float y, float z);

	IVertexBuffer normal(float x, float y, float z);

	void setTranslation(double x, double y, double z);

	void finishDrawing();

	ByteBuffer getByteBuffer();

	VertexFormat getVertexFormat();

	int getVertexCount();

	int getDrawMode();

	void putColor4(int argb);

	void putColorRGB_F4(float red, float green, float blue);

	void putColorRGBA(int index, int red, int green, int blue, int alpha);

	boolean isColorDisabled();
}
