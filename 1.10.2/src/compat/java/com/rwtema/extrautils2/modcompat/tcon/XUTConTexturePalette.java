package com.rwtema.extrautils2.modcompat.tcon;

import com.rwtema.extrautils2.fluids.TexturePlasma;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import slimeknights.tconstruct.library.client.texture.AbstractColoredTexture;

class XUTConTexturePalette extends AbstractColoredTexture {

	int min, max;
	private int[] colorPalette;

	public XUTConTexturePalette(TextureAtlasSprite baseTexture, String location, int[] palette) {
		super(baseTexture, location);
		colorPalette = palette;
		min = 255;
		max = 0;
	}

	@Override
	protected void processData(int[][] data) {
		min = 255;
		max = 0;
		for (int c : data[0]) {
			if (TexturePlasma.directColorModel.getAlpha(c) >= 32) {
				int brightness = getBrightness(c);

				min = Math.min(min, brightness);
				max = Math.max(max, brightness);
			}
		}
		super.processData(data);
	}

	@Override
	protected int colorPixel(int pixel, int mipmap, int pxCoord) {
		if (pixel == 0 || TexturePlasma.directColorModel.getAlpha(pixel) < 32) {
			return 0;
		}

		int brightness = getBrightness(pixel);

		float intensity = (brightness - min) / (float) (max - min);

		return TexturePlasma.interpolate(intensity, colorPalette);
	}

	public static int getBrightness(int pixel) {
		return ColorHelper.brightness(
				TexturePlasma.directColorModel.getRed(pixel),
				TexturePlasma.directColorModel.getGreen(pixel),
				TexturePlasma.directColorModel.getBlue(pixel)
		);
	}
}
