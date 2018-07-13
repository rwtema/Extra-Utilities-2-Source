package com.rwtema.extrautils2.modcompat;

import com.google.common.collect.Streams;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.fluids.TexturePlasma;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

	private List<IMaterialStats> stats = new ArrayList<>();
	private List<ITrait> allTraits = new ArrayList<>();
	private List<Pair<ITrait, String>> traits = new ArrayList<>();

	public XUTinkerMaterial(@Nonnull String name, int color, @Nullable String oreDicSuffix, @Nullable Supplier<ItemStack> representativeStack, boolean addFluid) {
		this.name = name;
		this.color = color;
		this.oreDicSuffix = oreDicSuffix;
		this.representativeStack = representativeStack;
		this.material = new Material(name, color);
		Lang.translate(String.format("material.%s.name", material.getIdentifier()), name);
		Lang.translate(String.format("material.%s.prefix", material.getIdentifier()), name);

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
			Lang.translate(fluid.getUnlocalizedName(), name);

		} else {
			fluid = null;
		}

		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				material.setRenderInfo(createRenderInfo());
			}
		});

		addStats(stats);
		addTraits();
	}

	public abstract void addTraits();

	public void addTrait(ITrait trait) {
		addTrait(trait, null);
	}

	public void addTraitToAllParts(ITrait trait) {
		allTraits.add(trait);
	}

	public void addTrait(ITrait trait, @Nullable String dependency) {
		traits.add(Pair.of(trait, dependency));
	}

	protected int[] createPalette() {
		return new int[]{ColorHelper.multShade(color, 0.8F), color};
	}


	public abstract void addStats(List<IMaterialStats> stats);

	@SideOnly(Side.CLIENT)
	public final MaterialRenderInfo createRenderInfo() {
		return new MRISupplier(name, this);
	}

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite createTexture(ResourceLocation baseTexture, String location);

	public List<IMaterialStats> getStats() {
		return stats;
	}

	public Set<ITrait> getTraits() {
		return Streams.concat(traits.stream().map(Pair::getLeft), allTraits.stream()).collect(Collectors.toSet());
	}

	public void registerTraits() {
		for (Pair<ITrait, String> trait : traits) {
			registerTrait(trait.getRight(), trait.getLeft());
		}

		for (String s : traits.stream().map(Pair::getRight).collect(Collectors.toSet())) {
			for (ITrait trait : allTraits) {
				registerTrait(s, trait);
			}
		}
	}

	private void registerTrait(String s, ITrait trait) {
		ITrait existingTrait = TinkerRegistry.getTrait(trait.getIdentifier());
		if (existingTrait != null) trait = existingTrait;

		this.material.addTrait(trait, s);
	}

}
