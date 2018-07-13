package com.rwtema.extrautils2.transfernodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.extrautils2.utils.datastructures.FunctionABBool;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class FacingHelper {
	public static final EnumMap<EnumFacing, EnumSet<EnumFacing>> orthogonal = CollectionHelper.populateEnumMultiMap(EnumFacing.class,
			(facing, facing2) -> facing.getAxis() != facing2.getAxis()
	);


	public static final EnumMap<EnumFacing, BiFunction<AxisAlignedBB, Double, AxisAlignedBB>> aabbsetters = new EnumMap<>(
			ImmutableMap.<EnumFacing, BiFunction<AxisAlignedBB, Double, AxisAlignedBB>>builder()
					.put(EnumFacing.DOWN, (b, t) -> new AxisAlignedBB(b.minX, t, b.minZ, b.maxX, b.maxY, b.maxZ))
					.put(EnumFacing.UP, (b, t) -> new AxisAlignedBB(b.minX, b.minY, b.minZ, b.maxX, t, b.maxZ))
					.put(EnumFacing.NORTH, (b, t) -> new AxisAlignedBB(b.minX, b.minY, t, b.maxX, b.maxY, b.maxZ))
					.put(EnumFacing.SOUTH, (b, t) -> new AxisAlignedBB(b.minX, b.minY, b.minZ, b.maxX, b.maxY, t))
					.put(EnumFacing.WEST, (b, t) -> new AxisAlignedBB(t, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ))
					.put(EnumFacing.EAST, (b, t) -> new AxisAlignedBB(b.minX, b.minY, b.minZ, t, b.maxY, b.maxZ))
					.build()
	);


	public static final EnumMap<EnumFacing, ToIntFunction<BlockPos>> blockFaceGetters = new EnumMap<>(
			ImmutableMap.<EnumFacing, ToIntFunction<BlockPos>>builder()
					.put(EnumFacing.DOWN, t -> t.getY())
					.put(EnumFacing.UP, t -> t.getY() + 1)
					.put(EnumFacing.NORTH, t -> t.getZ())
					.put(EnumFacing.SOUTH, t -> t.getZ() + 1)
					.put(EnumFacing.WEST, t -> t.getX())
					.put(EnumFacing.EAST, t -> t.getX() + 1)
					.build()
	);

	public static final EnumMap<EnumFacing.Axis, ToIntFunction<BlockPos>> blockPosGetters = new EnumMap<>(
			ImmutableMap.<EnumFacing.Axis, ToIntFunction<BlockPos>>builder()
					.put(EnumFacing.Axis.Y, Vec3i::getY)
					.put(EnumFacing.Axis.X, Vec3i::getX)
					.put(EnumFacing.Axis.Z, Vec3i::getZ)
					.build()
	);

	public static final EnumMap<EnumFacing, ToDoubleFunction<AxisAlignedBB>> aabbGetters = new EnumMap<>(
			ImmutableMap.<EnumFacing, ToDoubleFunction<AxisAlignedBB>>builder()
					.put(EnumFacing.DOWN, t -> t.minY)
					.put(EnumFacing.UP, t -> t.maxY)
					.put(EnumFacing.NORTH, t -> t.minZ)
					.put(EnumFacing.SOUTH, t -> t.maxZ)
					.put(EnumFacing.WEST, t -> t.minX)
					.put(EnumFacing.EAST, t -> t.maxX)
					.build()
	);

	public static final EnumMap<EnumFacing, List<EnumFacing>> lists = CollectionHelper.populateEnumMap(EnumFacing.class, ImmutableList::of);
	public static final EnumMap<EnumFacing, EnumSet<EnumFacing>> nonEqual = CollectionHelper.populateEnumMultiMap(EnumFacing.class, (FunctionABBool<EnumFacing, EnumFacing>) (facing, facing2) -> facing != facing2);
	public static final EnumMap<EnumFacing, EnumSet<EnumFacing>> horizontalOrthogonal = CollectionHelper.populateEnumMultiMap(EnumFacing.class, (FunctionABBool<EnumFacing, EnumFacing>) (facing, facing2) -> facing.getAxis() != facing2.getAxis() && facing2.getAxis() != EnumFacing.Axis.Y);
	public static final EnumFacing[] facingPlusNull = new EnumFacing[]{
			null, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH
	};

	private final static Random r = new Random();
	private static final EnumFacing[] facingValues = EnumFacing.values();
	public static EnumFacing[][] randOrders;

	static {
		EnumFacing[] base = EnumFacing.values();
		randOrders = new EnumFacing[12][6];

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				int k = (i + j) % 6;
				randOrders[i * 2][j] = base[k];
				randOrders[i * 2 + 1][j] = base[5 - k];
			}
		}
	}

	public static Iterable<EnumFacing> getRandomFaceOrder() {
		return new FaceIterRandom();
	}

	public static EnumFacing getDirectionFromEntityLiving(BlockPos pos, EntityLivingBase placer) {
		if (Math.abs(placer.posX - (double) ((float) pos.getX() + 0.5F)) < 2.0D && Math.abs(placer.posZ - (double) ((float) pos.getZ() + 0.5F)) < 2.0D) {
			double d0 = placer.posY + (double) placer.getEyeHeight();

			if (d0 - (double) pos.getY() > 2.0D) {
				return EnumFacing.UP;
			}

			if ((double) pos.getY() - d0 > 0.0D) {
				return EnumFacing.DOWN;
			}
		}

		return placer.getHorizontalFacing().getOpposite();
	}

	private static class FaceIterRandom implements Iterable<EnumFacing>, Iterator<EnumFacing> {
		byte[] b = new byte[]{0, 1, 2, 3, 4, 5};
		byte i = 0;

		@Override
		public Iterator<EnumFacing> iterator() {
			i = 0;
			return this;
		}

		@Override
		public boolean hasNext() {
			return i < 6;
		}

		@Override
		public EnumFacing next() {
			if (i == 5) {
				i++;
				return facingValues[b[5]];
			} else {
				int k = i + r.nextInt(6 - i);
				if (k == i) {
					i++;
					return facingValues[b[k]];
				}

				byte t = b[k];
				b[k] = b[i];
				b[i] = t;
				i++;

				return facingValues[t];
			}

		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
