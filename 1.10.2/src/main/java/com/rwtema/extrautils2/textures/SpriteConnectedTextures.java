package com.rwtema.extrautils2.textures;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.LogHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

public class SpriteConnectedTextures extends SpriteLoader {
	public final int texID;
	public static final String basePath = "textures";

	protected SpriteConnectedTextures(String spriteName, int texID) {
		super(spriteName);
		this.texID = texID;
	}

	public static String iconName(String name, int offset) {
		return name + "_" + offset;
	}

	@Nonnull
	public String getIconName() {
		return iconName(super.getIconName(), texID);
	}

	@Override
	protected void loadSprite(BufferedImage[] abufferedimage, AnimationMetadataSection animationmetadatasection) {
		this.setFramesTextureData(Lists.newArrayList());
		int size = abufferedimage[0].getWidth();
		int h = abufferedimage[0].getHeight();
		this.width = size;
		//noinspection SuspiciousNameCombination
		this.height = size;


		if (size % 2 != 0)
			throw new RuntimeException("Wrong width (must be divisible by 2)");

		if (h != (size * 5))
			throw new RuntimeException("Wrong aspect ratio (must be 1x5)");

		int[][] aint = new int[abufferedimage.length][];


		BufferedImage bufferedimage = abufferedimage[0];

		aint[0] = new int[size * size];

		int textureIndexMask = ConnectedTexturesHelper.textureIds[texID];

		int half_size = size / 2;

		int subImageIndex;

		subImageIndex = textureIndexMask / 125;
		bufferedimage.getRGB(0, subImageIndex * size, half_size, half_size, aint[0], 0, size);
		textureIndexMask -= subImageIndex * 125;

		subImageIndex = textureIndexMask / 25;
		bufferedimage.getRGB(0, subImageIndex * size + half_size, half_size, half_size, aint[0], half_size * size, size);
		textureIndexMask -= subImageIndex * 25;

		subImageIndex = textureIndexMask / 5;
		bufferedimage.getRGB(half_size, subImageIndex * size + half_size, half_size, half_size, aint[0], half_size * (size + 1), size);
		textureIndexMask -= subImageIndex * 5;

		subImageIndex = textureIndexMask;
		bufferedimage.getRGB(half_size, subImageIndex * size, half_size, half_size, aint[0], half_size, size);

		this.framesTextureData.clear();
		this.framesTextureData.add(aint);
	}

	@Override
	public boolean load(IResourceManager par1ResourceManager, ResourceLocation location) {
		ResourceLocation resourcelocation = new ResourceLocation(ExtraUtils2.MODID + ":connected/" + super.getIconName());
		ResourceLocation resourcelocation1 = Textures.completeTextureResourceLocation(resourcelocation);

		try {
			IResource iresource = par1ResourceManager.getResource(resourcelocation1);
			int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
			BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];
			abufferedimage[0] = ImageIO.read(iresource.getInputStream());
			this.loadSprite(abufferedimage, null);
			generateMipmaps(mipmapLevels);
		} catch (IOException ioexception1) {
			LogHelper.logger.error("Using missing texture, unable to load " + resourcelocation1, ioexception1);
			return true;
		}

		return false;

	}
}
