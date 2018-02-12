package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.world.WorldProvider;

public abstract class WorldProviderCompat extends WorldProvider {

	public String getWelcomeMessage() {
		return null;
	}
}
