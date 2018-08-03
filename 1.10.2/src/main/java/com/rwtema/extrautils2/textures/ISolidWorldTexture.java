package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public interface ISolidWorldTexture {
	@SideOnly(Side.CLIENT)
	static List<TextureComponent> handleTexture(TextureAtlasSprite icon) {
		return Textures.simpleCompositeTextureCache.computeIfAbsent(
				icon,
				Textures.simpleCompostiteFunction
		);
	}

	@SideOnly(Side.CLIENT)
	@Nullable
	TextureAtlasSprite getWorldIcon(IBlockAccess world, BlockPos blockPos, EnumFacing side);

	@SideOnly(Side.CLIENT)
	String getItemTexture(EnumFacing side);

	@SideOnly(Side.CLIENT)
	default List<TextureComponent> getComposites(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		TextureAtlasSprite worldIcon = getWorldIcon(world, blockPos, side);
		return handleTexture(worldIcon);
	}

}
