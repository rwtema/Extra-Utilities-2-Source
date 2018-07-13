package com.rwtema.extrautils2.textures;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteSub extends SpriteLoader {

	String name;
	int x;
	int y;
	int size;
	int image_width;
	int image_height;

	public SpriteSub(String spriteName, int x, int y, int image_width, int image_height, int size) {
		super(spriteName);
		this.x = x;
		this.y = y;
		this.size = size;
		this.image_width = image_width;
		this.image_height = image_height;
		name = spriteName + "_" + x + "_" + y + "_" + image_width + "_" + image_height + "_" + size;
	}

	@Nonnull
	public String getIconName() {
		return name;
	}

	@Override
	public boolean load(IResourceManager par1ResourceManager, ResourceLocation location) {
		ResourceLocation resourcelocation = new ResourceLocation(ExtraUtils2.MODID + ":" + super.getIconName());
		ResourceLocation resourcelocation1 = Textures.completeTextureResourceLocation(resourcelocation);

		try (IResource iresource = par1ResourceManager.getResource(resourcelocation1)) {
			int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
			BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];
			abufferedimage[0] = ImageIO.read(iresource.getInputStream());
			loadSprite(abufferedimage, null);
			generateMipmaps(mipmapLevels);
		} catch (IOException ioexception1) {
			LogHelper.logger.error("Using missing texture, unable to load " + resourcelocation1, ioexception1);
			return true;
		}

		return false;
	}


	@Override
	public void loadSprite(BufferedImage[] p_147964_1_, AnimationMetadataSection p_147964_2_) {
		this.setFramesTextureData(Lists.newArrayList());
		int w = p_147964_1_[0].getWidth();
		int h = p_147964_1_[0].getHeight();

		if ((w % image_width) != 0)
			throw new RuntimeException("Wrong width (must be divisible by " + image_width + ")");

		if ((h % image_height) != 0)
			throw new RuntimeException("Wrong height (must be divisible by " + image_height + ")");

		int scale = w / image_width;
		if ((image_height * scale) != h)
			throw new RuntimeException("Wrong aspect ratio (must be " + image_width + "x" + image_height + ")");

		int s = size * scale;

		this.width = s;
		this.height = s;

		int[][] aint = new int[p_147964_1_.length][];

		BufferedImage bufferedimage = p_147964_1_[0];

		aint[0] = new int[s * s];

		bufferedimage.getRGB(x * scale, y * scale, s, s, aint[0], 0, w);

		this.framesTextureData.clear();
		this.framesTextureData.add(aint);
	}
}
