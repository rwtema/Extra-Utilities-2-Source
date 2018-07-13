package com.rwtema.extrautils2.backend.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BoxQuadListDeferred extends Box {
	private final Function<EnumFacing, List<BakedQuad>> quadCreator;
	private Supplier<TextureAtlasSprite> textureSupplier;

	public BoxQuadListDeferred(float x0, float y0, float z0, float x1, float y1, float z1, Supplier<TextureAtlasSprite> textureSupplier, Function<EnumFacing, List<BakedQuad>> quadCreator) {
		super(x0, y0, z0, x1, y1, z1);
		this.textureSupplier = textureSupplier;
		this.quadCreator = quadCreator;
	}

	@Override
	public TextureAtlasSprite getTex() {
		return textureSupplier.get();
	}

	@Override
	public List<BakedQuad> makeQuads(@Nullable EnumFacing side) {
		return quadCreator.apply(side);
	}
}
