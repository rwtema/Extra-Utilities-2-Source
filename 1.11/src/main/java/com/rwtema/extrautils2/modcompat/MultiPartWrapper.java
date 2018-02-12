package com.rwtema.extrautils2.modcompat;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MultiPartWrapper implements IMultipart {


	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return null;
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public Block getBlock() {
		return null;
	}
}
