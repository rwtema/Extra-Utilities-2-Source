package com.rwtema.extrautils2.items;

import com.google.common.collect.ImmutableSet;
import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class ItemTrowel extends XUItemFlatMetadata {
	public ItemTrowel() {
		super("trowel");
		setMaxStackSize(1);
		setMaxDamage(250);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState blockIn, BlockPos pos, EntityLivingBase entityLiving) {
		if (blockIn.getBlockHardness(worldIn, pos) != 0)
			stack.damageItem(1, entityLiving);

		return true;
	}


	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		NBTTagList tagList = stack.getEnchantmentTagList();
		if (tagList != null) {
			for (int i = 0; i < tagList.tagCount(); i++) {
				if (tagList.getCompoundTagAt(i).getShort("id") == Enchantment.getEnchantmentID(Enchantments.SILK_TOUCH)) {
					tagList.removeTag(i);
					break;
				}
			}
		}
	}


	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		IBlockState blockState = player.world.getBlockState(pos);
		if (!player.capabilities.isCreativeMode && validMaterials.contains(blockState.getMaterial())) {
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0)
				itemstack.addEnchantment(Enchantments.SILK_TOUCH, 1);
		} else {
			NBTTagList tagList = itemstack.getEnchantmentTagList();
			if (tagList != null) {
				for (int i = 0; i < tagList.tagCount(); i++) {
					if (tagList.getCompoundTagAt(i).getShort("id") == Enchantment.getEnchantmentID(Enchantments.SILK_TOUCH)) {
						tagList.removeTag(i);
						break;
					}
				}
			}
		}
		return super.onBlockStartBreak(itemstack, pos, player);
	}

	@Override
	public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack) {
		return validMaterials.contains(state.getMaterial());
	}

	final Set<Material> validMaterials = ImmutableSet.of(Material.GRASS, Material.GROUND, Material.SAND, Material.SNOW, Material.CLAY);

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		return validMaterials.contains(state.getMaterial()) ? 4.0F : super.getStrVsBlock(stack, state);
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return net.minecraftforge.oredict.OreDictionary.itemMatches(new ItemStack(Items.IRON_INGOT), repair, false) || super.getIsRepairable(toRepair, repair);
	}

	@Override
	public boolean renderAsTool() {
		return true;
	}


	@Override
	public boolean hasEffect(ItemStack stack) {
		return false;
	}


}
