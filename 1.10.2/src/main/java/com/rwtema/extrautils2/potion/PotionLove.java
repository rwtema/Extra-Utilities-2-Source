package com.rwtema.extrautils2.potion;

import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class PotionLove extends XUPotion {
	public PotionLove() {
		super("Love", false, 0xffc0db);
	}

	@Override
	public boolean isInstant() {
		return true;
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, @Nonnull EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		if (entityLivingBaseIn instanceof EntityAnimal) {
			fallInLove(indirectSource, (EntityAnimal) entityLivingBaseIn);
		}
	}

	private void fallInLove(Entity indirectSource, EntityAnimal animal) {
		EntityPlayer player = indirectSource instanceof EntityPlayer ? (EntityPlayer) indirectSource : null;

		if (animal.getGrowingAge() == 0 && !animal.isInLove()) {
			animal.setInLove(player);
		}
	}

	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		return true;
	}

	@Override
	public void performEffect(@Nonnull EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
		World worldObj = entityLivingBaseIn.world;
		if (!worldObj.isRemote) {
			if (entityLivingBaseIn instanceof EntityAnimal) {
				fallInLove(null, (EntityAnimal) entityLivingBaseIn);
			}
		} else {
			Random rand = worldObj.rand;
			double d0 = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle(EnumParticleTypes.HEART,
					entityLivingBaseIn.posX + rand.nextFloat() * entityLivingBaseIn.width * 2.0F - entityLivingBaseIn.width,
					entityLivingBaseIn.posY + 0.5D + rand.nextFloat() * entityLivingBaseIn.height,
					entityLivingBaseIn.posZ + rand.nextFloat() * entityLivingBaseIn.width * 2.0F - entityLivingBaseIn.width,
					d0, d1, d2);
		}
	}
}
