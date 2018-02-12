package com.rwtema.extrautils2.textures;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockConnectedTextureBase;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectedTexture implements ISolidWorldTexture {
	public static EnumFacing[] up = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP};
	public static EnumFacing[] left = new EnumFacing[]{EnumFacing.WEST, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};
	public final String baseTextureName;
	final XUBlock base;
	final IBlockState state;
	public String texture;
	public SpriteConnectedTextures[] icons = new SpriteConnectedTextures[47];
	boolean hasConnectedTextures = false;

	public ConnectedTexture(String texture, IBlockState state, XUBlockConnectedTextureBase base) {
		this.texture = texture;
		this.state = state;
		this.base = base;

		try {
			ResourceLocation resourcelocation = new ResourceLocation(ExtraUtils2.MODID + ":connected/" + texture);
			ResourceLocation resourcelocation1 = Textures.completeTextureResourceLocation(resourcelocation);
			IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(resourcelocation1);
			BufferedImage read = ImageIO.read(iresource.getInputStream());
			hasConnectedTextures = read.getHeight() == (read.getWidth() * 5);
		} catch (IOException e) {
			hasConnectedTextures = false;

		}

		baseTextureName = "connected/" + texture;
		if (hasConnectedTextures) {
			for (int i : ConnectedTexturesHelper.trueTextures) {
				Textures.textureNames.put(getTexKey(i), icons[i] = new SpriteConnectedTextures(texture, i));
			}
		} else {
			Textures.register(baseTextureName);
		}
	}

	private static BlockPos multiOffset(BlockPos base, EnumFacing... sides) {
		for (EnumFacing side : sides) {
			if (side != null)
				base = base.offset(side);
		}
		return base;
	}

	public String getTexKey(int i) {
		if (hasConnectedTextures)
			return baseTextureName + "#" + i;
		else
			return baseTextureName;
	}

	protected boolean matches(IBlockAccess world, BlockPos pos, EnumFacing side, BlockPos originalPos) {
		IBlockState b = world.getBlockState(pos);
		return b == state && b.shouldSideBeRendered(world, pos, side);
	}

	@Override
	public List<TextureComponent> getComposites(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		if (side == null)
			return null;

		if (!hasConnectedTextures)
			return ISolidWorldTexture.handleTexture(getBaseTexture());

		EnumFacing u = up[side.getIndex()];
		EnumFacing l = left[side.getIndex()];
		EnumFacing r = l.getOpposite();
		EnumFacing d = u.getOpposite();

		int ar = 0;
		ar += matches(world, blockPos, side, u);
		ar += matches(world, blockPos, side, r) * 2;
		ar += matches(world, blockPos, side, d) * 4;
		ar += matches(world, blockPos, side, l) * 8;

		if (!ConnectedTexturesHelper.isAdvancedArrangement[ar])
			return getComposites(ConnectedTexturesHelper.textureFromArrangement[ar]);


		ar += matches(world, blockPos, side, u, r) * 16;
		ar += matches(world, blockPos, side, d, r) * 32;
		ar += matches(world, blockPos, side, d, l) * 64;
		ar += matches(world, blockPos, side, u, l) * 128;

		return getComposites(ConnectedTexturesHelper.textureFromArrangement[ar]);
	}

	public List<TextureComponent> getComposites(int texID) {
		int[][] texes = ConnectedTexturesHelper.texBounds[texID];
		ArrayList<TextureComponent> arrayList = Lists.newArrayListWithExpectedSize(texes.length);
		for (int[] tex : texes) {
			arrayList.add(new TextureComponent(
					icons[tex[0]],
					tex[1], tex[2],
					tex[3], tex[4]
			));
		}
		return arrayList;
	}

	@Override
	public TextureAtlasSprite getWorldIcon(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		if (side == null)
			return null;

		if (!hasConnectedTextures)
			return getBaseTexture();

		EnumFacing u = up[side.getIndex()];
		EnumFacing l = left[side.getIndex()];
		EnumFacing r = l.getOpposite();
		EnumFacing d = u.getOpposite();

		int ar = 0;
		ar += matches(world, blockPos, side, u);
		ar += matches(world, blockPos, side, r) * 2;
		ar += matches(world, blockPos, side, d) * 4;
		ar += matches(world, blockPos, side, l) * 8;

		if (!ConnectedTexturesHelper.isAdvancedArrangement[ar])
			return icons[ConnectedTexturesHelper.textureFromArrangement[ar]];


		ar += matches(world, blockPos, side, u, r) * 16;
		ar += matches(world, blockPos, side, d, r) * 32;
		ar += matches(world, blockPos, side, d, l) * 64;
		ar += matches(world, blockPos, side, u, l) * 128;

		return icons[ConnectedTexturesHelper.textureFromArrangement[ar]];
	}

	@Override
	public String getItemTexture(EnumFacing side) {
		if (hasConnectedTextures) {
			return getTexKey(ConnectedTexturesHelper.textureFromArrangement[15]);
		} else
			return baseTextureName;
	}

	public TextureAtlasSprite getBaseTexture() {
		return Textures.getSprite(baseTextureName);
	}

	private int matches(IBlockAccess world, BlockPos pos, EnumFacing side, EnumFacing... dirs) {
		return matches(world, multiOffset(pos, dirs), side, pos) ? 0 : 1;
	}
}
