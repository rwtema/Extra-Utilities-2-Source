package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public abstract class NullModel extends SimpleBakedModel {
	protected static final List<BakedQuad> EMPTY_QUADS = ImmutableList.of();
	protected static final boolean EMPTY_AMBIENTOCCLUSION = false;
	protected static final boolean EMPTY_GUI3D = false;
	protected static final TextureAtlasSprite EMPTY_TEXTURE = null;
	protected static final ItemCameraTransforms EMPTY_CAMERATRANSFORMS = ItemCameraTransforms.DEFAULT;
	protected static final Map<EnumFacing, List<BakedQuad>> EMPTY_FACE_QUADS;

	static {
		EMPTY_FACE_QUADS = ImmutableMap.<EnumFacing, List<BakedQuad>>builder()
				.put(EnumFacing.DOWN, ImmutableList.of())
				.put(EnumFacing.UP, ImmutableList.of())
				.put(EnumFacing.EAST, ImmutableList.of())
				.put(EnumFacing.WEST, ImmutableList.of())
				.put(EnumFacing.NORTH, ImmutableList.of())
				.put(EnumFacing.SOUTH, ImmutableList.of())
				.build();
	}

	public NullModel() {
		super(EMPTY_QUADS, EMPTY_FACE_QUADS, EMPTY_AMBIENTOCCLUSION, EMPTY_GUI3D, Textures.MISSING_SPRITE, EMPTY_CAMERATRANSFORMS, new ItemOverrideList(ImmutableList.of()));
	}

	@Nonnull
	@Override
	public abstract TextureAtlasSprite getParticleTexture();


}
