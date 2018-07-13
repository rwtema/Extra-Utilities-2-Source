package com.rwtema.extrautils2.transfernodes;

import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.INBTSerializable;

public class Ping implements INBTSerializable<NBTTagIntArray> {
	private IBlockAccess world;
	private IBuffer buffer;
	private BlockPos pos = null;
	private EnumFacing direction;

	private BlockPos home;
	private EnumFacing homeDirection;

	public boolean needsInit() {
		return world == null || home == null || homeDirection == null || buffer == null;
	}

	public void init(IBlockAccess world, BlockPos home, EnumFacing homeDirection, IBuffer buffer) {
		this.world = world;
		this.home = home;
		this.homeDirection = homeDirection;
		this.buffer = buffer;
	}

	public BlockPos getPos() {
		if (pos == null) {
			resetPosition();
		}
		return pos;
	}

	public EnumFacing getDirection() {
		if (direction == null) {
			resetPosition();
		}
		return direction;
	}

	public void resetPosition() {
		pos = getDefaultPos();
		direction = getDefaultSide();
	}

	protected EnumFacing getDefaultSide() {
		return homeDirection;
	}

	protected BlockPos getDefaultPos() {
		return home;
	}


	public void advanceSearch(IPipe pipe) {

		if (pipe == null) {
			resetPosition();
			return;
		}

		boolean mayHavePriorities = pipe.mayHavePriorities();

		GrocketPipeFilter.Priority found = null;

		BlockPos newPos = null;
		EnumFacing newDirection = null;


		for (EnumFacing facing : FacingHelper.getRandomFaceOrder()) {
			if (facing == direction.getOpposite())
				continue;

			if (pipe.canOutput(world, pos, facing, buffer)) {
				BlockPos offset = pos.offset(facing);
				if (TransferHelper.isInputtingPipe(world, offset, facing.getOpposite())) {
					if (mayHavePriorities) {
						GrocketPipeFilter.Priority priority = pipe.getPriority(world, pos, facing);
						if (found == null || priority.ordinal() < found.ordinal()) {
							found = priority;
						} else {
							continue;
						}
					}

					if (!mayHavePriorities || found == GrocketPipeFilter.Priority.HIGH) {
						pos = offset;
						direction = facing;
						return;
					} else {
						newPos = offset;
						newDirection = facing;
					}
				}
			}
		}

		if (found != null) {
			pos = newPos;
			direction = newDirection;
			return;
		}

		resetPosition();
	}

	@Override
	public NBTTagIntArray serializeNBT() {
		if (pos == null || direction == null)
			return new NBTTagIntArray(new int[]{0, 0, 0, 0});
		return new NBTTagIntArray(new int[]{pos.getX(), pos.getY(), pos.getZ(), direction.ordinal()});
	}

	@Override
	public void deserializeNBT(NBTTagIntArray nbt) {
		int[] ints = nbt.getIntArray();
		if (ints.length != 4) return;

		int i = ints[3];
		if (i < 0 || i >= 6) {
			return;
		}

		direction = EnumFacing.values()[i];

		pos = new BlockPos(ints[0], ints[1], ints[2]);
	}
}
