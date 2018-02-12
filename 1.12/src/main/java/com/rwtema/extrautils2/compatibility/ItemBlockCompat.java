package com.rwtema.extrautils2.compatibility;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockCompat extends ItemBlock {
	public ItemBlockCompat(Block block) {
		super(block);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		EnumActionResult enumActionResult = onItemUse(player.getHeldItem(hand), player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		if (enumActionResult != EnumActionResult.PASS) return enumActionResult;
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	public EnumActionResult onItemUse(ItemStack stack, @Nonnull EntityPlayer playerIn, World worldIn, @Nonnull BlockPos pos, EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.PASS;
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ActionResult<ItemStack> result = onItemRightClick(playerIn.getHeldItem(handIn), worldIn, playerIn, handIn);
		if (result != null) {
			return result;
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer player, EnumHand hand) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		addInformation(stack, Minecraft.getMinecraft().player, tooltip, flagIn.isAdvanced());
	}

	public void addInformation(ItemStack stack, @Nullable EntityPlayer player, List<String> tooltip, boolean flagIn) {

	}
}
