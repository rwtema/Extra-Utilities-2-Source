package com.rwtema.extrautils2.modcompat.tcon;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.fluids.TexturePlasma;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class XUTinkerMaterial {
	@Nonnull
	public final Material material;
	@Nullable
	public final Fluid fluid;
	@Nonnull
	public final String name;
	public final int color;
	@Nullable
	public final String oreDicSuffix;
	@Nullable
	public final Supplier<ItemStack> representativeStack;

	List<IMaterialStats> stats = new ArrayList<>();


	public XUTinkerMaterial(@Nonnull String name, int color, @Nullable String oreDicSuffix, @Nullable Supplier<ItemStack> representativeStack, boolean addFluid) {
		this.name = name;
		this.color = color;
		this.oreDicSuffix = oreDicSuffix;
		this.representativeStack = representativeStack;
		this.material = new Material(name, color);
		if (addFluid) {
			ResourceLocation still = new ResourceLocation(ExtraUtils2.MODID, name);
			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					Textures.registerSupplier(name, new Supplier<TextureAtlasSprite>() {
						@Override
						public TextureAtlasSprite get() {
							int[] ints = createPalette();
							return new TexturePlasma(still.toString(), new ResourceLocation(ExtraUtils2.MODID, "molten_fluid_base"), ints);
						}
					});
				}
			});

			fluid = new Fluid(name, still, still);
			fluid.setTemperature(1000);

		} else {
			fluid = null;
		}
	}

	public abstract void addTraits() ;

	public void addTrait(ITrait trait) {
		addTrait(trait, null);
	}

	public void addTrait(ITrait trait, String dependency) {
		this.material.addTrait(trait, dependency);
	}

	protected int[] createPalette() {
		return new int[]{ColorHelper.multShade(color, 0.8F), color};
	}


	public abstract void addStats(List<IMaterialStats> stats);

	@SideOnly(Side.CLIENT)
	public abstract MaterialRenderInfo createRenderInfo();

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite createTexture(TextureAtlasSprite baseTexture, String location);
}
