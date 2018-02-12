package com.rwtema.extrautils2.potion;

import javax.annotation.Nonnull;
import net.minecraft.entity.EntityLivingBase;

public class PotionNapalm extends XUPotion {
	public PotionNapalm() {
		super("Greek Fire", true, 0xff5000);
	}

	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		return p_76397_1_ % 20 == 0 ;
	}

	@Override
	public void performEffect(@Nonnull EntityLivingBase entity, int p_76394_2_) {
		if (!entity.isImmuneToFire()) {
			entity.setFire(15);
		}
	}
}
