package com.rwtema.extrautils2.modcompat;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.blocks.BlockDecorativeSolidWood;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.utils.datastructures.IntPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.List;
import java.util.Map;

import static slimeknights.tconstruct.library.materials.MaterialTypes.HEAD;
import static slimeknights.tconstruct.library.utils.HarvestLevels.COBALT;
import static slimeknights.tconstruct.library.utils.HarvestLevels.STONE;

public class TinkersIntegration {
	static List<XUTinkerMaterial> xuTinkerMaterials;

	public static void createObjects() {
		xuTinkerMaterials = ImmutableList.of(
				new XUTinkerMaterial("xu_magical_wood", 0XFFFDFFA8, "blockMagicalWood", BlockDecorativeSolidWood.DecorStates.magical_wood, false) {

					@Override
					public void addTraits() {
						addTrait(new XUTrait.TraitMagicalModifiers(), null);
						addTraitToAllParts(new XUTrait.TraitBrittle());
					}

					@Override
					public void addStats(List<IMaterialStats> stats) {
						stats.add(new HeadMaterialStats(35, 2.00f, 2.00f, STONE));
						stats.add(new HandleMaterialStats(1.00f, 25));
						stats.add(new ExtraMaterialStats(15));
						stats.add(new BowMaterialStats(1f, 1f, 0f));
					}

					@Override
					@SideOnly(Side.CLIENT)
					public TextureAtlasSprite createTexture(ResourceLocation baseTexture, String location) {
						return new XUTConTextureMagicWood(baseTexture, location);
//						return new XUTConTextureBase(baseTexture, location) {
//							@Override
//							protected void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride) {
//								int[] edgeDist = getEdgeDist(datum);
//
//								int mean = mean(datum);
//								int gold = 0xFFF9ED4F;
//								int gold_highlight = 0xFFFFFF8B;
//
//								int darkwood = 0xFF362A15;
//
//								boolean[] baseEdge = filter(edgeDist, 1);
//
//								boolean[] baseCorners = and(expand(getCorners(filterUp(edgeDist, 1))), baseEdge);
//								boolean[] baseCornersShift = and(or(shift(baseCorners, 0, -1), shift(baseCorners, -1, 0), shift(baseCorners, -1, -1)), baseEdge);
//
//								boolean[] subEdge = filter(edgeDist, 4);
//
//								boolean[] subInterior = filter(edgeDist, 5);
//
//
//								boolean[] subCorners = and(expand(getCorners(filterUp(edgeDist, 4))), subEdge);
//								boolean[] subCornersShift = and(or(shift(subCorners, -1, 0), shift(subCorners, 0, -1), shift(subCorners, -1, -1)), subCorners);
//
//								addColorPalette(0xFFBC9862, 0xFFBC9862);
//
//								addOverrides(colorOverride, subInterior, darkwood);
//
//								addOverrides(colorOverride, filter(edgeDist, 2), darkwood);
////								addOverrides(colorOverride, baseCornersShift, gold);
////								addOverrides(colorOverride, baseCorners, gold_highlight);
//								addOverrides(colorOverride, baseCornersShift, gold);
//								addOverrides(colorOverride, baseCorners, gold_highlight);
//
//								addOverrides(colorOverride, subEdge, 0xFF362A15);
////								addOverrides(colorOverride, subCornersShift, gold);
////								addOverrides(colorOverride, subCorners, gold_highlight);
//								addOverrides(colorOverride, subCornersShift, gold);
//								addOverrides(colorOverride, subCorners, gold_highlight);
//							}
//
//							public void addHighlights(Map<IntPair, Integer> valueOverride, boolean[] mask, int color, int[] datum, int min, int add) {
//								for (int x = 0; x < width; x++) {
//									for (int y = 0; y < height; y++) {
//										int coord = coord(x, y);
//										if (mask[coord]) {
//											int brightness = Math.max(getBrightness(datum[coord]), min) + add;
//											int col = multPixel(color, brightness);
//											valueOverride.put(IntPair.of(x, y), col);
//										}
//									}
//								}
//							}
//						};
					}
				},
				new XUTinkerMaterial("xu_evil_metal", 0XFFFDFFA8, "EvilMetal", ItemIngredients.Type.EVIL_INFUSED_INGOT, true) {
					@Override
					public void addTraits() {
						addTrait(new XUTrait.TraitWithering(), HEAD);
					}

					@Override
					public void addStats(List<IMaterialStats> stats) {
						stats.add(new HeadMaterialStats(666, 13, 13, COBALT));
						stats.add(new HandleMaterialStats(1, 0));
						stats.add(new ExtraMaterialStats(66));
					}

					@Override
					@SideOnly(Side.CLIENT)
					public TextureAtlasSprite createTexture(ResourceLocation baseTexture, String location) {
						return new XUTConTextureBase(baseTexture, location) {
							@Override
							protected void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride) {
								int[] edgeDist = getEdgeDist(datum);
//							addOverrides(valueOverride, and(expand(filter(edgeDist, 1)),filter(edgeDist, 0)) , 0x05EFFA85);
								addOverrides(colorOverride, filter(edgeDist, 2), 0xFF000000);
								addOverrides(valueOverride, and(invert(filter(edgeDist, 0)), invert(or(filter(edgeDist, 2), filter(edgeDist, 1)))), 0xFF000000);
								addColorPalette(0XFFE0E277, 0xFFFEFFED);
							}
						};
					}

					@Override
					protected int[] createPalette() {
						return new int[]{
								0xFFFF7A00,
								0xFFFDFFA8,
								0xFFFEFFED
						};
					}
				},
				new XUTinkerMaterial("xu_enchanted_metal", 0XFFBFF260, "EnchantedMetal", ItemIngredients.Type.ENCHANTED_INGOT, true) {
					@Override
					public void addTraits() {
						addTrait(new XUTrait.TraitExperience());
					}

					@Override
					public void addStats(List<IMaterialStats> stats) {
						stats.add(new HeadMaterialStats(350, 7, 3.5F, HarvestLevels.IRON));
						stats.add(new HandleMaterialStats(1.1F, 20));
						stats.add(new ExtraMaterialStats(80));
					}

					@Override
					@SideOnly(Side.CLIENT)
					public TextureAtlasSprite createTexture(ResourceLocation baseTexture, String location) {
						return new XUTConTextureBase(baseTexture, location) {
							@Override
							protected void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride) {
								int[] edgeDist = getEdgeDist(datum);
//							addOverrides(valueOverride, and(expand(filter(edgeDist, 1)),filter(edgeDist, 0)) , 0x05EFFA85);
								addOverrides(colorOverride, filter(edgeDist, 1), 0xFF382802);
								addOverrides(colorOverride, filter(edgeDist, 2), 0xFFB2E45A);
								addOverrides(colorOverride, filter(edgeDist, 3), 0xFF1A4C26);
								addOverrides(colorOverride, filter(edgeDist, 4), 0xFFA8E86D);
								addColorPalette(0xff1A4D26, 0xff67BA5E);
							}
						};
					}

					@Override
					protected int[] createPalette() {
						return new int[]{
								0xFF1A4C26,
								0xFFD8E45A,
								0xFF68B042,
								0xFFC9FF91,
								0xFFB2F767,
								0xffF5FF8F,
						};
					}
				},

