package com.rwtema.extrautils2.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import java.util.*;

public class PotionRelapse extends XUPotion {
	WeakHashMap<EntityLivingBase, Map<Potion, PotionEffect>> effects = new WeakHashMap<>();

	public PotionRelapse() {
		super("Relapse", false, 0x6050ff);
	}

	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		return true;
	}

	@Override
	public void performEffect(@Nonnull final EntityLivingBase entity, int p_76394_2_) {
		if (entity.world.isRemote) return;

		WorldServer worldServer = (WorldServer) entity.world;

		Collection<PotionEffect> current = entity.getActivePotionEffects();

		Map<Potion, PotionEffect> oldEffects;
		Map<Potion, PotionEffect> curEffects;
		if (current.isEmpty()) {
			curEffects = Collections.emptyMap();
			oldEffects = effects.remove(entity);
		} else {
			curEffects = new HashMap<>();
			for (PotionEffect potionEffect : current) {
				Potion potion = potionEffect.getPotion();
				if (potion != this) {
					if (potion.isBadEffect()) {
						curEffects.put(potion, new PotionEffect(potionEffect));
					}
				} else {
					potionEffect.setCurativeItems(Collections.emptyList());
				}
			}
			if (curEffects.isEmpty()) {
				oldEffects = effects.remove(entity);
			} else
				oldEffects = effects.put(entity, curEffects);
		}

		if (oldEffects == null) return;

		Random rand = worldServer.rand;

		for (Potion potion : oldEffects.keySet()) {
			if (!curEffects.containsKey(potion)) {
				PotionEffect effect = oldEffects.get(potion);
				int amplifier = effect.getAmplifier();

				if (amplifier == 0) {
					if (rand.nextInt(6) == 0)
						continue;
				} else if (rand.nextInt(2) == 0)
					amplifier--;

				int duration = effect.getDuration() >> 1;
				if (duration <= 0) continue;
				duration = duration + rand.nextInt(duration);


				final PotionEffect potionEffect = new PotionEffect(
						effect.getPotion(),
						duration,
						amplifier,
						effect.getIsAmbient(),
						effect.doesShowParticles()
				);
				worldServer.addScheduledTask(() -> entity.addPotionEffect(potionEffect));
			}
		}
	}

}
