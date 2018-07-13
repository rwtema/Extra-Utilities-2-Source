package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.util.math.MathHelper;

public class ColorHelper {
	public static int getA(int col) {
		return (col & 0xff000000) >>> 24;
	}

	public static int getR(int col) {
		return (col & 0x00ff0000) >> 16;
	}

	public static int getG(int col) {
		return (col & 0x0000ff00) >> 8;
	}

	public static int getB(int col) {
		return col & 0x000000ff;
	}

	public static float getRF(int col) {
		return getR(col) / 255.0F;
	}

	public static float getGF(int col) {
		return getG(col) / 255.0F;
	}

	public static float getBF(int col) {
		return getB(col) / 255.0F;
	}

	public static float getAF(int col) {
		return getA(col) / 255.0F;
	}

	public static int makeAlphaWhite(int alpha) {
		if (alpha <= 0) return 0;
		if (alpha >= 255) return -1;
		return ((alpha & 0xff) << 24) | 0xffffff;
	}

	public static int color(int r, int g, int b, int a) {
		return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	}

	public static float[] colToFloat(int rgb) {
		return new float[]{
				ColorHelper.getA(rgb) / 255F,
				ColorHelper.getR(rgb) / 255F,
				ColorHelper.getG(rgb) / 255F,
				ColorHelper.getB(rgb) / 255F};
	}

	public static int floatsToCol(float[] rgb) {
		return color((int) (rgb[1] * 255), (int) (rgb[2] * 255), (int) (rgb[3] * 255), (int) (rgb[0] * 255));
	}

	public static int brightness(int col) {
		return brightness(getR(col), getG(col), getB(col));
	}

	public static int brightness(int r, int g, int b) {
		return (r * 109 + g * 366 + b * 37) >> 9;
	}

	public static int makeGray(int b) {
		return color(b, b, b, 255);
	}

	public static int colorClamp(float r, float g, float b, float a) {
		return color(clamp(r), clamp(g), clamp(b), clamp(a));
	}

	public static int clamp(float f) {
		return MathHelper.clamp((int) (f * 255), 0, 255);
	}

	public static int multShade(int input, float perc) {
		if (perc >= 1 || input == 0) return input;
		if (perc <= 0) return input & 0xff000000;
		return color(
				Math.round(getR(input) * perc),
				Math.round(getG(input) * perc),
				Math.round(getB(input) * perc),
				getA(input)
		);
	}

	public static int alpha(int color, float alpha) {
		if (alpha == 0) return 0;
		float af = ColorHelper.getAF(color);
		if (af == 0) return 0;

		return colorClamp(ColorHelper.getRF(color),
				ColorHelper.getGF(color),
				ColorHelper.getBF(color),
				af * alpha
		);
	}
}
