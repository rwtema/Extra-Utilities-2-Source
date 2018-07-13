package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.PositionPool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBuildersWand extends ItemSelectionWand {
	public ItemBuildersWand(String name, int range, String texture, float[] col) {
		super(texture, name, col, range);

	}

	@Override
	protected boolean initialCheck(World world, BlockPos pos, EnumFacing side, ItemStack pickBlock1, IBlockState state) {
		return !state.getBlock().canPlaceBlockOnSide(world, pos.offset(side), side) || !CompatHelper.canPlaceBlockHere(world, state.getBlock(), pos.offset(side), false, side, null, pickBlock1);
	}

	@Override
	protected int getNumBlocks(EntityPlayer player, int maxBlocks, ItemStack pickBlock1, boolean grassBlock) {
		int numBlocks = 0;

		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (StackHelper.isNonNull(stack)) {
				if (stack.getItem() == pickBlock1.getItem() && stack.getItemDamage() == pickBlock1.getItemDamage() ||
						(grassBlock && stack.getItem() == Item.getItemFromBlock(Blocks.DIRT))
						) {
					if (player.capabilities.isCreativeMode) {
						numBlocks = maxBlocks;
						break;
					}

					numBlocks += StackHelper.getStacksize(stack);
				}

				if (numBlocks >= maxBlocks) {
					numBlocks = maxBlocks;
					break;
				}
			}
		}
		return numBlocks;
	}

	@Override
	protected boolean checkAndAddBlocks(EntityPlayer player, World world, EnumFacing side, ItemStack pickBlock1, Block block, IBlockState state, PositionPool pool, BlockPos p, List<BlockPos> blocks) {
		BlockPos loc = pool.offset(p, side);

		if (!player.canPlayerEdit(loc, side, pickBlock1))
			return false;

		if (!CompatHelper.canPlaceBlockHere(world, block, loc, false, side, null, pickBlock1))
			return false;

		blocks.add(loc);
		return true;
	}


	@Nonnull
	@Override
	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return EnumActionResult.SUCCESS;
		}

		if (!player.capabilities.allowEdit) {
			return EnumActionResult.FAIL;
		}

		IBlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();

		ItemStack pickBlock = getStack(world, pos);

		List<BlockPos> blocks = getPotentialBlocks(player, world, pos, side, range, pickBlock, blockState, block);
		if (blocks.isEmpty()) return EnumActionResult.FAIL;
		int slot = 0;

		int origSlot = player.inventory.currentItem;
		ItemStack origStack = player.inventory.getStackInSlot(origSlot);

		if (!InventoryPlayer.isHotbar(origSlot) || StackHelper.isNull(origStack)) {
			return EnumActionResult.FAIL;
		}

		boolean grassBlock = block == Blocks.GRASS || block == Blocks.MYCELIUM;

		for (BlockPos p : blocks) {

			ItemStack stackInSlot = null;
			for (; slot < player.inventory.getSizeInventory(); slot++) {
				if (slot == origSlot) continue;
				stackInSlot = player.inventory.getStackInSlot(slot);
				if (StackHelper.isNull(stackInSlot)) continue;

				if (stackInSlot.getItem() == pickBlock.getItem() && stackInSlot.getItemDamage() == pickBlock.getItemDamage()) {
					break;
				} else if (grassBlock && stackInSlot.getItem() == Item.getItemFromBlock(Blocks.DIRT)) {
					break;
				}
			}

			if (slot >= player.inventory.getSizeInventory() || StackHelper.isNull(stackInSlot))
				break;

			player.inventory.setInventorySlotContents(origSlot, stackInSlot.copy());
			stackInSlot.onItemUse(player, world, p, EnumHand.MAIN_HAND, side, hitX, hitY, hitZ);
			if (!player.isCreative()) {
				player.inventory.setInventorySlotContents(slot, player.inventory.getStackInSlot(origSlot));
			}
			player.inventory.setInventorySlotContents(origSlot, origStack);

			player.inventory.markDirty();

			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).mcServer.getPlayerList().syncPlayerInventory(((EntityPlayerMP) player));
			}
		}

		return EnumActionResult.SUCCESS;
	}

}
