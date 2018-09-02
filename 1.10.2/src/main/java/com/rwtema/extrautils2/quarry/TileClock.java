package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.WeakHashMap;

public class TileClock extends TilePower implements ITickable {

	public NBTSerializable.NBTBoolean powered = registerNBT("powered", new NBTSerializable.NBTBoolean());
	public NBTSerializable.NBTBoolean movingTime = registerNBT("moving", new NBTSerializable.NBTBoolean());
	public NBTSerializable.NBTDouble targetTime = registerNBT("target_time", new NBTSerializable.NBTDouble(-1));

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float getPower() {
		return 0;
	}

	@Override
	public void update() {
		if (movingTime.value ) {

		}
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
		powered.value = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
	}

}
