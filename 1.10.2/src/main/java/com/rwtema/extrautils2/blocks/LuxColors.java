package com.rwtema.extrautils2.blocks;

public enum LuxColors {
	BLUE(0x0015FF),
	PINK(0xEA00FF),
	RED(0x93100E),
	YELLOW(0xFFEB18),
	GREEN(0x2CFF18),
	CYAN(0x18FFEB),
	WHITE(0xDFDFDF),
	BLACK(0x515151);

	public final int color;

	LuxColors(int color) {
		this.color = color;
	}
}
