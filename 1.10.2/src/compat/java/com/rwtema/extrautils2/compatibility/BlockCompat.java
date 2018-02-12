package com.rwtema.extrautils2.compatibility;

import com.rwtema.extrautils2.fakeplayer.XUFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BlockCompat extends Block {
	public BlockCompat(Material blockMaterialIn, MapColor blockMapColorIn) {
		super(blockMaterialIn, blockMapColorIn);
	}

	public BlockCompat(Material materialIn) {
		super(materialIn);
	}

	public static IBlockState invokeGetStateForPlacement(Block block, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int i, EntityPlayer fakePlayer, EnumHand mainHand, ItemStack stack) {
		return block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, i, fakePlayer, stack);
	}

	public static PlayerInteractEvent.RightClickBlock onRightClickBlock(EntityPlayer fakePlayer, EnumHand mainHand, ItemStack copy, BlockPos pos, EnumFacing side, Vec3d vec3d) {
		return ForgeHooks.onRightClickBlock(fakePlayer, mainHand, copy, pos, side, vec3d);
	}

	public boolean canReplaceBase(World p_176193_1_, BlockPos p_176193_2_, EnumFacing p_176193_3_, ItemStack p_176193_4_) {
		return this.canPlaceBlockOnSide(p_176193_1_, p_176193_2_, p_176193_3_);
	}

	@Override
	public final boolean func_176193_a(@Nonnull World p_176193_1_, @Nonnull BlockPos p_176193_2_, @Nonnull EnumFacing p_176193_3_, @Nullable ItemStack p_176193_4_) {
		return canReplaceBase(p_176193_1_, p_176193_2_, p_176193_3_, p_176193_4_);
	}

	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		super.getSubBlocks(itemIn, tab, list);
	}

	public abstract void addCollisionBoxToListBase(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn);

	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {

	}

	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public final void getSubBlocks(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		getSubBlocksBase(itemIn, tab, list);
	}

	@Override
	public final void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		neighborChangedBase(state, worldIn, pos, blockIn);
	}

	@Override
	public final boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
		return onBlockActivatedBase(worldIn, pos, state, playerIn, hand, stack, side, hitX, hitY, hitZ);
	}

	@Nullable
	public AxisAlignedBB getCollisionBoundingBoxBase(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return blockState.getBoundingBox(blockAccess, pos);
	}

	@Nullable
	@Override
	public final AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return getCollisionBoundingBoxBase(blockState, worldIn, pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		addCollisionBoxToListBase(state, worldIn, pos, entityBox, collidingBoxes, entityIn);
	}
}
