package com.rwtema.extrautils2.fluids;

import com.rwtema.extrautils2.textures.SpriteLoaderProcessing;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.helpers.CIELabHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.IOException;

public class TexturePlasma extends SpriteLoaderProcessing {
	public static final DirectColorModel directColorModel = new DirectColorModel(32, 16711680, '\uff00', 255, -16777216);
	final int[] colorPalette;

	public TexturePlasma(String spriteName, ResourceLocation baseTexture, int... colorPalette) {
		super(spriteName, baseTexture);
		this.colorPalette = colorPalette;
	}

	public static int interpolate(float intensity, int[] colorPalette) {
		int n = colorPalette.length;
		if (n == 0) throw new IllegalArgumentException("Palette is empty");
		if (n == 1) return colorPalette[0];

		if (intensity <= 0) return colorPalette[0];
		if (intensity >= 1) return colorPalette[n - 1];

		float dv = intensity * (n - 1);
		int t = (int) Math.floor(dv);
		dv -= t;

		if (dv == 0) {
			return colorPalette[t];
		}
		float a1 = ColorHelper.getAF(colorPalette[t]);
		float a2 = ColorHelper.getAF(colorPalette[t + 1]);

		if (a1 <= .005) {
			return ColorHelper.alpha(colorPalette[t + 1], dv);
		}
		if (a2 <= .005) {
			return ColorHelper.alpha(colorPalette[t], 1 - dv);
		}

		float[] lab1 = CIELabHelper.rgb2lab(colorPalette[t], new float[3]);
		float[] lab2 = CIELabHelper.rgb2lab(colorPalette[t + 1], new float[3]);

		float[] labf = new float[]{
				lab1[0] + dv * (lab2[0] - lab1[0]),
				lab1[1] + dv * (lab2[1] - lab1[1]),
				lab1[2] + dv * (lab2[2] - lab1[2])
		};
		float[] rgb = CIELabHelper.lab2rgb(labf, new float[3]);

		return ColorHelper.colorClamp(rgb[0], rgb[1], rgb[2], a1 + (a2 - a1) * dv);
	}

	@Override
	public void loadSpriteFrames(IResource resource, int p_188539_2_) throws IOException {
		super.loadSpriteFrames(resource, p_188539_2_);
		int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
		frameCounter = XURandom.rand.nextInt(j);
	}

	@Override
	protected void process(BufferedImage image, int i, int n, int w) {
		for (int px = 0; px < w; px++) {
			for (int py = 0; py < w; py++) {

				int col = image.getRGB(px, py);
				int a = ColorHelper.getA(col);
				int intensity = (ColorHelper.getR(col) + ColorHelper.getG(col) + ColorHelper.getB(col)) / 3;

				image.setRGB(px, py, interpolate(intensity / 255F, colorPalette));
			}
		}
	}


}
