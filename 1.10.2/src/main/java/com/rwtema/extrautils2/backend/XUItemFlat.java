package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class XUItemFlat extends XUItem {
	public XUItemFlat() {
		super();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public abstract void registerTextures();

	@SideOnly(Side.CLIENT)
	public abstract String getTexture(@Nullable ItemStack itemStack, int renderPass);

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getSprite(@Nullable ItemStack itemStack, int renderPass) {
		String texture = getTexture(itemStack, renderPass);
		TextureAtlasSprite sprite = Textures.sprites.get(texture);
		if (sprite == null) sprite = Textures.MISSING_SPRITE;
		return sprite;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderLayers(@Nullable ItemStack itemStack) {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return getSprite(null, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack) {
		for (int i = 0; i < getRenderLayers(stack); i++) {
			model.addTintedSprite(getSprite(stack, i), renderLayerIn3D(stack, i), getTint(stack, i));
		}
	}

	@SideOnly(Side.CLIENT)
	public int getTint(ItemStack stack, int i) {
		return -1;
	}

	public boolean renderLayerIn3D(ItemStack stack, int renderPass) {
		return true;
	}
}
