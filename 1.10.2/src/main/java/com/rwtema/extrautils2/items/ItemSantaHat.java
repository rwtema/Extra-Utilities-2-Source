package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.backend.model.PassthruModelItem;
import com.rwtema.extrautils2.backend.model.Textures;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemSantaHat extends ItemArmor implements IXUItem {
	public final static ArmorMaterial SANTA = EnumHelper.addArmorMaterial("Santaman", "extrautils2:santa", 200, new int[]{1, 1, 1, 1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1);
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite sprite;

	public ItemSantaHat() {
		super(SANTA, 0, EntityEquipmentSlot.HEAD);
	}

	@Override
	public boolean hasOverlay(@Nonnull ItemStack stack) {
		return true;
	}

	@Nullable
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return super.getArmorTexture(stack, entity, slot, type);
	}

	@Override
	public void registerTextures() {
		Textures.register("models/armor/santa_hat");
	}

	@Override
	public IBakedModel createModel(int metadata) {
		return new PassthruModelItem(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseTexture() {
		return sprite;
	}

	@Override
	public void addQuads(PassthruModelItem.ModelLayer model, ItemStack stack, World world, EntityLivingBase entity) {
		model.addSprite(sprite);
	}

	@Override
	public void postTextureRegister() {
		sprite = Textures.getSprite("models/armor/santa_hat");
	}

	@Override
	public boolean renderAsTool() {
		return false;
	}

	@Override
	public void clearCaches() {

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
