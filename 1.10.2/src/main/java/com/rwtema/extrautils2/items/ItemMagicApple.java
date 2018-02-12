package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUItem;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemMagicApple extends XUItemFood {
	public ItemMagicApple() {
		super(4, 0.3F, false, "reroll_apple");
		this.setCreativeTab(ExtraUtils2.creativeTabExtraUtils);
		XUItem.items.add(this);
		setUnlocalizedName(ExtraUtils2.MODID + ":magicapple");
		setAlwaysEdible();
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 20;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, @Nonnull EntityPlayer player) {
		super.onFoodEaten(stack, worldIn, player);
		if (!worldIn.isRemote) {
			player.xpSeed = worldIn.rand.nextInt();
			player.sendMessage(Lang.chat("You feel your luck changing"));
			if (player.openContainer instanceof ContainerEnchantment) {
				player.closeScreen();
			}
		}
	}
}
