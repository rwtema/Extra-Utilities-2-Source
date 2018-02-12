package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.gui.backend.DynamicContainer;
import com.rwtema.extrautils2.gui.backend.IDynamicHandler;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class ItemIndexerRemote extends XUItemFlatMetadata implements IDynamicHandler {
	public ItemIndexerRemote() {
		super("indexer_remote");
		setMaxStackSize(1);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
//		if (stack.hasTagCompound() && Validate.notNull(stack.getTagCompound()).hasKey("BlockPos", Constants.NBT.TAG_COMPOUND)) {
//			return EnumActionResult.FAIL;
//		}

		if (worldIn.isRemote)
			return worldIn.getTileEntity(pos) instanceof TileIndexer ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;

		TileEntity entity = worldIn.getTileEntity(pos);
		if (!(entity instanceof TileIndexer)) return EnumActionResult.FAIL;
		TileIndexer indexer = (TileIndexer) entity;
		if (!indexer.isValidPlayer(playerIn)) {
			return EnumActionResult.SUCCESS;
		}

		NBTTagCompound nbt = NBTHelper.getOrInitTagCompound(stack);
		nbt.setInteger("Dimension", worldIn.provider.getDimension());
		nbt.setTag("BlockPos", NBTHelper.blockPosToNBT(pos));
		return EnumActionResult.SUCCESS;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClickBase(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote) return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
		if (!itemStackIn.hasTagCompound()) return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);

		NBTTagCompound nbt = itemStackIn.getTagCompound();

		int dimension = nbt.getInteger("Dimension");
		WorldServer world = DimensionManager.getWorld(dimension);
		if (world == null) {
			playerIn.sendMessage(Lang.chat("Unable to contact indexer"));
			return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);
		}

		BlockPos blockPos = NBTHelper.nbtToBlockPos(nbt.getCompoundTag("BlockPos"));
		if (!world.isBlockLoaded(blockPos)) {
			playerIn.sendMessage(Lang.chat("Unable to contact indexer"));
			return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);
		}

		TileEntity tile = world.getTileEntity(blockPos);
		if (!(tile instanceof TileIndexer)) {
			playerIn.sendMessage(Lang.chat("Unable to contact indexer"));
			return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);
		}

		if (((TileIndexer) tile).isValidPlayer(playerIn)) {
			playerIn.openGui(ExtraUtils2.instance, -1, world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileIndexer indexer;
		BlockPos pos = new BlockPos(x, y, z);
		if (world.isRemote) {
			indexer = new TileIndexer();
			indexer.setWorld(world);
			indexer.setPos(pos);
		} else {
			if (!world.isBlockLoaded(pos)) {
				return null;
			}

			TileEntity tile = world.getTileEntity(pos);
			if (!(tile instanceof TileIndexer)) {
				return null;
			}

			indexer = (TileIndexer) tile;

			indexer.positionsHash = 0;
			indexer.reload();
		}

		return new TileIndexer.ContainerIndexer(indexer, player, 2);

	}

}