package com.rwtema.extrautils2.textures;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.compatibility.TextureAtlasSpriteCompat;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public abstract class SpriteLoader extends TextureAtlasSpriteCompat {
	public SpriteLoader(String spriteName) {
		super(spriteName);
	}

	@Override
	public final boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
		return true;
	}

	@Override
	public abstract boolean load(IResourceManager manager, ResourceLocation location);

	protected void loadSprite(BufferedImage[] images, AnimationMetadataSection meta) {
		int i = images[0].getWidth();
		int j = images[0].getHeight();
		this.width = i;
		this.height = j;
		int[][] aint = new int[images.length][];

		for (int k = 0; k < images.length; ++k) {
			BufferedImage bufferedimage = images[k];

			if (bufferedimage != null) {
				if (k > 0 && (bufferedimage.getWidth() != i >> k || bufferedimage.getHeight() != j >> k)) {
					throw new RuntimeException(String.format("Unable to load miplevel: %d, image is size: %dx%d, expected %dx%d", k, bufferedimage.getWidth(), bufferedimage.getHeight(), i >> k, j >> k));
				}

				aint[k] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
				bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[k], 0, bufferedimage.getWidth());
			}
		}

		if (meta == null) {
			if (j != i) {
				throw new RuntimeException("broken aspect ratio and not an animation");
			}

			this.framesTextureData.add(aint);
		} else {
			int j1 = j / i;
			this.height = this.width;

			if (meta.getFrameCount() > 0) {

				for (int i1 : meta.getFrameIndexSet()) {


					if (i1 >= j1) {
						throw new RuntimeException("invalid frameindex " + i1);
					}

					this.allocateFrameTextureData(i1);
					this.framesTextureData.set(i1, getFrameTextureData(aint, i, i, i1));
				}

				this.animationMetadata = meta;
			} else {
				List<AnimationFrame> list = Lists.newArrayList();

				for (int l1 = 0; l1 < j1; ++l1) {
					this.framesTextureData.add(getFrameTextureData(aint, i, i, l1));
					list.add(new AnimationFrame(l1, -1));
				}

				this.animationMetadata = new AnimationMetadataSection(list, this.width, this.height, meta.getFrameTime(), meta.isInterpolate());
			}
		}
	}

	@Override
	public void loadSprite(PngSizeInfo sizeInfo, boolean p_188538_2_) throws IOException {
		super.loadSprite(sizeInfo, p_188538_2_);
	}

	@Override
	public void loadSpriteFrames(IResource resource, int p_188539_2_) throws IOException {
		BufferedImage bufferedimage = TextureUtil.readBufferedImage(resource.getInputStream());

		int[][] aint = new int[p_188539_2_][];
		aint[0] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
		bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[0], 0, bufferedimage.getWidth());

		this.framesTextureData.add(aint);
	}
}
