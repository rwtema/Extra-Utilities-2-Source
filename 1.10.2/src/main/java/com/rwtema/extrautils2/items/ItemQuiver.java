package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.IXUItemTexture;
import com.rwtema.extrautils2.backend.XUItemFlat;
import com.rwtema.extrautils2.backend.XUItemFlatMetadata;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemQuiver extends ItemArrow implements IXUItemTexture {
	public ItemQuiver() {
		setMaxStackSize(1);
	}

	@Override
	public boolean isInfinite(ItemStack stack, ItemStack bow, EntityPlayer player) {
		return true;
	}

	@Override
	public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
		return super.createArrow(worldIn, stack, shooter);
	}

	@Override
	public String getTexture(int i) {
		return "quiver";
	}


	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	public boolean renderAsTool() {
		return false;
	}
}
