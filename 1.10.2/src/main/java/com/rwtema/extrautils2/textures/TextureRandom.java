package com.rwtema.extrautils2.textures;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class TextureRandom extends TextureLocation {

	public TextureRandom(String texture) {
		super(texture);
	}

	@Override
	protected void assignBaseTextures() {
		Random random = new Random(12234L);
		for (int i = 0; i < 6; i++) {
			int j = random.nextInt(textures.length);
			baseTexture[i] = textures[j % textures.length];
		}
	}

	@Override
	protected int getRandomIndex(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		long random = MathHelper.getCoordinateRandom(
				blockPos.getX() + side.ordinal() * 12234533,
				blockPos.getY(),
				blockPos.getZ()
		);

		int a = (int) (random ^ (random >> 32));
		a = Math.abs(a);
		return a;
	}

}
