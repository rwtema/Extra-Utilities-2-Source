package com.rwtema.extrautils2.modcompat.tcon;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;

public class MRISupplier extends MaterialRenderInfo.AbstractMaterialRenderInfo {

	private XUTinkerMaterial owner;

	public MRISupplier(String suffix, XUTinkerMaterial owner) {
		this.owner = owner;

		setTextureSuffix(suffix);
	}

	@Override
	public TextureAtlasSprite getTexture(TextureAtlasSprite baseTexture, String location) {
		return owner.createTexture(baseTexture, location);
	}

}
