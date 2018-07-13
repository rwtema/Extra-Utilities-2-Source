package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SimpleWorldTexture implements ISolidWorldTexture {
	final String texture;

	public SimpleWorldTexture(String texture) {
		this.texture = texture;
		Textures.register(texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getWorldIcon(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		return Textures.getSprite(texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemTexture(EnumFacing side) {
		return texture;
	}
}