				new XUTinkerMaterial("xu_demonic_metal", 0xFFA33B00, "DemonicMetal", ItemIngredients.Type.DEMON_INGOT, true) {
					@Override
					public void addStats(List<IMaterialStats> stats) {
						stats.add(new HeadMaterialStats(80, 0.5F, 11.1F, HarvestLevels.IRON));
						stats.add(new HandleMaterialStats(0.25F, 100));
						stats.add(new ExtraMaterialStats(20));
						stats.add(new BowMaterialStats(0.7f, 1.1f, 4f));
					}

					@Override
					@SideOnly(Side.CLIENT)
					public TextureAtlasSprite createTexture(ResourceLocation baseTexture, String location) {
						return new XUTConTextureBase(baseTexture, location) {
							@Override
							protected void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride) {
								int[] edgeDist = getEdgeDist(datum);
								boolean[] edges = filter(edgeDist, 1);
//							addOverrides(colorOverride, and(expand(edges),filter(edgeDist, 2)), 0xFF420B00);
								addOverrides(colorOverride, edges, 0x550B00);
//							boolean[] corners = and(expand(getCorners(invert(filter(datum, 0)))), edges);
//							addOverrides(colorOverride, corners, 0xFFFFdA70);
								addColorPalette(
										0xFFFFAA00,
										0xFF930400,
										0xFFC20700,
										0xFFD80700,
										0xFFF00B00
//									0xFFFF0B00
								);
							}
						};
					}

					@Override
					public void addTraits() {
						addTrait(new XUTrait.TraitChatty(), MaterialTypes.HEAD);
					}

					@Override
					protected int[] createPalette() {
						return new int[]{
								0xFF420B00,
								0xFF930400,
								0xFFC20700,
								0xFFFF7A00,
								0xFFD80700,
								0xFFF00B00,
								0xFFFF0B00,
						};
					}
				}
		);
	}

	public static void doRegister() {
		boolean useFluids = TConstruct.pulseManager.isPulseLoaded(TinkerSmeltery.PulseId);

		for (XUTinkerMaterial mat : xuTinkerMaterials) {
			if (useFluids && mat.fluid != null) {
				FluidRegistry.registerFluid(mat.fluid);

				if (!FluidRegistry.getBucketFluids().contains(mat.fluid)) {
					FluidRegistry.addBucketForFluid(mat.fluid);
				}
			}

			if (mat.representativeStack != null) {
				ItemStack stack = mat.representativeStack.get();
				if (StackHelper.isNonNull(stack)) {
					mat.material.setRepresentativeItem(stack.copy());
				}
			}

			TinkerRegistry.addMaterial(mat.material);
			mat.registerTraits();

			for (IMaterialStats stat : mat.getStats()) {
				TinkerRegistry.addMaterialStats(mat.material, stat);
			}

			if (useFluids && mat.fluid != null) {
				mat.material.setFluid(mat.fluid);
				mat.material.setCastable(true);
			} else {
				mat.material.setCraftable(true);
			}

		}

	}

	public static void init() {
		for (XUTinkerMaterial mat : xuTinkerMaterials) {

			if (mat.fluid != null && mat.oreDicSuffix != null) {
				TinkerSmeltery.registerOredictMeltingCasting(mat.fluid, mat.oreDicSuffix);
			} else if (mat.oreDicSuffix != null) {
				mat.material.addItem(mat.oreDicSuffix, 1, Material.VALUE_Ingot);
			}

			// where you pour gold on a part to make a cast
			TinkerSmeltery.registerToolpartMeltingCasting(mat.material);
		}
	}

}
