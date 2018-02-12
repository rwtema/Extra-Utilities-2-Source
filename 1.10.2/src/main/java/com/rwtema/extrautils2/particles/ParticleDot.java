package com.rwtema.extrautils2.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleDot extends Particle {
	public ParticleDot(World worldIn, double posXIn, double posYIn, double posZIn, float r, float g, float b) {
		super(worldIn, posXIn, posYIn, posZIn, 0, 0, 0);
		motionX *= 0.05F;
		motionY *= 0.05F;
		motionZ *= 0.05F;
		particleRed = r;
		particleGreen = g;
		particleBlue = b;
		particleScale = 2;
		this.setParticleTextureIndex(0);

	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}
}
