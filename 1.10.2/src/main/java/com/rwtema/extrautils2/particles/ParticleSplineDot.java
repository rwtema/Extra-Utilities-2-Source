package com.rwtema.extrautils2.particles;

import com.rwtema.extrautils2.utils.helpers.SplineHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleSplineDot extends ParticleDot {

	Vec3d startPos;
	Vec3d endPos;
	Vec3d startVel;
	Vec3d endVel;

	double[] xParam;
	double[] yParam;
	double[] zParam;

	public ParticleSplineDot(World world, Vec3d startPos, Vec3d endPos, Vec3d startVel, Vec3d endVel, float r, float g, float b, int age) {
		super(world, startPos.x, startPos.y, startPos.z, r, g, b);
		this.startPos = startPos;
		this.endPos = endPos;
		this.startVel = startVel;
		this.endVel = endVel;

		xParam = SplineHelper.splineParams(startPos.x, endPos.x, startVel.x, endVel.x);
		yParam = SplineHelper.splineParams(startPos.y, endPos.y, startVel.y, endVel.y);
		zParam = SplineHelper.splineParams(startPos.z, endPos.z, startVel.z, endVel.z);

		this.particleMaxAge = age;
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		this.resetPositionToBB();
	}

	@Override
	public void onUpdate() {
		if (this.particleAge++ >= this.particleMaxAge)
			this.setExpired();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		double h = ((double) particleAge) / particleMaxAge;

		posX = SplineHelper.evalSpline(h, xParam);
		posY = SplineHelper.evalSpline(h, yParam);
		posZ = SplineHelper.evalSpline(h, zParam);

		motionX = SplineHelper.evalSpline(h, xParam) - prevPosX;
		motionY = SplineHelper.evalSpline(h, yParam) - prevPosY;
		motionZ = SplineHelper.evalSpline(h, zParam) - prevPosZ;

		move(motionX, motionY, motionZ);
	}
}
