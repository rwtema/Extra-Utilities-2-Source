package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.util.math.MathHelper;

public class LightMathHelper {
	static float[] APPROX_SQRT;

	static {
		APPROX_SQRT = new float[4096];
		for (int i = 0; i < APPROX_SQRT.length; i++) {
			APPROX_SQRT[i] = MathHelper.sqrt(i / 4096F);
		}
	}

	public static float approxSqrt(float v, float r) {
		if (v <= 0) return 0;
		if (v >= r) return 1;
		return APPROX_SQRT[(int) (v / r * 4096F) & 4095];
	}

	public static float[] norm(float a, float b, float c) {
		float a2 = a * a;
		float b2 = b * b;
		float c2 = c * c;
		float r = a2 + b2 + c2;
		return new float[]{approxSqrt(a2, r) * Math.signum(a), approxSqrt(b2, r) * Math.signum(b), approxSqrt(c2, r) * Math.signum(c)};
	}

	public static float partialDist(float x, float y, float z, float r) {
		return approxSqrt(x * x + y * y + z * z, r * r);
	}
}
