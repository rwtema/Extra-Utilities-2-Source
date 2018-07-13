package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

@SuppressWarnings("unused")
public class BlockStates {
	public static final IBlockState AIR = Blocks.AIR.getDefaultState();
	public static final IBlockState COBBLESTONE = Blocks.COBBLESTONE.getDefaultState();
	public static final IBlockState LAVA_LEVEL_0 = Blocks.LAVA.getDefaultState();
	public static final IBlockState WATER_LEVEL_0 = Blocks.WATER.getDefaultState();
	public static final IBlockState WATER_LEVEL_1 = Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, 1);
	public static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
	public static final IBlockState STONE = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);
	public static final IBlockState TORCH_UP = Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP);
	public static final IBlockState SAND = Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.SAND);
	public static final IBlockState RED_SAND = Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
	public static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();
	public static final IBlockState RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();

	public static final IBlockState SNOW_LEVEL_0 = Blocks.SNOW_LAYER.getDefaultState();
}