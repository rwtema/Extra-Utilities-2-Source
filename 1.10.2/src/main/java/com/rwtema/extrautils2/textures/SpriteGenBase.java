package com.rwtema.extrautils2.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageTypeSpecifier;
import java.awt.image.BufferedImage;

public class SpriteGenBase extends SpriteLoader {

	private final int frameNo;

	public SpriteGenBase(String spriteName, int frameNo) {
		super(spriteName);
		this.frameNo = frameNo;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location) {
		int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
		BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];

//		AnimationMetadataSection animationmetadatasection = iresource.getMetadata("animation");

		int w = 16;
		int h = 16;
		int total_h = h * frameNo;
		BufferedImage newImage = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, total_h);


		for (int i = 0; i < frameNo; i++) {
			BufferedImage frame = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB).createBufferedImage(w, total_h);

		}


		abufferedimage[0] = newImage;

//		this.loadSprite(abufferedimage, animationmetadatasection);
		generateMipmaps(mipmapLevels);

		return false;
	}
}
