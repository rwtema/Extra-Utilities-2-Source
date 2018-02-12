package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;

public class VecHelper {
	public static Vec3d addSide(Vec3d vec, EnumFacing side, double mult) {
		return vec.addVector(
				side.getFrontOffsetX() * mult,
				side.getFrontOffsetY() * mult,
				side.getFrontOffsetZ() * mult
		);
	}

	public static Vec3d randUnitVec(Random rand) {
		float t = rand.nextFloat() * (float) Math.PI * 2;
		float s = rand.nextFloat() * (float) Math.PI * 2;
		float cs = MathHelper.cos(s);
		return new Vec3d(cs * MathHelper.cos(t), cs * MathHelper.sin(t), MathHelper.sin(s));
	}

	public static Vec3d lambdaCombine(Vec3d left, Vec3d right, DoubleBinaryOperator lambda){
		//noinspection SuspiciousNameCombination
		return new Vec3d(
				lambda.applyAsDouble(left.x, right.x),
				lambda.applyAsDouble(left.y, right.y),
				lambda.applyAsDouble(left.z, right.z)
		);
	}

	public static Vec3d lambdaCombine(Vec3d left, double scalar, DoubleBinaryOperator lambda){
		//noinspection SuspiciousNameCombination
		return new Vec3d(
				lambda.applyAsDouble(left.x, scalar),
				lambda.applyAsDouble(left.y, scalar),
				lambda.applyAsDouble(left.z, scalar)
		);
	}
}
