package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class BoxSingleQuad extends Box {
	private final UV[] vecs;
	public boolean addShading = true;
	boolean doubleSided = true;

	public BoxSingleQuad(Vec3d pos0, float u0, float v0,
						 Vec3d pos1, float u1, float v1,
						 Vec3d pos2, float u2, float v2,
						 Vec3d pos3, float u3, float v3) {
		this(
				new UV(pos0, u0, v0),
				new UV(pos1, u1, v1),
				new UV(pos2, u2, v2),
				new UV(pos3, u3, v3)
		);

	}

	public BoxSingleQuad(UV... vecs) {
		super(getBB(vecs, 0), getBB(vecs, 1), getBB(vecs, 2), getBB(vecs, 3), getBB(vecs, 4), getBB(vecs, 5));
		this.vecs = vecs;
	}

	public static float getBB(UV[] vecs, int i) {
		float k = i >= 3 ? Float.MIN_VALUE : Float.MAX_VALUE;
		for (UV vec : vecs) {
			final float t;
			switch (i) {
				case 0:
				case 3:
					t = vec.x;
					break;
				case 1:
				case 4:
					t = vec.y;
					break;
				case 2:
				case 5:
					t = vec.z;
					break;
				default:
					throw new IllegalArgumentException("Wrong Arg: " + i);

			}
			k = i >= 3 ? Math.max(k, t) : Math.min(k, t);
		}
		return k;
	}

	@Override
	public BoxSingleQuad copy() {
		UV[] v = new UV[vecs.length];
		for (int i = 0; i < vecs.length; i++) {
			v[i] = vecs[i].copy();
		}

		BoxSingleQuad box = new BoxSingleQuad(v);
		box.doubleSided = doubleSided;
		box.addShading = addShading;
		return box;
	}

	@Override
	public Box rotateY(int numRotations) {
		if (numRotations == 0) return this;
		if (numRotations < 0) {
			numRotations += 4;
		}
		numRotations = numRotations & 3;
		if (numRotations == 0) return this;
		clearCache();
		for (int i = 0; i < numRotations; i++) {
			BoxSingleQuad prev = copy();
			for (int i1 = 0; i1 < vecs.length; i1++) {
				UV uv = vecs[i1];
				UV pv = prev.vecs[i1];
				uv.x = pv.z;
				uv.z = 1 - pv.x;
			}
		}

		setBounds(getBB(vecs, 0), getBB(vecs, 1), getBB(vecs, 2), getBB(vecs, 3), getBB(vecs, 4), getBB(vecs, 5));

		return this;
	}


	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	public Box rotateToSide(EnumFacing dir) {
		if (dir == EnumFacing.DOWN) return this;

		BoxSingleQuad prev = this.copy();
		clearCache();
		for (int i = 0; i < vecs.length; i++) {
			UV vec = vecs[i];
			UV pv = prev.vecs[i];
			switch (dir) {
				case UP:// (0, 1, 0)
					vec.y = 1 - pv.y;
					vec.z = 1 - pv.z;
					break;

				case NORTH:// (0, 0, -1),
					vec.x = 1 - pv.x;
					vec.y = pv.z;
					vec.z = pv.y;
					break;

				case SOUTH:// (0, 0, 1),
					vec.y = pv.z;
					vec.z = 1 - pv.y;
					break;

				case WEST:// (-1, 0, 0),
					vec.x = pv.y;
					vec.y = pv.z;
					vec.z = pv.x;
					break;

				case EAST:// (1, 0, 0),
					vec.x = 1 - pv.y;
					vec.y = pv.z;
					vec.z = 1 - pv.x;
					break;

				default:
					break;
			}
		}

		setBounds(getBB(vecs, 0), getBB(vecs, 1), getBB(vecs, 2), getBB(vecs, 3), getBB(vecs, 4), getBB(vecs, 5));

		return this;
	}

	@Override
	public void clearCache() {

	}

	@Override
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		if (side != null) return null;

		if (doubleSided) {
			UV[] v2 = new UV[vecs.length];
			for (int i = 0; i < v2.length; i++) {
				v2[i] = vecs[vecs.length - 1 - i];
			}

			return ImmutableList.of(QuadHelper.createBakedQuad(vecs, texture, addShading, tint), QuadHelper.createBakedQuad(v2, texture, addShading, tint));
		} else {
			return ImmutableList.of(QuadHelper.createBakedQuad(vecs, texture, addShading, tint));
		}
	}

	public Box setDoubleSided(boolean doubleSided) {
		this.doubleSided = doubleSided;
		return this;
	}
}
