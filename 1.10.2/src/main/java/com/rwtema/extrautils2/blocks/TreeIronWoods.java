package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TreeIronWoods extends XUTree {
	public static final PropertyEnumSimple<TreeType> TREE_TYPE = new PropertyEnumSimple<>(TreeType.class);

	public TreeIronWoods() {
		super("ironwood", ImmutableMap.of(
				ImmutableMap.of(TREE_TYPE, TreeType.RAW),
				new TreeModelTex("tree/iron_wood_raw"),
				ImmutableMap.of(TREE_TYPE, TreeType.BURNT),
				new TreeModelTex("tree/iron_wood_burnt"))
		);
	}

	@Override
	protected XUBlockStateCreator.Builder getBuilder(XUBlock block) {
		return new XUBlockStateCreator.Builder(block).addDropProperties(TREE_TYPE);
	}

	@Override
	public XUTreePlanks getXuTreePlanks() {
		return new XUTreePlanks() {
			@Override
			public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 0;
			}

			@Override
			public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
				return true;
			}

			@Override
			public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 15;
			}

			@Override
			public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
				return true;
			}
		};
	}

	@Override
	public XUTreeSapling getXuTreeSapling() {
		return super.getXuTreeSapling();
	}

	@Override
	public XUTreeLog getXuTreeLog() {
		return new XUTreeLog() {
			{
				setTickRandomly(true);
			}

			@Override
			public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				super.updateTick(worldIn, pos, state, rand);
			}

			@Nonnull
			@Override
			public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
				if (state.getValue(TREE_TYPE) == TreeType.RAW) {
					return Blocks.LOG.getDrops(world, pos, Blocks.LOG.getDefaultState(), fortune);
				} else {
					return super.getDrops(world, pos, state, fortune);
				}
			}

			@Override
			public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 0;
			}

			@Override
			public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
				if (neighborBlock == Blocks.FIRE) {

				}
			}

			@Override
			public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
				return true;
			}

			@Override
			public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 15;
			}

			@Override
			public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
				return true;
			}
		};
	}

	@Override
	public XUTreeLeaves getXuTreeLeaves() {
		return new XUTreeLeaves() {
			{
				setTickRandomly(true);
			}

			@Override
			public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
				super.updateTick(worldIn, pos, state, rand);
			}

			@Override
			public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 0;
			}

			@Override
			public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
				return true;
			}

			@Override
			public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
				return 15;
			}

			@Override
			public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
				return true;
			}
		};
	}

	@Override
	public int getHeight(World worldIn, Random rand, IBlockState state, BlockPos pos) {
		return 5;
	}

	@Override
	protected int getLeavesColour(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
		if (state.getValue(TREE_TYPE) == TreeType.BURNT) {
			return 0xffffffff;
		}
		return super.getLeavesColour(state, worldIn, pos, tintIndex);
	}

	public enum TreeType {
		RAW,
		BURNT
	}
}
