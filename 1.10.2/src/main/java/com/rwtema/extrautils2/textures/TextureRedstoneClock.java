package com.rwtema.extrautils2.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;

public class TextureRedstoneClock extends TextureAtlasSprite {
	public TextureRedstoneClock(String spriteName) {
		super(spriteName);
	}

	@Override
	public void updateAnimation() {
		++this.tickCounter;

		if (this.tickCounter >= 2) {
			this.tickCounter = 0;

			WorldClient theWorld = Minecraft.getMinecraft().world;
			int size = this.framesTextureData.size();
			int t = theWorld != null ? (((int) (theWorld.getTotalWorldTime() % (20 * size)) * size) / 20) % size : 0;

			int k = frameCounter;

			if (t == frameCounter) return;

			int numSteps = t - frameCounter;
			if (numSteps < 0) numSteps += 20;

			if (numSteps <= 2) k += numSteps;
			else if (numSteps > (size * 3) / 4)
				return;
			else k += 4;

			k = k % size;

			if (this.frameCounter != k) {
				TextureUtil.uploadTextureMipmap(this.framesTextureData.get(k), this.width, this.height, this.originX, this.originY, false, false);
				this.frameCounter = k;
			}
		}
	}
}
