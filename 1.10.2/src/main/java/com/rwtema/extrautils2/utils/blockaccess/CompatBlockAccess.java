package com.rwtema.extrautils2.utils.blockaccess;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class CompatBlockAccess implements IBlockAccess {

	@SideOnly(Side.CLIENT)
	public boolean extendedLevelsInChunkCache() {
		return false;
	}


}
