package com.rwtema.extrautils2.compatibility;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCompat extends Item {
	public static void invokeGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		itemIn.getSubItems(tab, NonNullListWrapper.wrap(subItems));
	}

	public static EnumActionResult invokeOnItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return stack.getItem().onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}

	public static ActionResult<ItemStack> invokeOnItemRightClick(ItemStack itemStack, World playerIn, EntityPlayer handIn, EnumHand p_77659_4_) {
		return itemStack.getItem().onItemRightClick(playerIn, handIn, p_77659_4_);
	}

	public static EnumActionResult invokeOnItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return stack.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	public void getSubItemsBase(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn));
	}

	public EnumActionResult onItemUseBase(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.PASS;
	}

	public ActionResult<ItemStack> onItemRightClickBase(ItemStack itemStack, World playerIn, EntityPlayer handIn, EnumHand p_77659_4_) {
		return new ActionResult<>(EnumActionResult.PASS, itemStack);
	}

	public EnumActionResult onItemUseFirstBase(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return EnumActionResult.PASS;
	}

	@Override
	public final void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			getSubItemsBase(this, tab, subItems);
		}
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return onItemUseBase(player.getHeldItem(hand), player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		return onItemRightClickBase(playerIn.getHeldItem(handIn), worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return onItemUseFirstBase(player.getHeldItem(hand), player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public final void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		addInformation(stack, null, tooltip, flagIn.isAdvanced());
	}


	public void addInformation(ItemStack stack, @Nullable EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

	}
}
