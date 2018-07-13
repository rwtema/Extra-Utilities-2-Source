package com.rwtema.extrautils2.compatibility;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemCompat extends Item {
	public static void invokeGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		itemIn.getSubItems(itemIn, tab, subItems);
	}

	public static EnumActionResult invokeOnItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return stack.getItem().onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
	}

	public static ActionResult<ItemStack> invokeOnItemRightClick(ItemStack itemStack, World playerIn, EntityPlayer handIn, EnumHand p_77659_4_) {
		return itemStack.getItem().onItemRightClick(itemStack, playerIn, handIn, p_77659_4_);
	}

	public static EnumActionResult invokeOnItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return stack.getItem().onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable EntityPlayer player, List<String> tooltip, boolean flagIn) {
		super.addInformation(stack, player, tooltip, flagIn);
	}

	public void getSubItemsBase(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn));
	}

	public EnumActionResult onItemUseBase(ItemStack player, EntityPlayer worldIn, World pos, BlockPos hand, EnumHand facing, EnumFacing hitX, float hitY, float hitZ, float p_180614_9_) {
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ, p_180614_9_);
	}

	public ActionResult<ItemStack> onItemRightClickBase(ItemStack worldIn, World playerIn, EntityPlayer handIn, EnumHand p_77659_4_) {
		return super.onItemRightClick(worldIn, playerIn, handIn, p_77659_4_);
	}

	public EnumActionResult onItemUseFirstBase(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public final void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		getSubItemsBase(itemIn, tab, subItems);
	}

	@Nonnull
	@Override
	public final EnumActionResult onItemUse(ItemStack player, EntityPlayer worldIn, World pos, BlockPos hand, EnumHand facing, EnumFacing hitX, float hitY, float hitZ, float p_180614_9_) {
		return onItemUseBase(player, worldIn, pos, hand, facing, hitX, hitY, hitZ, p_180614_9_);
	}

	@Nonnull
	@Override
	public final ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		return onItemRightClickBase(stack, world, player, hand);
	}

	@Nonnull
	@Override
	public final EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return onItemUseFirstBase(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}
}
