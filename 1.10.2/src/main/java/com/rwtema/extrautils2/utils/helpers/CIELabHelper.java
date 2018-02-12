package com.rwtema.extrautils2.utils.helpers;

public class CIELabHelper {
	private static final float DELTA = 6F / 29F;
	private static final float DELTA2 = DELTA * DELTA;
	private static final float DELTA3 = DELTA2 * DELTA;
	private static final float xr = 0.964221f;
	private static final float yr = 1.0f;
	private static final float zr = 0.825211f;
	private static final double EPS = 0.06475065; //0.04045;

	public static int changeColor(int inputColor, int targetColor) {
		float[] a = rgb2lab(inputColor, new float[3]);
		float[] b = rgb2lab(targetColor, new float[3]);
		float[] rgb = new float[3];
		lab2rgb((a[0] + 3*b[0]) / 4, b[1], b[2], rgb);

		return ColorHelper.colorClamp(rgb[0], rgb[1], rgb[2], ColorHelper.getAF(inputColor));
	}


	public static float[] lab2rgb(float l, float la, float lb, float[] rgb) {

		float Ls = (l - 0.5F) / 2.55F;
		float as = la - 0.5F;
		float bs = lb - 0.5F;

		float fy = (Ls + 16F) / 116F;
		float fx = fy + as / 500F;
		float fz = fy - bs / 200F;

		float xr = applyFInv(fx);
		float yr = applyFInv(fy);
		float zr = applyFInv(fz);

		float X = xr * CIELabHelper.xr;
		float Y = yr * CIELabHelper.yr;
		float Z = zr * CIELabHelper.zr;

		float r, g, b;

		r = clamp(3.13405134F * X - 1.6170277F * Y - 0.49065221F * Z);
		g = clamp(-0.97876273F * X + 1.9161422F * Y + 0.03344963F * Z);
		b = clamp(0.07194258F * X - 0.2289712F * Y + 1.40521831F * Z);

		rgb[0] = applyGInv(r);
		rgb[1] = applyGInv(g);
		rgb[2] = applyGInv(b);
		return rgb;
	}

	public static float clamp(float x) {
		if (x < 0) return 0;
		else if (x > 1) return 1;
		else return x;
	}

	public static float[] rgb2lab(int col, float[] lab) {
		return rgb2lab(
				ColorHelper.getRF(col),
				ColorHelper.getGF(col),
				ColorHelper.getBF(col),
				lab
		);
	}

	public static float[] rgb2lab(float r, float g, float b, float[] lab) {

		float X, Y, Z;
		// assuming sRGB (D65)
		r = applyG(r);
		g = applyG(g);
		b = applyG(b);


		X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
		Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
		Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

		float xr, yr, zr;
		// XYZ to Lab
		xr = X / CIELabHelper.xr;
		yr = Y / CIELabHelper.yr;
		zr = Z / CIELabHelper.zr;

		float fx, fy, fz;

		fx = applyF(xr);
		fy = applyF(yr);
		fz = applyF(zr);

		float Ls, as, bs;
		Ls = (116 * fy) - 16;
		as = 500 * (fx - fy);
		bs = 200 * (fy - fz);

		lab[0] = (2.55F * Ls + .5F);
		lab[1] = (as + .5F);
		lab[2] = (bs + .5F);
		return lab;
	}

	private static float applyG(float t) {
		if (t <= EPS)
			return t / 12;
		else
			return (float) Math.pow((t + 0.055) / 1.055, 2.4);
	}

	private static float applyGInv(float t) {
		if (t <= (EPS / 12))
			return t * 12;
		else
			return (float) (Math.pow(t, 1 / 2.4) * 1.055 - 0.055);
	}

	private static float applyF(float xr) {
		float x;
		if (xr > DELTA3)
			x = (float) Math.pow(xr, 1 / 3.0);
		else
			x = xr / (3 * DELTA2) + 4F / 29F;
		return x;
	}

	private static float applyFInv(float x) {
		float xr;
		if (x > DELTA)
			xr = (float) Math.pow(x, 3.0);
		else
			xr = (3 * DELTA2) * (x - 4F / 29F);
		return xr;
	}

	public static float[] lab2rgb(float[] labf, float[] rgb) {
		return lab2rgb(labf[0], labf[1], labf[2], rgb);
	}
}
