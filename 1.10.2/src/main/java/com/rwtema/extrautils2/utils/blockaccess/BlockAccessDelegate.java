package com.rwtema.extrautils2.utils.blockaccess;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockAccessDelegate extends CompatBlockAccess implements IBlockAccess {
	@Nullable
	private IBlockAccess base;

	public BlockAccessDelegate() {
		this(null);
	}

	public BlockAccessDelegate(@Nullable IBlockAccess base) {
		this.setBase(base);
	}

	@Override
	public TileEntity getTileEntity(@Nonnull BlockPos pos) {
		if (getBase() == null) return null;
		return getBase().getTileEntity(pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
		if (getBase() == null) return 0;
		return getBase().getCombinedLight(pos, lightValue);
	}

	@Nonnull
	@Override
	public IBlockState getBlockState(@Nonnull BlockPos pos) {
		if (getBase() == null) return Blocks.AIR.getDefaultState();
		return getBase().getBlockState(pos);
	}

	@Override
	public boolean isAirBlock(@Nonnull BlockPos pos) {
		return getBase() == null || getBase().isAirBlock(pos);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Biome getBiome(@Nonnull BlockPos pos) {
		if (getBase() == null) return Biomes.PLAINS;
		return getBase().getBiome(pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean extendedLevelsInChunkCache() {
		return true;
	}

	@Override
	public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
		if (getBase() == null) return 0;
		return getBase().getStrongPower(pos, direction);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public WorldType getWorldType() {
		if (getBase() == null) return WorldType.DEFAULT;
		return getBase().getWorldType();
	}

	@Override
	public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
		return getBase() != null && getBase().isSideSolid(pos, side, _default);
	}

	@Nullable
	public IBlockAccess getBase() {
		if(base == this) return null;
		return base;
	}

	public void setBase(@Nullable IBlockAccess base) {
		if(base == this) throw new IllegalStateException();
		if (base instanceof BlockAccessDelegate) {
			setBase(((BlockAccessDelegate) base).getBase());
		}
		this.base = base;
	}
}
