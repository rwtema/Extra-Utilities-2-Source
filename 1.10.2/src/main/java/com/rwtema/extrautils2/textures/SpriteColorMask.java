package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class SpriteColorMask extends SpriteLoaderProcessing {
	final boolean isMask;

	public SpriteColorMask(String tex, String mask, boolean isMask) {
		super(getSpriteName(tex, mask), new ResourceLocation(tex));
		this.isMask = isMask;
	}

	public static String registerSupplier(String tex, String mask, boolean isMask) {
		String spriteName = getSpriteName(tex, mask);
		Textures.registerSupplier(spriteName, () -> new SpriteColorMask(tex, mask, isMask));
		return spriteName;
	}

	public static String getSpriteName(String tex, String mask) {
		return ExtraUtils2.MODID + ":" + tex.replace(':', '_') + "_" + mask;
	}


	@Override
	protected void process(BufferedImage image, int i, int n, int w) {
		float min = 1, max = 0;
		for (int px = 0; px < w; px++) {
			for (int py = 0; py < w; py++) {
				int col = image.getRGB(px, py);
				float intensity = ColorHelper.brightness(col) / 255F;
				min = Math.min(min, intensity);
				max = Math.max(max, intensity);
			}
		}


		float k0 = min;
		float k1 = 1-min;

		for (int px = 0; px < w; px++) {
			for (int py = 0; py < w; py++) {
				int col = image.getRGB(px, py);
				float intensity = ColorHelper.brightness(col) / 255F;
				float v = (intensity - min) / (max - min);
				v = k0 + k1 * v;

				float alpha;
				if (isMask) {
					alpha = Math.min(v * (1 - v) * 2, 1);
					alpha = alpha * 0.5F + 0.5F;
				} else alpha = 1;


				image.setRGB(px, py, ColorHelper.colorClamp(v, v, v, alpha));
			}
		}
	}
}
