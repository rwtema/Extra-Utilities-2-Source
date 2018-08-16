package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.utils.CapGetter;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public interface IPipe {

	boolean canInput(IBlockAccess world, BlockPos pos, EnumFacing dir);

	boolean canOutput(IBlockAccess world, BlockPos pos, EnumFacing dir, @Nullable IBuffer buffer);

	boolean canOutputTile(IBlockAccess world, BlockPos pos, EnumFacing dir);

	<T> boolean hasCapability(IBlockAccess world, BlockPos pos, EnumFacing side, CapGetter<T> capability);

	<T> T getCapability(IBlockAccess world, BlockPos pos, EnumFacing side, CapGetter<T> capability);

	boolean shouldTileConnectionShowNozzle(IBlockAccess world, BlockPos pos, EnumFacing facing);

	boolean mayHavePriorities();

	GrocketPipeFilter.Priority getPriority(IBlockAccess world, BlockPos pos, EnumFacing facing);
}
