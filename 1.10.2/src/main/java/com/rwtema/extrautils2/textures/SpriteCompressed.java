package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.utils.LogHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import java.awt.image.BufferedImage;

public class SpriteCompressed extends SpriteLoader {
	private final int n;
	private final float max_n;
	private final ResourceLocation textureLocation;

	public SpriteCompressed(String textureBase, int n, float max_n) {
		super(ExtraUtils2.RESOURCE_FOLDER + ":compressed/" + textureBase + "_" + n);
		this.n = n;
		this.max_n = max_n;
		this.textureLocation = new ResourceLocation("minecraft", "textures/blocks/" + textureBase + ".png");
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location) {
		try (IResource iresource = manager.getResource(textureLocation)) {
			int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
			BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];

			AnimationMetadataSection animationmetadatasection = iresource.getMetadata("animation");

			BufferedImage image = ImageIO.read(iresource.getInputStream());

			int w = image.getWidth();
			int h = image.getHeight();
			BufferedImage newImage = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, h);

			float border = (2 + n / max_n / 2) / 32F;

			for (int px = 0; px < w; px++) {
				for (int py = 0; py < h; py++) {
					float x = ((float) px / (w - 1));
					float y = ((float) (py % w)) / (w - 1);
					int col = image.getRGB(px, py);
					float d = MathHelper.sqrt((x - 0.5F) * (x - 0.5F) + (y - 0.5F) * (y - 0.5F));

					float br = 1 - n / max_n + (0.5F - d) / 2F;
					if (br > 1) br = 1;

					if (x <= border || y < border || (1 - x) <= border || (1 - y) <= border) {
						br *= 0.5F;
					}

					int a = ColorHelper.getA(col);
					int r = MathHelper.clamp(Math.round(ColorHelper.getR(col) * br), 0, 255);
					int g = MathHelper.clamp(Math.round(ColorHelper.getG(col) * br), 0, 255);
					int b = MathHelper.clamp(Math.round(ColorHelper.getB(col) * br), 0, 255);

					newImage.setRGB(px, py, ColorHelper.color(r, g, b, a));
				}
			}


			abufferedimage[0] = newImage;

			this.loadSprite(abufferedimage, animationmetadatasection);
			generateMipmaps(mipmapLevels);
		} catch (Throwable ioexception1) {
			LogHelper.logger.error("Using missing texture, unable to load " + textureLocation, ioexception1);
			return true;
		}

		return false;
	}


}
