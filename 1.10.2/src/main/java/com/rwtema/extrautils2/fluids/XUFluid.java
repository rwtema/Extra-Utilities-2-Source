package com.rwtema.extrautils2.fluids;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

public class XUFluid extends Fluid {
	public static final ResourceLocation BLANK_WATER_STILL = new ResourceLocation(ExtraUtils2.MODID, "blank_water_still");
	public static final ResourceLocation BLANK_LAVA_STILL = new ResourceLocation(ExtraUtils2.MODID, "blank_lava_still");
	public static final ResourceLocation BLANK_WATER_FLOW = new ResourceLocation(ExtraUtils2.MODID, "blank_water_flow");
	public static final ResourceLocation BLANK_LAVA_FLOW = new ResourceLocation(ExtraUtils2.MODID, "blank_lava_flow");

	private int xuColor;


	public XUFluid(String locName, String fluidName, ResourceLocation still, ResourceLocation flowing, Color color) {
		this(locName, fluidName, still, flowing);
		xuColor = color.getRGB();
	}


	public XUFluid(String locName, String fluidName, ResourceLocation still, ResourceLocation flowing, int color) {
		this(locName, fluidName, still, flowing);
		xuColor = color;
	}

	public XUFluid(String locName, String fluidName, ResourceLocation still, ResourceLocation flowing) {
		super(fluidName, still, flowing);
		Lang.translate("fluid." + fluidName, locName);
		ExtraUtils2.proxy.registerTexture(still.toString(), flowing.toString());
	}

	public Fluid setColor(Color color) {
		xuColor = color.getRGB();
		return this;
	}

	public Fluid setColor(int color) {
		xuColor = color;
		return this;
	}

	@Override
	public int getColor() {
		return xuColor;
	}
}
