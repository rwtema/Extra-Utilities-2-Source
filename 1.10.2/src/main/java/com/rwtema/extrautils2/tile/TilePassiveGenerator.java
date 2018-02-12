package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.blocks.BlockPassiveGenerator;
import com.rwtema.extrautils2.power.IPowerSubType;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public class TilePassiveGenerator extends TilePower implements IPowerSubType {
	@Override
	public float getPower() {
		BlockPassiveGenerator.GeneratorType value = getBlockState().getValue(BlockPassiveGenerator.GENERATOR_TYPE);
		return -value.getPowerLevel(this, world);
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		PowerManager.instance.markDirty(this);
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return getBlockState().getValue(BlockPassiveGenerator.GENERATOR_TYPE);
	}

	@Override
	public Collection<ResourceLocation> getTypes() {
		return getBlockState().getValue(BlockPassiveGenerator.GENERATOR_TYPE).types;
	}
}
