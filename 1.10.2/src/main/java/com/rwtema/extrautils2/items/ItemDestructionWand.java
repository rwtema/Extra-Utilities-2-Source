package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.PositionPool;
import com.rwtema.extrautils2.utils.helpers.PlayerHelper;
import com.rwtema.extrautils2.utils.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ItemDestructionWand extends ItemSelectionWand {
	Item[] harvestDelegates = new Item[]{Items.STONE_SHOVEL, Items.STONE_PICKAXE, Items.STONE_PICKAXE};
	Item[] speedDelegates = new Item[]{Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_PICKAXE};

	public ItemDestructionWand(String texture, String name, float[] col, int range) {
		super(texture, name, col, range);
	}

	@Override
	protected boolean initialCheck(World world, BlockPos pos, EnumFacing side, ItemStack pickBlock1, IBlockState state) {
		return !canHarvestBlock(state);
	}

	@Override
	protected int getNumBlocks(EntityPlayer player, int maxBlocks, ItemStack pickBlock1, boolean grassBlock) {
		return range;
	}

	@Override
	protected boolean checkAndAddBlocks(EntityPlayer player, World world, EnumFacing side, ItemStack pickBlock1, Block block, IBlockState mainState, PositionPool pool, BlockPos p, List<BlockPos> blocks) {
		if (!player.canHarvestBlock(mainState)) return false;
		BlockPos p1 = p.offset(side);

		if (!player.capabilities.isFlying && p.getX() == MathHelper.floor(player.posX) && p.getY() == MathHelper.floor(player.posY - 0.20000000298023224D) && p.getZ() == MathHelper.floor(player.posZ))
			return false;

		IBlockState blockState1 = world.getBlockState(p1);
		Block block1 = blockState1.getBlock();
		if (block1.isNormalCube(blockState1, world, p1)) return false;
		AxisAlignedBB collisionBoundingBox = blockState1.getCollisionBoundingBox(world, p1);
		if (collisionBoundingBox != null) {
			collisionBoundingBox = collisionBoundingBox.offset(-p1.getX(), -p1.getY(), -p1.getZ());
			switch (side) {
				case UP:
					if (collisionBoundingBox.minY <= 0) return false;
				case DOWN:
					if (collisionBoundingBox.maxY >= 1) return false;
				case SOUTH:
					if (collisionBoundingBox.minZ <= 0) return false;
					break;
				case NORTH:
					if (collisionBoundingBox.maxZ >= 1) return false;
					break;
				case EAST:
					if (collisionBoundingBox.minX <= 0) return false;
					break;
				case WEST:
					if (collisionBoundingBox.maxX >= 1) return false;
					break;
			}
		}

		blocks.add(p);
		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		World world = player.world;
		if (world.isAirBlock(pos)) return false;

		if (world.isRemote) return false;

		RayTraceResult mop = PlayerHelper.rayTrace(player);

		if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK || !mop.getBlockPos().equals(pos))
			return false;

		IBlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		ItemStack pickBlock = getStack(world, pos);

		List<BlockPos> potentialBlocks = getPotentialBlocks(player, world, pos, mop.sideHit, range, pickBlock, blockState, block);
		if (potentialBlocks.isEmpty())
			return false;

		boolean doDrops = !player.capabilities.isCreativeMode;

		for (BlockPos potentialBlock : potentialBlocks) {
			IBlockState iblockstate = world.getBlockState(potentialBlock);
			Block block1 = iblockstate.getBlock();

			if (iblockstate.getMaterial() != Material.AIR) {
//				world.playAuxSFX(2001, potentialBlock, Block.getStateId(iblockstate));

				if (doDrops) {
					block1.dropBlockAsItem(world, potentialBlock, iblockstate, 0);
				}

				world.setBlockState(potentialBlock, Blocks.AIR.getDefaultState(), 3);
				WorldHelper.markBlockForUpdate(world, potentialBlock);
			}
		}

		return true;
	}

	@Override
	public boolean canHarvestBlock(IBlockState state) {
		if (state.getMaterial().isToolNotRequired()) return true;
		for (Item item : harvestDelegates) {
			if (item.canHarvestBlock(state)) return true;
		}
		return super.canHarvestBlock(state);
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		float t = super.getStrVsBlock(stack, state);
		for (Item item : speedDelegates) {
			t = Math.max(t, item.getStrVsBlock(stack, state));
		}
		return t;
	}
//
//	@Override
//	public float getDigSpeed(ItemStack stack, net.minecraft.block.state.IBlockState state) {
//		float t = super.getDigSpeed(stack, state);
//		for (Item item : speedDelegates) {
//			t = Math.max(t, item.getDigSpeed(new ItemStack(item), state));
//		}
//		return t;
//	}


	@SubscribeEvent
	public void adjustDigSpeed(PlayerEvent.BreakSpeed event) {
		EntityPlayer player = event.getEntityPlayer();
		if (StackHelper.isNull(player.getHeldItemMainhand()) || player.getHeldItemMainhand().getItem() != this) return;

		BlockPos pos = event.getPos();

		World world = player.world;
		if (world.isAirBlock(pos)) return;

		RayTraceResult mop = PlayerHelper.rayTrace(player);

		if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK || !mop.getBlockPos().equals(pos)) {
			event.setCanceled(true);
			return;
		}

		IBlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		ItemStack pickBlock = getStack(world, pos);

		List<BlockPos> potentialBlocks = getPotentialBlocks(player, world, pos, mop.sideHit, range, pickBlock, blockState, block);

		if (potentialBlocks.isEmpty()) {
			event.setCanceled(true);
			return;
		}

		event.setNewSpeed(event.getNewSpeed() / potentialBlocks.size());
	}

}
