package com.rwtema.extrautils2.potion;

import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nonnull;

public class PotionFizzyLifting extends XUPotion {
	public PotionFizzyLifting() {
		super("Fizzy Lifting", true, 0xa0f060);
	}

	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		return true;
	}

	@Override
	public void performEffect(@Nonnull EntityLivingBase entity, int p_76394_2_) {
		entity.isAirBorne = true;
		entity.onGround = false;
		entity.fallDistance = 0;
		if (entity.motionY < 0) {
			entity.motionY *= 0.5;
		}
		if (entity.isSneaking()) {
			entity.motionY += 0.03F;
		} else {
			entity.motionY += 0.06F;
		}
	}
}
