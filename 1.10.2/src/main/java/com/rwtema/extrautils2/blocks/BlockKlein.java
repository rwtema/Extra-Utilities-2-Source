package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockTESR;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.BoxSingleQuad;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.function.DoubleBinaryOperator;

public class BlockKlein extends XUBlockTESR {
	static final double STEM_WIDTH = 0.1;
	static final double STEM_HEIGHT = 0.7;
	static final double LOOP_RADIUS = 1 - STEM_HEIGHT;
	static final double OFFSET = STEM_HEIGHT / 2;
	static final double OFFSET2 = 1 - STEM_HEIGHT / 2;
	static final double OFFSET3 = 0.1;

	public static double getStemRadius(double t) {
		if (t < 0) {
			t = (t + 10) % 1;
		} else if (t > 1)
			t = t % 1;

		if (t < OFFSET) {
			return getBulbWidth(t / OFFSET);
		} else if (t < OFFSET + OFFSET3) {
			double v = (t - OFFSET) / OFFSET3;
			v = (1 + Math.cos(v * Math.PI)) / 2;
			return (1 - v) * STEM_WIDTH + v * getBulbWidth(1);

		} else if (t > (1 - OFFSET3) && t < 1) {
			double v = (t - (1 - OFFSET3)) / OFFSET3;
			if (v > 1) v = 1;
			v = Math.sqrt(1 - v * v);
			return (1 - v) * STEM_WIDTH + v * getBulbWidth(0);
		} else
			return STEM_WIDTH;
	}

	public static double getBulbWidth(double t) {
		double a = STEM_WIDTH + 0.2 * circle(-1 + t * 3);
		double v2 = 0.8 * (1 - t) + t * STEM_WIDTH;
		return t * v2 + (1 - t) * a;
	}

	public static Vec3d getStemNormal(double t) {
		boolean isReturning;
		if (t < 0) {
			t = (t + 10) % 1;
		}

		if (t > 1) {
			isReturning = true;
			t = t % 1;
		} else
			isReturning = false;

		double x, y;
		if (t < OFFSET) {
			x = -1;
			y = 0;

		} else if (t < OFFSET2) {
			double k = (t - OFFSET) / (OFFSET2 - OFFSET) * Math.PI;
			x = -Math.cos(k);
			y = Math.sin(k);
		} else {
			double k = (t - OFFSET2) / (1 - OFFSET2);
			y = -Math.sin(k * Math.PI);
			double v = 1 / Math.sqrt(1 + y * y);
			x = v;
			y *= v;
		}

		if (isReturning) {
			x *= -1;
			y *= -1;
		}


		return new Vec3d(x, y, 0);
	}

	public static Vec3d getStemPos(double t) {
		if (t < 0) {
			t = (t + 10) % 1;
		} else if (t > 1)
			t = t % 1;

		final double x, y;
		if (t < OFFSET) {
			x = 0;
			y = t / OFFSET * STEM_HEIGHT;
		} else if (t < OFFSET2) {
			double k = (t - OFFSET) / (OFFSET2 - OFFSET) * Math.PI;
			x = LOOP_RADIUS - LOOP_RADIUS * Math.cos(k);
			y = STEM_HEIGHT + LOOP_RADIUS * Math.sin(k);
		} else {
			double k = (t - OFFSET2) / (1 - OFFSET2);
			x = LOOP_RADIUS + LOOP_RADIUS * Math.cos(k * Math.PI);
			y = STEM_HEIGHT * (1 - k);
		}

		return new Vec3d(0.34455 + x, -0.05 + y, 0.5);
	}

	public static double circle(double t) {
		if (t < -1) return 0;
		if (t > 1) return 0;
		return Math.sqrt(1 - t * t);
	}

	public static Vec3d apply(Vec3d a, Vec3d b, DoubleBinaryOperator f) {
		return new Vec3d(
				f.applyAsDouble(a.x, b.x),
				f.applyAsDouble(a.y, b.y),
				f.applyAsDouble(a.z, b.z));
	}

	@Override
	public void registerTextures() {
		Textures.register("klein_lighting");
	}


	@Override
	public BoxModel getWorldModel(@Nullable ItemStack stack, IBlockState state, float timer) {
		BoxModel model = new BoxModel();
		model.addBoxI(0, 0, 0, 16, 16, 16);
		return model;
	}

	@Override
	public BoxModel getRenderModel(@Nullable ItemStack stack, IBlockState state, float timer) {
		BoxModel model = new BoxModel();
		double time = (timer / 30) % 4;
		final double offset = 0.05;
		final int angle_n = 8;
		final int height_n = 10;
		double[] ca = new double[angle_n], sa = new double[angle_n];
		for (int i = 0; i < angle_n; i++) {
			double t = (i * Math.PI * 2) / angle_n;
			ca[i] = Math.cos(t);
			sa[i] = Math.sin(t);
		}

		final double r = 0.95;

		for (int z = -1; z <= 1; z += 2) {
			for (int i = 0; z == 1 ? i < height_n : i > -height_n; i += z) {
				double tA = time + offset * i;
				double tB = time + offset * (i + 1);
				Vec3d stemPosA = getStemPos(tA).subtract(0.5, 0.5, 0.5).scale(r).addVector(0.5, 0.5, 0.5);
				Vec3d stemPosB = getStemPos(tB).subtract(0.5, 0.5, 0.5).scale(r).addVector(0.5, 0.5, 0.5);
				double radA = getStemRadius(tA) * r;
				double radB = getStemRadius(tB) * r;
				Vec3d normA = getStemNormal(tA);
				Vec3d normB = getStemNormal(tB);

				for (int j = 0; j < angle_n; j++) {
					int j2 = (j + 1) % angle_n;

					float u0 = ((float) j) / angle_n;
					float u1 = ((float) (j + 1)) / angle_n;


					float v0 = (1 + ((float) i) / height_n) / 2;
					float v1 = (1 + ((float) (i + 1)) / height_n) / 2;

					model.add(new BoxSingleQuad(
							stemPosA.add(normA.scale(radA * ca[j])).addVector(0, 0, radA * sa[j]), u0, v0,
							stemPosB.add(normB.scale(radB * ca[j])).addVector(0, 0, radB * sa[j]), u0, v1,
							stemPosB.add(normB.scale(radB * ca[j2])).addVector(0, 0, radB * sa[j2]), u1, v1,
							stemPosA.add(normA.scale(radA * ca[j2])).addVector(0, 0, radA * sa[j2]), u1, v0
					).setDoubleSided(false).setTexture("klein_lighting"));
				}
			}
		}
		return model;
	}

//	public BakedQuad buildQuad(Vec3d pos0, float u0, float v0,
//							   Vec3d pos1, float u1, float v1,
//							   Vec3d pos2, float u2, float v2,
//							   Vec3d pos3, float u3, float v3) {
//		return QuadHelper.buildBoxQuad(DefaultVertexFormats.ITEM,
//				(float) pos0.x, (float) pos0.y, (float) pos0.z, u0, v0,
//				(float) pos1.x, (float) pos1.y, (float) pos1.z, u1, v1,
//				(float) pos2.x, (float) pos2.y, (float) pos2.z, u2, v2,
//				(float) pos3.x, (float) pos3.y, (float) pos3.z, u3, v3
//		);
//	}

	interface dubFunc {
		double apply(double a, double b);
	}
}
