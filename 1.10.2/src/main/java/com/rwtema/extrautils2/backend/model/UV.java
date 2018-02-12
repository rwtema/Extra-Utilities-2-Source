package com.rwtema.extrautils2.backend.model;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

public class UV {
	private static final float[] cornerUs = {0, 0, 1, 1};
	private static final float[] cornerVs = {0, 1, 1, 0};

	public float x;
	public float y;
	public float z;
	public float u;
	public float v;

	public UV(Vec3d pos, float u, float v) {
		this((float) pos.x, (float) pos.y, (float) pos.z, u, v);
	}

	public UV(float x, float y, float z, float u, float v) {
		this.x = x;
		this.y = y;
		this.z = z;

		this.u = u;
		this.v = v;
	}

	public UV(float x, float y, float z, int corner) {
		this(x, y, z, cornerUs[corner], cornerVs[corner]);
	}

	public UV(float x, float y, float z, int corner, float u1, float u2, float v1, float v2) {
		this(x, y, z, (Math.min(u1, u2) + Math.abs(u2 - u1) * cornerUs[corner]), (Math.min(v1, v2) + Math.abs(v2 - v1) * cornerVs[corner]));
	}

	public static UV interpolate(UV a, UV b, float p) {
		float q = 1 - p;
		return new UV(
				a.x * p + b.x * q,
				a.y * p + b.y * q,
				a.z * p + b.z * q,
				a.u * p + b.u * q,
				a.v * p + b.v * q
		);
	}

	public static UV interpolateQuad(UV[] uvs, float u, float v) {
		return interpolate(interpolate(uvs[0], uvs[1], u), interpolate(uvs[3], uvs[2], u), v);
	}

	public void setUVTextureBounds(float u1, float u2, float v1, float v2, int corner) {
		u = (u1 + (u2 - u1) * cornerUs[corner]);
		v = (v1 + (v2 - v1) * cornerVs[corner]);
	}

	public UV add(float d, EnumFacing side) {
		return new UV(
				x + side.getFrontOffsetX() * d,
				y + side.getFrontOffsetY() * d,
				z + side.getFrontOffsetZ() * d,
				u, v);
	}

	@SideOnly(Side.CLIENT)
	public Vector3f toVector3f() {
		return new Vector3f(x, y, z);
	}

	public void offset(float dx, float dy, float dz) {
		x += dx;
		y += dy;
		z += dz;
	}

	@Override
	public String toString() {
		return "UV{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				", u=" + u +
				", v=" + v +
				'}';
	}

	public UV copy() {
		return new UV(x, y, z, u, v);
	}
}
