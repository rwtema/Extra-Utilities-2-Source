package com.rwtema.extrautils2.machine;

import com.rwtema.extrautils2.api.machine.XUMachineGenerators;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.Collection;

class FoodEnergyRecipe extends EnergyBaseRecipe {
	public FoodEnergyRecipe() {
		super(XUMachineGenerators.INPUT_ITEM);
	}

	public static int nerfLevels(double energy, double maxLevel) {
		if (energy < maxLevel) return (int) Math.ceil(energy);
		double f = 1;
		double totalEnergy = 0;
		while (energy > maxLevel) {
			totalEnergy += maxLevel / f;
			energy -= maxLevel;
			f += 1;

		}
		totalEnergy += energy / f;
		return (int) Math.ceil(totalEnergy);
	}

	@Override
	public int getEnergyOutput(@Nonnull ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (!(item instanceof ItemFood))
			return 0;

		ItemFood food = (ItemFood) item;
		if (!isValid(food)) return 0;
		int healAmount = getHealAmount(itemStack, food);
		return nerfLevels(healAmount * getSaturationModifier(itemStack, food) * 8000, 64000);
	}

	private float getSaturationModifier(@Nonnull ItemStack itemStack, ItemFood food) {
		float saturationModifier = food.getSaturationModifier(itemStack);
		PotionEffect potionId = food.potionId;
		if (potionId != null) {
			Potion potion = potionId.getPotion();
			if (potion == MobEffects.SATURATION) {
				saturationModifier = Math.max(1F, saturationModifier);
			}
		}
		return saturationModifier;
	}

	private int getHealAmount(@Nonnull ItemStack itemStack, ItemFood food) {
		float healAmount = food.getHealAmount(itemStack);
		PotionEffect potionId = food.potionId;
		if (potionId != null) {
			Potion potion = potionId.getPotion();
			if (potion == MobEffects.INSTANT_HEALTH) {
				healAmount += Math.min(4 << potionId.getAmplifier(), 20);
			} else if (potion == MobEffects.SATURATION) {
				healAmount += (potionId.getAmplifier() + 1) / 2F;
			} else if (potion == MobEffects.REGENERATION) {
				healAmount += potionId.getDuration() / (float)(50 >> potionId.getAmplifier()) / 4F;
			} else if (potion == MobEffects.ABSORPTION) {
				healAmount += (potionId.getAmplifier() + 1) / 2F;
			} else {
				healAmount += 1;
			}
		}
		if (food == Items.GOLDEN_APPLE) {
			if (itemStack.getMetadata() > 0) {
				healAmount += 8 + 4;
			} else {
				healAmount += 2 + 1;
			}
		}
		return (int) (healAmount + 0.5F);
	}

	@Override
	protected float getEnergyRate(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof ItemFood))
			return 0;

		ItemFood food = (ItemFood) item;
		int healAmount = getHealAmount(stack, food);

		return Math.max(nerfLevels(healAmount * 8, 64), 1);
	}

	public boolean isValid(ItemFood food) {
		return food.potionId == null || !food.potionId.getPotion().isBadEffect();
	}

	@Nonnull
	@Override
	public Collection<ItemStack> getInputValues() {
		return EnergyBaseRecipe.getCreativeStacks(null, item -> item instanceof ItemFood && isValid((ItemFood) item));
	}
}
