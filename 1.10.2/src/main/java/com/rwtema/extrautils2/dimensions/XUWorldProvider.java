package com.rwtema.extrautils2.dimensions;

import com.rwtema.extrautils2.compatibility.WorldProviderCompat;
import net.minecraft.world.DimensionType;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public abstract class XUWorldProvider extends WorldProviderCompat {
	final DimensionEntry entry;

	public XUWorldProvider(DimensionEntry entry) {
		this.entry = entry;
	}

	@Override
	@Nonnull
	public DimensionType getDimensionType() {
		return Validate.notNull(entry.value);
	}
}
