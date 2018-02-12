package com.rwtema.extrautils2.potion;

import com.rwtema.extrautils2.network.SpecialChat;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionSecondChance extends XUPotion {
	public PotionSecondChance() {
		super("Second Chance", false, 0x60ff70);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase entityLiving = event.getEntityLiving();
		if (entityLiving.world.isRemote) return;
		if (event.getSource().canHarmInCreative()) {
			return;
		}
		PotionEffect curEffect = entityLiving.getActivePotionEffect(PotionSecondChance.this);
		if (curEffect == null) return;

		NBTTagCompound entityData = entityLiving.getEntityData();
		if (entityData.getBoolean("AlreadyHadOneChance")) {
			return;
		}

		entityLiving.removePotionEffect(PotionSecondChance.this);

		if (curEffect.getAmplifier() > 0) {
			entityLiving.addPotionEffect(new PotionEffect(curEffect.getPotion(),
					curEffect.getDuration(),
					curEffect.getAmplifier() - 1,
					curEffect.getIsAmbient(),
					curEffect.doesShowParticles()
			));
		}

		entityLiving.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 20 * 10));

		entityLiving.setHealth(0.01F);
		entityLiving.heal(entityLiving.getMaxHealth());
		if (entityLiving instanceof EntityPlayer) {
			SpecialChat.sendChat(((EntityPlayer) entityLiving), Lang.chat("Second Chance!"));
		}
		entityData.setBoolean("AlreadyHadOneChance", true);
		event.setCanceled(true);
	}

}
