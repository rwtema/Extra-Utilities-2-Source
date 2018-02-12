package com.rwtema.extrautils2.lighting;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public interface ILight {
	World getLightWorld();

	float getLightOffset(BlockPos pos, EnumSkyBlock type);

	EnumSkyBlock[] getLightType();

}
