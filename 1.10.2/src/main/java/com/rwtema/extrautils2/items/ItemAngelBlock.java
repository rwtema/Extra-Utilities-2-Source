package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.XUItemBlock;
import com.rwtema.extrautils2.compatibility.CompatHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemAngelBlock extends XUItemBlock {
	public ItemAngelBlock(Block block) {
		super(block);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer player, EnumHand hand) {
		if (worldIn.isRemote) return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);

		int x = (int) Math.floor(player.posX);
		int y = (int) Math.floor(player.posY + player.getEyeHeight());
		int z = (int) Math.floor(player.posZ);

		Vec3d look = player.getLookVec();

		EnumFacing side = EnumFacing.getFacingFromVector((float) look.x, (float) look.y, (float) look.z);

		switch (side) {
			case DOWN:
				y = (int) (Math.floor(player.getEntityBoundingBox().minY) - 1);
				break;
			case UP:
				y = (int) (Math.ceil(player.getEntityBoundingBox().maxY) + 1);
				break;
			case NORTH:
				z = (int) (Math.floor(player.getEntityBoundingBox().minZ) - 1);
				break;
			case SOUTH:
				z = (int) (Math.floor(player.getEntityBoundingBox().maxZ) + 1);
				break;
			case WEST:
				x = (int) (Math.floor(player.getEntityBoundingBox().minX) - 1);
				break;
			case EAST:
				x = (int) (Math.floor(player.getEntityBoundingBox().maxX) + 1);
				break;
		}

		BlockPos pos = new BlockPos(x, y, z);
		if (CompatHelper.canPlaceBlockHere(worldIn, block, pos, false, side, player, itemStackIn)) {
			itemStackIn.onItemUse(player, worldIn, pos, hand, side, 0, 0, 0);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}
}
