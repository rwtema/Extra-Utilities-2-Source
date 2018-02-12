package com.rwtema.extrautils2.fluids.fluid;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidDemonAcid extends Fluid {
	public static ResourceLocation TEXTURE = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER+ ":plasma");
	public FluidDemonAcid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
		super("demon_acid", TEXTURE, TEXTURE);
		ExtraUtils2.proxy.registerTexture(TEXTURE.toString());
	}
}
