package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class XUItemFood extends ItemFood implements IXUItem {
	final String texture;
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite sprite;

	public XUItemFood(int amount, float saturation, boolean isWolfFood, String texture) {
		super(amount, saturation, isWolfFood);
		this.texture = texture;
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	protected void onFoodEaten(ItemStack stack, World worldIn, @Nonnull EntityPlayer player) {
		super.onFoodEaten(stack, worldIn, player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		Textures.register(texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this);
	}

	@Override
	public TextureAtlasSprite getBaseTexture() {
		return sprite;
	}

	@Override
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entity) {
		model.addTintedSprite(sprite, true, -1);
	}

	@Override
	public void postTextureRegister() {
		sprite = Textures.getSprite(texture);
	}

	@Override
	public boolean renderAsTool() {
		return false;
	}

	@Override
	public void clearCaches() {
		sprite = null;
	}

	@Override
	public boolean allowOverride() {
		return true;
	}

	@Override
	public int getMaxMetadata() {
		return 0;
	}
}
