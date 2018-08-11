package com.rwtema.extrautils2.compatibility;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
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
import java.util.Random;

public abstract class BlockCompat extends Block {
	public BlockCompat(Material blockMaterialIn, MapColor blockMapColorIn) {
		super(blockMaterialIn, blockMapColorIn);
	}

	public BlockCompat(Material materialIn) {
		super(materialIn);
	}

	public static IBlockState invokeGetStateForPlacement(Block block, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand, ItemStack stack) {
		return block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
	}

	public static PlayerInteractEvent.RightClickBlock onRightClickBlock(EntityPlayer player, EnumHand hand, ItemStack stack, BlockPos pos, EnumFacing side, Vec3d vec3d) {
		return ForgeHooks.onRightClickBlock(player, hand, pos, side, vec3d);
	}

	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {

	}

	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {

	}

	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}

	@Override
	public final void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		neighborChangedBase(state, worldIn, pos, blockIn);
	}

	@Override
	public final void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		getSubBlocksBase(Item.getItemFromBlock(this), tab, list);
	}

	@Override
	public final boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return onBlockActivatedBase(worldIn, pos, state, playerIn, hand, playerIn.getHeldItem(hand), facing, hitX, hitY, hitZ);
	}

	@Nullable
	public AxisAlignedBB getCollisionBoundingBoxBase(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return blockState.getBoundingBox(blockAccess, pos);
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return getCollisionBoundingBoxBase(blockState, worldIn, pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
		addCollisionBoxToListBase(state, worldIn, pos, entityBox, collidingBoxes, entityIn);
	}

	public void addCollisionBoxToListBase(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn) {

	}


	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		NonNullList<ItemStack> stacks = NonNullList.create();
		Random rand = world instanceof World ? ((World) world).rand : RANDOM;

		int count = quantityDropped(state, fortune, rand);
		for (int i = 0; i < count; i++) {
			Item item = this.getItemDropped(state, rand, fortune);
			if (item != Items.AIR) {
				stacks.add(new ItemStack(item, 1, this.damageDropped(state)));
			}
		}
		return stacks;
	}

	@Override
	public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
		drops.addAll(getDrops(world, pos, state, fortune));
	}

	public boolean causesDownwardCurrent(IBlockAccess worldIn, @Nonnull BlockPos pos, EnumFacing side) {
		return false;
	}

	@Nonnull
	@Deprecated
	public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_)
	{
		return isSideSolid(p_193383_2_,p_193383_1_, p_193383_3_, p_193383_4_) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}
}
