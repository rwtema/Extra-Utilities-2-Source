package com.rwtema.extrautils2.compatibility;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public abstract class TextureAtlasSpriteCompat extends TextureAtlasSprite {

	protected TextureAtlasSpriteCompat(String spriteName) {
		super(spriteName);
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		return load(manager, location);
	}

	public abstract boolean load(IResourceManager manager, ResourceLocation location);
}
