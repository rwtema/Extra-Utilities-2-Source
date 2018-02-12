package com.rwtema.extrautils2.modcompat;

import com.rwtema.extrautils2.transfernodes.BlockTransferPipe;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TransferPipesWrapper {



	private static class NoTilePipeMultiPart implements IMultipart {
		final Block pipe;

		private NoTilePipeMultiPart(Block pipe) {
			this.pipe = pipe;
		}

		@Override
		public Block getBlock() {
			return pipe;
		}

		@Override
		public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
			return EnumCenterSlot.CENTER;
		}

		@Override
		public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
			return EnumCenterSlot.CENTER;
		}

		final static AxisAlignedBB centerBounds = new AxisAlignedBB(6/16F,6/16F,6/16F,1-6/16F,1-6/16F,1-6/16F);

		@Override
		public AxisAlignedBB getBoundingBox(IPartInfo part) {
			return centerBounds;
		}
	}
}
