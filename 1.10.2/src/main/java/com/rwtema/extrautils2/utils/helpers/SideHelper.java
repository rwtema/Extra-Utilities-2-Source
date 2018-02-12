package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

public class SideHelper {
	public static EnumFacing[][] edges = new EnumFacing[][]{
			{EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.NORTH, EnumFacing.WEST},
			{EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.WEST},
			{EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH},
			{EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH},
			{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.WEST},
			{EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.WEST},
			{EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.UP},
			{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.UP},
			{EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.UP},
			{EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.UP},
	};
	public static EnumFacing[][] corners = new EnumFacing[][]{
			{EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH},
			{EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH},
			{EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH},
			{EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.SOUTH},
			{EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH},
			{EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.SOUTH},
	};

	public static EnumFacing[][] perp_sides = new EnumFacing[][]{
			{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
	};

	public static EnumFacing[][] crossProd;

	static {
		crossProd = new EnumFacing[6][6];
		BlockPos zero = new BlockPos(0, 0, 0);
		for (int i = 0; i < EnumFacing.values().length; i++) {
			for (int j = 0; j < EnumFacing.values().length; j++) {
				EnumFacing a = EnumFacing.values()[i];
				EnumFacing b = EnumFacing.values()[j];
				BlockPos crossP = zero.offset(a).crossProduct(zero.offset(b));
				if (!crossP.equals(zero)) {
					crossProd[i][j] = EnumFacing.getFacingFromVector(crossP.getX(), crossP.getY(), crossP.getZ());
				}
			}
		}
	}
}
