package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.power.energy.EmptyEnergyHandler;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class TileQuarryProxy extends XUTile {
	NBTSerializable.NBTEnum<EnumFacing> facing = registerNBT("facing", new NBTSerializable.NBTEnum<>(EnumFacing.DOWN));
	Boolean powered = null;

	@SuppressWarnings("unused")
	public TileQuarryProxy() {

	}

	public TileQuarryProxy(EnumFacing facing) {
		this.facing.value = facing;
	}

	@Nullable
	public TileQuarry getParent() {
		EnumFacing value = facing.value;
		if (world == null || pos == null) return null;
		TileEntity tile = world.getTileEntity(pos.offset(value));
		if (tile instanceof TileQuarry) {
			return (TileQuarry) tile;
		} else
			return null;
	}

	@Nullable
	@Override
	public IEnergyStorage getEnergyHandler(EnumFacing facing) {
		return getEnergyHandler();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileQuarry parent = getParent();
		return parent != null && parent.onBlockActivated(worldIn, pos.offset(facing.value), worldIn.getBlockState(pos.offset(facing.value)), playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}

	public IEnergyStorage getEnergyHandler() {
		TileQuarry parent = getParent();
		if (parent != null) return parent.energy;
		return EmptyEnergyHandler.INSTANCE;
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		powered = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
		TileQuarry parent = getParent();
		if (parent != null) {
			parent.redstoneDirty = true;
		}
	}

	public boolean isPowered() {
		if (powered == null) powered = world.isBlockIndirectlyGettingPowered(pos) > 0;
		return powered;
	}
}
