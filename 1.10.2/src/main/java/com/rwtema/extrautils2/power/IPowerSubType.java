package com.rwtema.extrautils2.power;

import java.util.Collection;
import net.minecraft.util.ResourceLocation;

public interface IPowerSubType extends IPower {
	Collection<ResourceLocation> getTypes();

}
