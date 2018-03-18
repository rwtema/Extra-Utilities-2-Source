package com.rwtema.extrautils2.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public abstract class XU2StackCreator {
	@Nullable
	public static XU2StackCreator INSTANCE;

	// Golden Lasso
	public abstract ItemStack getGoldenLassoCraftingStack(Class<?  extends EntityLivingBase> entity);

	public abstract ItemStack getGoldenLassoCraftingStackVillagerRequiresContract();

	// Creative Items
	public abstract ItemStack getConfiguredCreativeChest(ItemStack contents, boolean allowNonCreativeToPlaceAndBreak, boolean allowNonCreativeToConfigure);

	public abstract ItemStack getConfiguredCreativeHarvestBlock(ItemStack contents, boolean allowNonCreativeToPlaceAndBreak);

	public abstract ItemStack getConfiguredCreativeDrum(FluidStack contents);

	public abstract ItemStack getConfiguredCreativeEnergyBlock(boolean allowNonCreativeToPlaceAndBreak);


}
