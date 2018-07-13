package com.rwtema.extrautils2.backend.model;

import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BoxRotatable extends Box {
	public UV[][] faceVecs;

	public BoxRotatable(UV[][] faceVecs) {
		super(0, 0, 0, 1, 1, 1);
		this.faceVecs = faceVecs;
		rebuildBounds();
	}

	public BoxRotatable(float x0, float y0, float z0, float x1, float y1, float z1) {
		super(x0, y0, z0, x1, y1, z1);
		faceVecs = new UV[][]{
				{//down
						new UV(x1, y0, z0, 0),
						new UV(x1, y0, z1, 1),
						new UV(x0, y0, z1, 2),
						new UV(x0, y0, z0, 3),

				},
				{//up
						new UV(x0, y1, z0, 0),
						new UV(x0, y1, z1, 1),
						new UV(x1, y1, z1, 2),
						new UV(x1, y1, z0, 3),

				},
				{//north
						new UV(x0, y0, z0, 0),
						new UV(x0, y1, z0, 1),
						new UV(x1, y1, z0, 2),
						new UV(x1, y0, z0, 3),
				},
				{//south
						new UV(x1, y0, z1, 0),
						new UV(x1, y1, z1, 1),
						new UV(x0, y1, z1, 2),
						new UV(x0, y0, z1, 3),
				},
				{//west
						new UV(x0, y0, z1, 0),
						new UV(x0, y1, z1, 1),
						new UV(x0, y1, z0, 2),
						new UV(x0, y0, z0, 3),
				},
				{//east
						new UV(x1, y0, z0, 0),
						new UV(x1, y1, z0, 1),
						new UV(x1, y1, z1, 2),
						new UV(x1, y0, z1, 3),
				},
		};

		rebuildBounds();
	}

	@Override
	public Box setBounds(float x0, float y0, float z0, float x1, float y1, float z1) {
		if (faceVecs == null) {
			return super.setBounds(x0, y0, z0, x1, y1, z1);
		}
		rebuildBounds();

		return this;
	}

	protected void rebuildBounds() {
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		for (UV[] faceVec : faceVecs) {
			for (UV uv : faceVec) {
				minX = Math.min(uv.x, minX);
				maxX = Math.max(uv.x, maxX);
				minY = Math.min(uv.y, minY);
				maxY = Math.max(uv.y, maxY);
				minZ = Math.min(uv.z, minZ);
				maxZ = Math.max(uv.z, maxZ);
			}
		}
	}

	public BoxRotatable rotate(float theta, float rx, float ry, float rz, float ux, float uy, float uz) {
		float c = MathHelper.cos(theta * (3.14159265358979323846F / 180));
		float s = MathHelper.sin(theta * (3.14159265358979323846F / 180));
		return rotate(c, s, rz, ux, uy, uz, rx, ry);
	}

	public BoxRotatable rotate(float c, float s, float rx, float ry, float rz, float ux, float uy, float uz) {
		float d = c * c + s * s;
		if (d < 1e-6) return this;
		if (Math.abs(d - 1) > 1e-6) {
			d = MathHelper.sqrt(d);
			c = c / d;
			s = s / d;
		}

		float c2 = 1 - c;
		float uxy = ux * uy * c2;
		float uxz = ux * uz * c2;
		float uyz = uy * uz * c2;
		for (UV[] faceVec : faceVecs) {
			for (UV uv : faceVec) {
				float px = uv.x - rx, py = uv.y - ry, pz = uv.z - rz;

				uv.x = rx +
						px * (c + ux * ux * c2) +
						py * (uxy - uz * s) +
						pz * (uxz + uy * s);
				uv.y = ry +
						px * (uxy + uz * s) +
						py * (c + uy * uy * c2) +
						pz * (uyz - ux * s);
				uv.z = rz +
						px * (uxz - uy * s) +
						py * (uyz + ux * s) +
						pz * (c + uz * uz * c2);
			}
		}
		rebuildBounds();
		clearCache();
		return this;
	}

	@Override
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		if (side != null) return null;

		ArrayList<BakedQuad> quads = new ArrayList<>(6);
		for (int i = 0; i < faceVecs.length; i++) {
			UV[] faceVec = faceVecs[i];
			if (invisible[i]) continue;
			quads.add(QuadHelper.createBakedQuad(faceVec, texture, true, tint));
		}

		return quads;
	}

	@Override
	public Box setTextureBounds(float[][] bounds) {
		for (int i = 0; i < faceVecs.length; i++) {
			float[] bound = bounds[i];
			if (bound == null) continue;
			for (int j = 0; j < 4; j++) {
				UV uv = faceVecs[i][j];
				uv.setUVTextureBounds(bound[0] / 16F, bound[2] / 16F, bound[1] / 16F, bound[3] / 16F, j);
			}
		}
		return this;
	}

	@Override
	public Box copy() {
		UV[][] newVecs = new UV[faceVecs.length][4];
		for (int i = 0; i < faceVecs.length; i++) {
			for (int j = 0; j < 4; j++) {
				newVecs[i][j] = faceVecs[i][j].copy();
			}
		}
		return new BoxRotatable(newVecs);
	}
}
