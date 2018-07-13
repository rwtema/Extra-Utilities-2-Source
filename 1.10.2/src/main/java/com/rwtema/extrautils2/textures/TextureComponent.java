package com.rwtema.extrautils2.textures;

import com.rwtema.extrautils2.backend.model.Box;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class TextureComponent {
	public final TextureAtlasSprite sprite;
	public final int u0, u1, v0, v1;

	public TextureComponent(TextureAtlasSprite sprite, int u0, int v0, int u1, int v1) {
		this.sprite = sprite;
		this.u0 = u0;
		this.u1 = u1;
		this.v0 = v0;
		this.v1 = v1;
	}

	public Box makeQuad(EnumFacing side) {
		return new Box(0, 0, 0, 1, 1, 1);
	}
}
