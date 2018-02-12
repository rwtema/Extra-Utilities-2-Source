package com.rwtema.extrautils2.tile;

import com.rwtema.extrautils2.blocks.BlockCreativeHarvest;
import com.rwtema.extrautils2.compatibility.BlockCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.tile.tesr.ITESREnchantment;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileCreativeHarvest extends XUTile implements ITESREnchantment<TileCreativeHarvest> {


	public final NBTSerializable.NBTBlockState mimicState = registerNBT("state", new NBTSerializable.NBTBlockState());

	public final NBTSerializable.NBTBoolean pickupable = registerNBT("pickupable", new NBTSerializable.NBTBoolean(false));

	@Nullable
	@Override
	public NBTTagCompound getSaveInfo() {
		IBlockState value = mimicState.value;
		if (value == null) return null;
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		nbtTagCompound.setTag("creative_block", mimicState.serializeNBT());
		ItemStack stack = value.getBlock().getItem(world, pos, value);
		nbtTagCompound.setTag("display_stack", stack.serializeNBT());
		return nbtTagCompound;
	}

	@Override
	public void loadSaveInfo(@Nonnull NBTTagCompound tag) {
		if (tag.hasKey("creative_block", Constants.NBT.TAG_COMPOUND)) {
			mimicState.deserializeNBT(tag.getCompoundTag("creative_block"));
			if (mimicState.value == null) {
				mimicState.value = Blocks.STONE.getDefaultState();
			}
			pickupable.value = true;
		}
		super.loadSaveInfo(tag);
	}

	@Override
	public boolean shouldShowEnchantment() {
		return mimicState.value != null;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (mimicState.value == null && playerIn.capabilities.isCreativeMode) {
			if (worldIn.isRemote) return true;
			ItemStack stack = playerIn.getHeldItem(hand);
			if (StackHelper.isNonNull(stack) && stack.getItem() instanceof ItemBlock) {
				ItemBlock itemblock = (ItemBlock) stack.getItem();
				if (!(itemblock.getBlock() instanceof BlockCreativeHarvest)) {
					mimicState.value = BlockCompat.invokeGetStateForPlacement(itemblock.getBlock(), worldIn, pos, side, hitX, hitY, hitZ, itemblock.getMetadata(stack.getMetadata()), playerIn, EnumHand.MAIN_HAND, stack);
					markDirty();
					markForUpdate();
				}
			}
			return true;
		} else if (playerIn.isSneaking()  ){
			if (worldIn.isRemote) return true;

			if(!(playerIn.capabilities.isCreativeMode || pickupable.value)){
				return true;
			}

			Optional<ItemStack> pickBlock = getPickBlock(playerIn, null);
			if (!pickBlock.isPresent()) {
				return true;
			}

			Block.spawnAsEntity(worldIn, pos, pickBlock.get());
			worldIn.playEvent(2001, pos, Block.getStateId(getBlockState()));
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
		}

		return false;
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		if (mimicState.value == null) {
			packet.writeInt(-1);
		} else {
			Block block = mimicState.value.getBlock();
			packet.writeInt(Block.getIdFromBlock(block));
			packet.writeByte(block.getMetaFromState(mimicState.value));
		}
	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		int i = packet.readInt();
		mimicState.value = null;
		if (i >= 0) {
			int meta = packet.readUnsignedByte();
			Block blockById = Block.getBlockById(i);
			if (blockById != Blocks.AIR) {
				IBlockState stateFromMeta = blockById.getStateFromMeta(meta);
				if (mimicState.value != stateFromMeta) {
					mimicState.value = stateFromMeta;
					markForUpdate();
				}

			}
		}
	}
}
