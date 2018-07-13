package com.rwtema.extrautils2.particles;

import com.rwtema.extrautils2.compatibility.EntityCompat;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class ParticleBlood extends Particle {
	protected ParticleBlood(World worldIn, double posXIn, double posYIn, double posZIn, double motion, int color) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.setSize(0.01F, 0.01F);
		this.particleGravity = 0.5F;
		motionX = rand.nextGaussian() * motion;
		motionY = rand.nextGaussian() * motion;
		motionZ = rand.nextGaussian() * motion;
		this.particleRed = ColorHelper.getRF(color) * (0.9F + 0.1F * rand.nextFloat());
		this.particleGreen = ColorHelper.getGF(color) * (0.9F + 0.1F * rand.nextFloat());
		this.particleBlue = ColorHelper.getBF(color) * (0.9F + 0.1F * rand.nextFloat());
		particleAlpha = ColorHelper.getAF(color);
		particleScale = 2;
		this.particleMaxAge = (int) (8.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
		canCollide = true;
		this.setParticleTextureIndex(0);
	}

	public ParticleBlood(EntityLivingBase entity) {
		this(entity.world,
				MathHelper.nextDouble(entity.world.rand, entity.getRenderBoundingBox().minX, entity.getRenderBoundingBox().maxX),
				MathHelper.nextDouble(entity.world.rand, entity.getRenderBoundingBox().minY, entity.getRenderBoundingBox().maxY),
				MathHelper.nextDouble(entity.world.rand, entity.getRenderBoundingBox().minZ, entity.getRenderBoundingBox().maxZ),
				0.2, getColor(entity, entity.world.rand));
	}

	private static int getColor(EntityLivingBase entity, Random rand) {
		int i = rand.nextInt(3);
		if (entity instanceof EntityMob || i != 0) {
			EntityList.EntityEggInfo entityEggInfo = EntityList.ENTITY_EGGS.get(EntityCompat.getKey(entity));
			if (entityEggInfo != null) {
				if (i == 1) {
					return (entityEggInfo.primaryColor | 0xff000000);
				} else {
					return (entityEggInfo.secondaryColor | 0xff000000);
				}
			}
		}
		return 0xffff0000;
	}

}
