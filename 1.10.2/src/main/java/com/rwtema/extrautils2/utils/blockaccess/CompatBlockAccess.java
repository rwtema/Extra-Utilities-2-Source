package com.rwtema.extrautils2.utils.blockaccess;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class CompatBlockAccess implements IBlockAccess {

	@SideOnly(Side.CLIENT)
	public boolean extendedLevelsInChunkCache() {
		return false;
	}


}
