package com.rwtema.extrautils2.potion;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PotionPurge extends XUPotion {
	public PotionPurge() {
		super("Purging", true, 0xff6a00);
	}

	@Override
	public void performEffect(@Nonnull EntityLivingBase entityLivingBaseIn, int amplifier) {

	}

	@Override
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, @Nonnull EntityLivingBase entityLivingBaseIn, int amplifier, double health) {
		entityLivingBaseIn.clearActivePotions();
		if (entityLivingBaseIn instanceof EntityPlayer) {
			FoodStats foodStats = ((EntityPlayer) entityLivingBaseIn).getFoodStats();
			NBTTagCompound compound = new NBTTagCompound();
			foodStats.writeNBT(compound);
			if (entityLivingBaseIn == source) {
				compound.setInteger("foodLevel", 0);
				compound.setFloat("foodSaturationLevel", 0);
			} else {
				compound.setInteger("foodLevel", Math.max(0, (compound.getInteger("foodLevel") + 1) / 2));
				compound.setFloat("foodSaturationLevel", 0);
			}
			foodStats.readNBT(compound);
		}
	}

	@Override
	public boolean isInstant() {
		return true;
	}
}
