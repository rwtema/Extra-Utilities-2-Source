package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.utils.CapGetter;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class TransferHelper {
	public static boolean isInputtingPipe(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		IPipe pipe = getPipe(world, pos);
		return pipe != null && pipe.canInput(world, pos, dir);
	}

	@Nullable
	public static IPipe getPipe(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof IPipe)
			return (IPipe) tileEntity;

		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof IPipe) {
			return (IPipe) block;
		}

		return null;
	}

	public static boolean isOutputtingPipe(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		IPipe pipe = getPipe(world, pos);
		return pipe != null && pipe.canOutput(world, pos, dir, null);
	}

	public static boolean hasValidCapability(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity != null && hasValidCapability(tileEntity, dir);

	}

	public static boolean hasValidCapability(ICapabilityProvider tileEntity, EnumFacing dir) {
		for (CapGetter<?> capability : CapGetter.caps) {
			if (capability.hasInterface(tileEntity, dir)) {
				return true;
			}
		}
		return false;
	}
}
