package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.item.ItemStack;

import java.util.List;

public enum Upgrade {
	STACK_SIZE(1, 5),
	SPEED(64, 1),
	MINING(1, 10) {
		@Override
		public float getPowerUse(int level) {
			return 0;
		}
	};
	//	static final float PENALTY = 4F / 15F;
	static final float PENALTY = 2 / 61F;
	public final int maxLevel;
	public final float power;

	Upgrade(int maxLevel, float power) {
		this.maxLevel = maxLevel;
		this.power = power;
	}

	public static void addTooltip(List<String> tooltip, ItemStack stack, IUpgradeProvider item, int stacklimitoverride) {
		Upgrade upgrade = item.getUpgrade(stack);
		if (upgrade != null) {
			int stackSize = StackHelper.getStacksize(stack);
			int maxLevel = stacklimitoverride == -1 ? upgrade.maxLevel : stacklimitoverride;
			tooltip.add(Lang.translateArgs("Max Upgrades: %s", maxLevel));
			if (upgrade.power > 0) {
				if (maxLevel == 1) {
					tooltip.add(Lang.translateArgs("Power Penalty: +%s GP", upgrade.getPowerUse(1)));
				} else {
					tooltip.add(Lang.translateArgs("Power Penalty (level 1): +%s GP", upgrade.getPowerUse(1)));
					if (stackSize > 1 && maxLevel > 1) {
						tooltip.add(Lang.translateArgs("Power Penalty (level %s): +%s GP", Math.min(stackSize, maxLevel), upgrade.getPowerUse(Math.min(stackSize, maxLevel))));
					}
				}
			}
		}
	}

	public int getModifierLevel(int level) {
		return level;
	}

	public float getPowerUse(int level) {
//		return level * power;
		if (level == 1) return 1;
		float v = Math.round(100 * level * (1 + level * PENALTY) / (1F + PENALTY)) / 100F;
		return v * power;
	}


}
