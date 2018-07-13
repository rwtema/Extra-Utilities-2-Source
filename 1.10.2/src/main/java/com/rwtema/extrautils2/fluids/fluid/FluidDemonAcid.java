package com.rwtema.extrautils2.fluids.fluid;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.fluids.XUFluid;
import net.minecraft.util.ResourceLocation;

public class FluidDemonAcid extends XUFluid {
	public static ResourceLocation TEXTURE = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER + ":plasma");

	public FluidDemonAcid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
		super("Demon Acid", "demon_acid", TEXTURE, TEXTURE);
	}
}
