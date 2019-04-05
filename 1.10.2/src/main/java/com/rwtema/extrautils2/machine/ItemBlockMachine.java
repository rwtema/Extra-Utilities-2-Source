package com.rwtema.extrautils2.machine;

import com.rwtema.extrautils2.api.machine.Machine;
import com.rwtema.extrautils2.api.machine.MachineRegistry;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUItemBlock;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBlockMachine extends XUItemBlock {
	BlockMachine machine;

	public ItemBlockMachine(Block block) {
		super(block);
		machine = (BlockMachine) block;
		this.setHasSubtypes(true);
	}

	@Nullable
	public static Machine getMachineType(@Nonnull ItemStack stack) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		return tagCompound != null ? MachineRegistry.getMachine(tagCompound.getString("Type")) : null;
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack) {
		Machine type = getMachineType(stack);
		return BlockMachine.getDisplayName(type);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemStack stack, @Nonnull EntityPlayer playerIn, World worldIn, @Nonnull BlockPos pos, EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!stack.hasTagCompound()) return EnumActionResult.FAIL;

		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block1 = iblockstate.getBlock();

		if (!block1.isReplaceable(worldIn, pos)) {
			pos = pos.offset(facing);
		}

		if (StackHelper.getStacksize(stack) != 0 && playerIn.canPlayerEdit(pos, facing, stack) && CompatHelper.canPlaceBlockHere(worldIn, this.block, pos, false, facing, null, stack)) {
			int i = this.getMetadata(stack.getMetadata());

			Machine machine = getMachineType(stack);
			if (machine == null) return EnumActionResult.FAIL;

			Machine.EnergyMode mode = machine.energyMode;

			IBlockState iblockstate1 = this.machine.xuBlockState.getStateFromDropMeta(i)
					.withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, playerIn.getHorizontalFacing())
					.withProperty(BlockMachine.ACTIVE, false)
					.withProperty(BlockMachine.TYPE, mode);

			if (this.myPlaceBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
				SoundType soundtype = this.block.getSoundType();
				worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				StackHelper.decrease(stack);
			}

			return EnumActionResult.SUCCESS;
		} else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
		Machine machine = getMachineType(stack);
		if (machine == null) return false;
		
		if (side == null || side == EnumFacing.UP || side == EnumFacing.DOWN) side = EnumFacing.NORTH;			

		Machine.EnergyMode mode = machine.energyMode;

		IBlockState iblockstate1 = this.machine.xuBlockState.defaultState
				.withProperty(XUBlockStateCreator.ROTATION_HORIZONTAL, side)
				.withProperty(BlockMachine.ACTIVE, false)
				.withProperty(BlockMachine.TYPE, mode);
		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1);
	}

	public boolean myPlaceBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!world.setBlockState(pos, newState, 3)) return false;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this.block) {
			setTileEntityNBT(world, player, pos, stack);
			this.block.onBlockPlacedBy(world, pos, state, player, stack);
		}

		return true;
	}
}
