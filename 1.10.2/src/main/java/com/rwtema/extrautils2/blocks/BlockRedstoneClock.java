package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockFull;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.textures.TextureRedstoneClock;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRedstoneClock extends XUBlockFull {
	public static final PropertyEnumSimple<PowerState> PROPERTY_POWER_STATE = new PropertyEnumSimple<>(PowerState.class);
	public final static int POWER_TIME = 2;
	public static final int TICK_TIME = 20;
	boolean canProvidePower = true;
	boolean changing = false;

	public BlockRedstoneClock() {
		super(Material.ROCK);

		for (PowerState powerState : PowerState.values()) {
			powerState.state = getDefaultState().withProperty(PROPERTY_POWER_STATE, powerState);
		}
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this, PROPERTY_POWER_STATE);
	}

	@Override
	public void registerTextures() {
		Textures.register("redstone_clock_off");
		Textures.textureNames.put("redstone_clock_on", new TextureRedstoneClock(ExtraUtils2.MODID + ":redstone_clock_on"));
	}

	@Override
	public String getTexture(IBlockState state, EnumFacing side) {
		if (state.getValue(PROPERTY_POWER_STATE) == PowerState.DISABLED)
			return "redstone_clock_off";
		else
			return "redstone_clock_on";
	}

	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return canProvidePower && state.getValue(PROPERTY_POWER_STATE) == PowerState.ENABLED_POWERED ? 15 : 0;
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return true;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.scheduleBlockUpdate(pos, this, 1, 0);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean causesDownwardCurrent(IBlockAccess worldIn, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
		if (changing || worldIn.isRemote) return;
		boolean powered = isPowered(worldIn, pos);

		PowerState value = state.getValue(PROPERTY_POWER_STATE);

		changing = true;
		if (powered && value != PowerState.DISABLED) {
			worldIn.setBlockState(pos, PowerState.DISABLED.state, 3);

		} else if (!powered && value == PowerState.DISABLED) {
			int l = (int) (worldIn.getTotalWorldTime() % TICK_TIME);

			if (l < POWER_TIME) {
				worldIn.setBlockState(pos, PowerState.ENABLED_POWERED.state, 3);
				worldIn.scheduleBlockUpdate(pos, this, POWER_TIME - l, 0);
			} else {
				worldIn.setBlockState(pos, PowerState.ENABLED_NOT_POWERED.state, 3);
				worldIn.scheduleBlockUpdate(pos, this, TICK_TIME - l, 0);
			}
		}
		changing = false;
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.isRemote) return;
		PowerState powerState = state.getValue(PROPERTY_POWER_STATE);

		if (powerState == PowerState.DISABLED) return;

		int l = (int) (worldIn.getTotalWorldTime() % TICK_TIME);

		changing = true;
		if (l < POWER_TIME) {
			worldIn.setBlockState(pos, PowerState.ENABLED_POWERED.state, 1);
			worldIn.scheduleBlockUpdate(pos, this, POWER_TIME - l, 0);
		} else {
			worldIn.setBlockState(pos, PowerState.ENABLED_NOT_POWERED.state, 1);
			if (isPowered(worldIn, pos)) {
				worldIn.setBlockState(pos, PowerState.DISABLED.state, 3);
			} else {
				worldIn.scheduleBlockUpdate(pos, this, TICK_TIME - l, 0);
			}
		}
		changing = false;
	}

	private boolean isPowered(IBlockAccess worldIn, BlockPos pos) {
		canProvidePower = false;
		boolean powered = false;
		for (EnumFacing side : EnumFacing.values()) {
			if (worldIn.getStrongPower(pos.offset(side), side) > 0) {
				powered = true;
				break;
			}
		}
		canProvidePower = true;
		return powered;
	}

	enum PowerState {
		ENABLED_NOT_POWERED,
		ENABLED_POWERED,
		DISABLED;

		public IBlockState state;
	}
}
