package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemBlockPlantable extends XUItemBlock implements IPlantable {
	IPlantable plantable;

	public ItemBlockPlantable(Block block) {
		super(block);
		plantable = (IPlantable) block;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return plantable.getPlantType(world, pos);
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return plantable.getPlant(world, pos);
	}
}
