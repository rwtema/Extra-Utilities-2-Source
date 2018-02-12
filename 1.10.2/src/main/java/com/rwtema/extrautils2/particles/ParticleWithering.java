package com.rwtema.extrautils2.particles;

import net.minecraft.world.World;

public class ParticleWithering extends ParticleDot {
	public ParticleWithering(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn, 0, 0, 0);
		this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
		float brightness = rand.nextFloat() * 0.2F;
//		motionX *= 0.5;
		particleRed = brightness;
		particleGreen = brightness;
		particleBlue = brightness;
	}
}
