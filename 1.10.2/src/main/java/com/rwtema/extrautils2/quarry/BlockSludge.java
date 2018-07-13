package com.rwtema.extrautils2.quarry;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.textures.SpriteSub;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class BlockSludge extends XUBlockStatic {
	final static PropertyInteger DECAY_LEVEL = PropertyInteger.create("decay", 0, 3);
	public final int TICK_TIME = 128;

	public BlockSludge() {
		super(Material.SPONGE);
		setHardness(0);

	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addWorldProperties(DECAY_LEVEL).build();
	}

	@Override
	@Nonnull
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}


	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.scheduleBlockUpdate(pos.toImmutable(), this, 1 + worldIn.rand.nextInt(TICK_TIME), 0);
	}

	@Override
	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
		super.neighborChangedBase(state, worldIn, pos, neighborBlock);
		worldIn.scheduleBlockUpdate(pos, this, TICK_TIME, 0);
	}

	public boolean isFluid(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getMaterial().isLiquid() || state.getBlock() instanceof IFluidBlock;

	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.isRemote) return;
		int i = state.getValue(DECAY_LEVEL);

		if (isFluid(worldIn, pos.down())) {
			worldIn.setBlockState(pos.down(), state, 3);
		}

		boolean waterNearby = false;
		boolean spread = false;
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos offset = pos.offset(facing);

			if (isFluid(worldIn, offset)) {
				waterNearby = true;

				if (!isFluid(worldIn, offset.up())) {
					BlockPos down = offset.down();
					if (blocksWater(worldIn, down)) {
						worldIn.setBlockState(offset, state);
					} else {
						EnumFacing opposite = facing.getOpposite();
						for (EnumFacing facing2 : EnumFacing.HORIZONTALS) {
							if (facing2 != opposite) {
								BlockPos offset1 = offset.offset(facing2);
								if (blocksWater(worldIn, offset1)) {
									worldIn.setBlockState(offset, state);
									break;
								}
							}
						}
					}
				}
			}
		}

		if (!waterNearby) {
			waterNearby = isFluid(worldIn, pos.up());
		}

		if (waterNearby) {
			return;
		}

		if (i == 3) {
			worldIn.setBlockToAir(pos);
		} else {
			worldIn.setBlockState(pos, state.withProperty(DECAY_LEVEL, i + 1));
			worldIn.scheduleBlockUpdate(pos, this, TICK_TIME, 0);
		}
	}

	private boolean blocksWater(World worldIn, BlockPos offset1) {
		return worldIn.getBlockState(offset1).getBlock() == this;
	}

	@Override
	@Nonnull
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		return ImmutableList.of();
	}

	@Override
	public void registerTextures() {
		for (int i = 0; i < 4; i++) {
			Textures.textureNames.put("sludge_" + i, new SpriteSub("sludge", 0, i, 1, 4, 1));
		}
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		return BoxModel.newStandardBlock("sludge_" + state.getValue(DECAY_LEVEL));
	}
}
