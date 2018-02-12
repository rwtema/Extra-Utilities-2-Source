package com.rwtema.extrautils2.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class SpriteLoaderProcessing extends SpriteLoader {
	protected final ResourceLocation baseTexture;

	public SpriteLoaderProcessing(String spriteName, ResourceLocation baseTexture) {
		super(spriteName);
		this.baseTexture = baseTexture;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location) {
		try {
			ResourceLocation baseTexture = this.baseTexture;
			baseTexture = new ResourceLocation(baseTexture.getResourceDomain(), String.format("%s/%s%s", "textures", baseTexture.getResourcePath(), ".png"));
			IResource iresource = manager.getResource(baseTexture);
			int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
			BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];

			AnimationMetadataSection animationmetadatasection = iresource.getMetadata("animation");

			BufferedImage image = ImageIO.read(iresource.getInputStream());


			int w = image.getWidth();
			int h = image.getHeight();
			BufferedImage newImage = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, h);

			int n = (h / w);

			if ((n * w) != h) return true;

			int[] tempArr = new int[w * w];

			for (int i = 0; i < n; i++) {
				BufferedImage temp = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, w);
				image.getRGB(0, i * w, w, w, tempArr, 0, w);
				temp.setRGB(0, 0, w, w, tempArr, 0, w);

				process(temp, i, n, w);

				temp.getRGB(0, 0, w, w, tempArr, 0, w);
				newImage.setRGB(0, i * w, w, w, tempArr, 0, w);
			}

			abufferedimage[0] = newImage;

			this.loadSprite(abufferedimage, animationmetadatasection);
			generateMipmaps(mipmapLevels);
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}

		return false;
	}

	protected abstract void process(BufferedImage image, int i, int n, int w);
}
