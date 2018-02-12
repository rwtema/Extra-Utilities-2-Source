package com.rwtema.extrautils2.potion;

import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;

public class PotionDoom extends XUPotion {
	public static final DamageSource DOOM = new DamageSource("doom").setDamageBypassesArmor().setDamageAllowedInCreativeMode();

	static {
		Lang.translate("death.attack.doom", "%1$s met his doom");
		Lang.translate("death.attack.doom.item", "%1$s met his doom");
	}

	public PotionDoom() {
		super("Doom", true, 0x301010);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	public void performEffect(@Nonnull EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
		PotionEffect activePotionEffect = entityLivingBaseIn.getActivePotionEffect(this);
		if (activePotionEffect != null) {
			int duration = activePotionEffect.getDuration();
			if (entityLivingBaseIn.world.isRemote) {
				int t = duration / 20;
				if (t <= 0) return;

				if ((t <= 10 || (t % 10) == 0) && (duration % 20) == 0) {
					if (entityLivingBaseIn instanceof EntityPlayer) {
						SpecialChat.sendChat(((EntityPlayer) entityLivingBaseIn),
								Lang.chat("The Specter of Death will arrive in %s seconds.", t)
						);
					}
				}
			} else {
				if (duration <= 4) {
					entityLivingBaseIn.attackEntityFrom(DOOM, Float.MAX_VALUE);
				}
			}
		}
	}
}
