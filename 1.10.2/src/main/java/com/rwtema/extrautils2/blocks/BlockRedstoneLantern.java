package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.BoxSingleQuad;
import com.rwtema.extrautils2.backend.model.UV;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockRedstoneLantern extends XUBlockStatic {
	static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	final int[][] TEXTURE_NUMBER_BOUNDS = new int[][]{
//           x, y, w
			{0, 0, 5}, // 0
			{6, 0, 5}, // 1
			{12, 0, 5}, // 2
			{18, 0, 5}, // 3
			{24, 0, 5}, // 4

			{0, 8, 5}, // 5
			{6, 8, 5}, // 6
			{12, 8, 5}, // 7
			{18, 8, 5}, // 8
			{24, 8, 5}, // 9

			{0, 16, 9}, // 10
			{10, 16, 9}, // 11
			{20, 16, 9}, // 12

			{0, 24, 9}, // 13
			{10, 24, 9}, // 14
			{20, 24, 9}, // 15
	};

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addWorldProperties(POWER).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		int v = state.getValue(POWER);
		BoxModel model = new BoxModel();

		float eps = 1e-4F;
		float par5 = 1 / 16F + eps;
		float par7 = 15 / 16F - eps;
		model.addBox(par5, par5, par5, par7, par7, par7, "redstone_lantern").setLayer(BlockRenderLayer.CUTOUT);

		float x01 = 1.05F / 16F;
		float x11 = 14.95F / 16F;
		Box redstone_box = new Box(x01, x01, x01, x11, x11, x11).setTexture("redstone_lantern");
		redstone_box.setTextureBounds(
				new float[][]{
						{15, v, 16, v + 1},
						{15, v, 16, v + 1},
						{15, v, 16, v + 1},
						{15, v, 16, v + 1},
						{15, v, 16, v + 1},
						{15, v, 16, v + 1},
				}
		);
		model.add(redstone_box);

		int[] bounds = TEXTURE_NUMBER_BOUNDS[v];
		int x = bounds[0];
		int y = bounds[1];
		int w = bounds[2];
		int h = 7;

		float u0 = x / 32F;
		float u1 = (x + w) / 32F;
		float v0 = 1 - ((y + h) / 32F);
		float v1 = 1 - (y / 32F);


		float x0 = (16 - w) / 16F / 2F;
		float x1 = (16 + w) / 16F / 2F;
		float z0 = (16 - h) / 16F / 2F;
		float z1 = (16 + h) / 16F / 2F;
		for (EnumFacing facing : EnumFacing.values()) {
			model.add(
					new BoxSingleQuad(
							new UV(x0, 1 / 16F, z0, u0, v0),
							new UV(x1, 1 / 16F, z0, u1, v0),
							new UV(x1, 1 / 16F, z1, u1, v1),
							new UV(x0, 1 / 16F, z1, u0, v1)
					).setTexture("redstone_lantern_numbers").rotateToSide(facing).setLayer(BlockRenderLayer.CUTOUT));
		}

		return model;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			if (playerIn.isSneaking()) {
				int value = state.getValue(POWER) - 1;
				state = state.withProperty(POWER, value < 0 ? 15 : value);
			} else {
				state = state.cycleProperty(POWER);
			}
			worldIn.setBlockState(pos, state, 3);
			float f = 0.5F + 0.1F * state.getValue(POWER) / 15F;
			worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
			CompatHelper.notifyNeighborsOfStateChange(worldIn, pos, this);
			for (EnumFacing facing : EnumFacing.values()) {
				CompatHelper.notifyNeighborsOfStateChange(worldIn, pos.offset(facing), this);
			}
//			SpecialChat.sendChat(playerIn, new TextComponentString("" + state.getValue(POWER)));
		}
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return blockState.getValue(POWER);
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		EnumFacing.Axis axis = side.getAxis();
		if (axis != EnumFacing.Axis.Y) {
			IBlockState state = blockAccess.getBlockState(pos.offset(side.getOpposite()));
			Block block = state.getBlock();
			if (block == Blocks.POWERED_COMPARATOR || block == Blocks.UNPOWERED_COMPARATOR) {
				EnumFacing compFacing = state.getValue(BlockHorizontal.FACING);
				if (compFacing.getAxis() != axis) {
					return blockState.getValue(POWER);
				}
			}
		}

		return 0;
	}
}
