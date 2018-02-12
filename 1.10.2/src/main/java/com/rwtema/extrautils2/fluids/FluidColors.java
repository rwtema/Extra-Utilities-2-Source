package com.rwtema.extrautils2.fluids;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.utils.datastructures.SidedCacheLoader;
import com.rwtema.extrautils2.utils.helpers.CIELabHelper;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;

public class FluidColors {

	public static final LoadingCache<Fluid, Integer> FLUID_COLOR = CacheBuilder.newBuilder().build(new SidedCacheLoader<Fluid, Integer>() {
		@Override
		@SideOnly(Side.SERVER)
		public Integer applyServer(Fluid input) {
			return -1;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Integer applyClient(Fluid fluid) {
			TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();

			ResourceLocation fluidStill = fluid.getStill();
			TextureAtlasSprite fluidStillSprite = null;
			if (fluidStill != null) {
				fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
			}
			if (fluidStillSprite == null) {
				return -1;
			}

			if (fluidStillSprite.getFrameCount() == 0)
				return -1;

			int[][] pixels = fluidStillSprite.getFrameTextureData(0);

			int pr = 0, pg = 0, pb = 0, pn = 0;
			for (int[] pixel : pixels) {
				for (int i : pixel) {
					if (ColorHelper.getA(i) < 16)
						continue;

					float[] lab = CIELabHelper.rgb2lab(i, new float[3]);

					pn++;
					pr += lab[0];
					pg += lab[1];
					pb += lab[2];
				}
			}

			if (pn == 0) return -1;

			float[] rgb = CIELabHelper.lab2rgb(pr / pn, pg / pn, pb / pn, new float[3]);

			return ColorHelper.colorClamp(rgb[0], rgb[1], rgb[2], 1);
		}
	});


	static {
		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
					@Override
					public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
						FLUID_COLOR.invalidateAll();
					}
				});
			}
		});
	}

	public static int getColor(FluidStack stack) {
		Fluid fluid;
		if (stack == null || (fluid = stack.getFluid()) == null) return -1;
		int color = fluid.getColor(stack);
		int base_color;
		try {
			base_color = FLUID_COLOR.get(fluid);
		} catch (ExecutionException ignore) {
			FLUID_COLOR.put(fluid, -1);
			base_color = -1;
		}
		if (color == 0xffffffff) return base_color;
		float r = ColorHelper.getRF(color) * ColorHelper.getRF(base_color);
		float g = ColorHelper.getGF(color) * ColorHelper.getGF(base_color);
		float b = ColorHelper.getBF(color) * ColorHelper.getBF(base_color);
		return ColorHelper.colorClamp(r, g, b, 1);
	}
}
