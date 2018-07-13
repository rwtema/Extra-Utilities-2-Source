package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.XUItemBlock;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemBlockPipe extends XUItemBlock {
	public ItemBlockPipe(Block block) {
		super(block);
	}


	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemStack stack, @Nonnull EntityPlayer playerIn, World worldIn, @Nonnull BlockPos pos, EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (StackHelper.isNull(stack) || StackHelper.isEmpty(stack))
			return EnumActionResult.FAIL;

		if (!playerIn.canPlayerEdit(pos, facing, stack)) return EnumActionResult.FAIL;

		if (worldIn.isRemote) return EnumActionResult.SUCCESS;

		if (!BlockTransferHolder.placePipe(worldIn, pos, playerIn) && !BlockTransferHolder.placePipe(worldIn, pos.offset(facing), playerIn)) {
			return EnumActionResult.FAIL;
		}

		SoundType soundtype = Blocks.STONE.getSoundType();
		worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
		StackHelper.decrease(stack);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
		return BlockTransferHolder.placePipe(world, pos, player);
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side, EntityPlayer player, @Nonnull ItemStack stack) {
		return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack) || checkPos(worldIn, pos) || checkPos(worldIn, pos.offset(side));

	}

	public boolean checkPos(World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileTransferHolder && ((TileTransferHolder) tileEntity).centerPipe == null;
	}
}
