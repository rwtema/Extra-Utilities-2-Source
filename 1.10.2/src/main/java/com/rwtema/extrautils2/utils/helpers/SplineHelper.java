package com.rwtema.extrautils2.utils.helpers;

public class SplineHelper {
	public static double[] splineParams(double p0, double p1, double d0, double d1) {
		return new double[]{
				2 * p0 - 2 * p1 + d0 + d1,
				-3 * p0 + 3 * p1 - 2 * d0 - d1,
				d0,
				p0
		};
	}

	public static double evalSpline(double t, double[] p) {
		return ((p[0] * t + p[1]) * t + p[2]) * t + p[3];
	}

}
