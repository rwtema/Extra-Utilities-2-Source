package com.rwtema.extrautils2.power;

import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public interface IPowerSubType extends IPower {
	Collection<ResourceLocation> getTypes();

}
