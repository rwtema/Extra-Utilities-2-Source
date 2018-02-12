package com.rwtema.extrautils2.potion;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class XUPotion extends Potion {
	public final String xuName;
	public final String texture;

	public XUPotion(String xuName, boolean isBadEffectIn, int liquidColorIn) {
		super(isBadEffectIn, liquidColorIn);
		this.xuName = xuName;
		String baseName = Lang.stripText(xuName);
		setPotionName("effect.xu2." + baseName);
		texture = "potions/" + baseName;
		ExtraUtils2.proxy.registerTexture(texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		TextureAtlasSprite sprite = Textures.getSprite(texture);
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if (screen != null) {
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			screen.drawTexturedModalRect(x + 7, y + 7, sprite, 16, 16);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
		TextureAtlasSprite sprite = Textures.getSprite(texture);
		Gui screen = Minecraft.getMinecraft().ingameGUI;
		if (screen != null) {
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			screen.drawTexturedModalRect(x+4 , y +4, sprite, 16, 16);
		}
	}
}
