package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.StackHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public abstract class XUItemFlatMetadata extends XUItemFlat {

	private final String[] textures;

	public XUItemFlatMetadata(String... textures) {
		this.textures = textures;
		if (textures.length > 1) {
			setHasSubtypes(true);
		}
	}

	@Override
	public void getSubItemsBase(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {

		if (textures.length > 1) {
			for (int i = 0; i < textures.length; i++) {
				subItems.add(new ItemStack(itemIn, 1, i));
			}
		} else {
			super.getSubItemsBase(itemIn, tab, subItems);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerTextures() {
		Textures.register(textures);
	}

	@SideOnly(Side.CLIENT)
	public String getTexture(@Nullable ItemStack itemStack, int renderPass) {
		int damage = StackHelper.isNonNull(itemStack) ? itemStack.getItemDamage() : 0;
		if (damage < 0) damage = 0;
		if (damage >= textures.length) damage = textures.length - 1;
		return textures[damage];
	}

	@Override
	public int getMaxMetadata() {
		return textures.length - 1;
	}

	@Override
	public boolean allowOverride() {
		return true;
	}

	@Override
	public String getModelSubName(int metadata) {
		if (metadata < 0)
			metadata = 0;
		if (metadata >= textures.length)
			metadata = textures.length - 1;
		return textures[metadata];
	}
}
