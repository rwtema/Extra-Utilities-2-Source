package com.rwtema.extrautils2.textures;

import com.google.common.base.Throwables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.Textures;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TextureLocation implements ISolidWorldTexture {
	protected final String[] textures;
	protected final String[] baseTexture = new String[6];

	public TextureLocation(String texture) {

		try {
			ResourceLocation resourcelocation = new ResourceLocation(ExtraUtils2.MODID + ":connected/" + texture);
			ResourceLocation resourcelocation1 = Textures.completeTextureResourceLocation(resourcelocation);
			IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(resourcelocation1);
			BufferedImage read = ImageIO.read(iresource.getInputStream());
			int w = read.getWidth();
			int h = read.getHeight();
			if ((h % w) != 0) throw new RuntimeException("Height must be a multiple of the width.");
			int n = h / w;

			this.textures = new String[n];
			for (int i = 0; i < n; i++) {
				String key = texture + "#" + i;
				this.textures[i] = key;
				Textures.textureNames.put(key, new SpriteSub("connected/" + texture, 0, i, 1, n, 1));
			}

			assignBaseTextures();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	protected abstract void assignBaseTextures();

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getWorldIcon(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		int a = getRandomIndex(world, blockPos, side);
		int l = a % textures.length;

		return Textures.getSprite(textures[l]);
	}

	protected abstract int getRandomIndex(IBlockAccess world, BlockPos blockPos, EnumFacing side);

	@Override
	public String getItemTexture(EnumFacing side) {
		return baseTexture[side.ordinal()];
	}
}
