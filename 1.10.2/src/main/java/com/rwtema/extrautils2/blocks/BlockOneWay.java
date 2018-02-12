package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.transfernodes.FacingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockOneWay extends XUBlockStatic {

	public BlockOneWay() {
		super(Material.GLASS);
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == base_state.getValue(XUBlockStateCreator.ROTATION_ALL);
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

		return xuBlockState.getStateFromDropMeta(meta).withProperty(XUBlockStateCreator.ROTATION_ALL, FacingHelper.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public float getAmbientOcclusionLightValue(IBlockState state) {
		return 1;
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return super.isBlockNormalCube(state);
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return super.isNormalCube(state);
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return super.isNormalCube(state, world, pos);
	}


	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_ALL, EnumFacing.NORTH).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = BoxModel.newStandardBlock(false);
		model.addBoxI(0, 0, 0, 16, 16, 16)
				.setTexture("strange_glass_one_way_side")
				.setTextureSides(EnumFacing.DOWN, "strange_glass_one_way", EnumFacing.UP, "strange_glass_one_way_opening")
				.setInvisible(EnumFacing.UP);
		;

		model.addBox(0, 1 / 1024F, 0, 1, 1 / 1024F, 1)
				.setTextureSides(EnumFacing.UP, "strange_glass_one_way_reverse")
				.setInvisible(EnumFacing.DOWN);
		model.setLayer(BlockRenderLayer.CUTOUT);
		model.renderAsNormalBlock = false;
		return model.rotateToSide(state.getValue(XUBlockStateCreator.ROTATION_ALL));
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		IBlockState otherState = blockAccess.getBlockState(pos.offset(side));
		return (otherState != blockState || blockState.getValue(XUBlockStateCreator.ROTATION_ALL) == side) && !otherState.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}


	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}


	@Override
	public void addCollisionBoxToListBase(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		if (entityIn != null) {
			AxisAlignedBB aabb = entityIn.getEntityBoundingBox();
			EnumFacing facing = state.getValue(XUBlockStateCreator.ROTATION_ALL);
			double v = FacingHelper.aabbGetters.get(facing.getOpposite()).applyAsDouble(aabb);
			int vi = FacingHelper.blockPosGetters.get(facing.getAxis()).applyAsInt(pos);

			if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? vi < v : (vi + 1) > v) {
				return;
			}

			AxisAlignedBB bb = Block.FULL_BLOCK_AABB.offset(pos);

			if (bb.intersects(entityBox)) {
				collidingBoxes.add(bb);
			}
		}
	}

}
