package com.rwtema.extrautils2.utils.blockaccess;

import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;


public class BlockAccessEmpty extends CompatBlockAccess implements IBlockAccess {
	public static final BlockAccessEmpty INSTANCE = new BlockAccessEmpty();

	@Override
	public TileEntity getTileEntity(@Nonnull BlockPos pos) {
		return null;
	}

	@Override
	public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
		return 0;
	}

	@Nonnull
	@Override
	public IBlockState getBlockState(@Nonnull BlockPos pos) {
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isAirBlock(@Nonnull BlockPos pos) {
		IBlockState state = getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	@Nonnull
	@Override
	public Biome getBiome(@Nonnull BlockPos pos) {
		return Biomes.PLAINS;
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@Override
	public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
		return 0;
	}

	@Nonnull
	@Override
	public WorldType getWorldType() {
		return WorldType.DEFAULT;
	}

	@Override
	public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
		return getBlockState(pos).isSideSolid(this, pos, side);
	}
}
