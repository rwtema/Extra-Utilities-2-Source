package com.rwtema.extrautils2.modcompat.tcon;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import com.rwtema.extrautils2.backend.entries.VoidEntry;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.modcompat.ModCompatibility;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.XURandom;
import com.rwtema.extrautils2.utils.datastructures.IntPair;
import com.rwtema.extrautils2.utils.helpers.CollectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.List;
import java.util.Map;

@ModCompatibility(mod = TConstruct.modID)
public class TConstructIntegration extends VoidEntry {
	List<XUTinkerMaterial> xuTinkerMaterials = ImmutableList.of(
//			new XUTinkerMaterial("xu_enchanted_metal", 0XFF1A4C26, "EnchantedMetal", null, true) {
//
//
//				@Override
//				public void addTraits() {
//
//				}
//
//				@Override
//				public void addStats(List<IMaterialStats> stats) {
//					stats.add(new HeadMaterialStats(100, 3, 10, 7));
//					stats.add(new HandleMaterialStats(1.1F, 100));
//					stats.add(new ExtraMaterialStats(100));
//				}
//
//				@Override
//				public MaterialRenderInfo createRenderInfo() {
//					return new MRISupplier("xu_enchanted", (sprite, s) -> new XUTConTextureBase(sprite, s) {
//						@Override
//						protected void preProcess(int[] datum, Map<IntPair, Integer> valueOverride, Map<IntPair, Integer> colorOverride) {
//							int[] edgeDist = getEdgeDist(datum);
////							addOverrides(valueOverride, and(expand(filter(edgeDist, 1)),filter(edgeDist, 0)) , 0x05EFFA85);
//							addOverrides(colorOverride, filter(edgeDist, 1), 0xFF382802);
//							addOverrides(colorOverride, filter(edgeDist, 2), 0xFFB2E45A);
//							addOverrides(colorOverride, filter(edgeDist, 3), 0xFF1A4C26);
//							addOverrides(colorOverride, filter(edgeDist, 4), 0xFFA8E86D);
//							addColorPalette(0xff1A4D26, 0xff67BA5E);
//						}
//					});
//				}
//			},

			new XUTinkerMaterial("xu_demonic_metal", 0xFF420B00, "DemonicMetal", null, true) {


				@Override
				public void addStats(List<IMaterialStats> stats) {
					stats.add(new HeadMaterialStats(80, 0.5F, 8.25F, HarvestLevels.IRON));
					stats.add(new HandleMaterialStats(0.25F, 100));
					stats.add(new ExtraMaterialStats(20));
					stats.add(new BowMaterialStats(0.7f, 1.1f, 4f));
				}

				@Override
				@SideOnly(Side.CLIENT)
				public MaterialRenderInfo createRenderInfo() {
					return new MRISupplier("xu_demonic", this);
				}

				@Override
				@SideOnly(Side.CLIENT)
				public TextureAtlasSprite createTexture(TextureAtlasSprite baseTexture, String location) {
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
					addTrait(new TraitChatty(), MaterialTypes.HEAD);
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

	public TConstructIntegration() {
		super("tinkers_construct_integration");
	}

	@Override
	public void init() {
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
					mat.material.setRepresentativeItem(stack);
				}
			}

			TinkerRegistry.addMaterial(mat.material);
			if (useFluids && mat.fluid != null) {
				mat.material.setFluid(mat.fluid);
				mat.material.setCastable(true);
			} else {
				mat.material.setCraftable(true);
			}

			mat.addTraits();
			List<IMaterialStats> stats = mat.stats;
			mat.addStats(stats);
			for (IMaterialStats stat : stats) {
				TinkerRegistry.addMaterialStats(mat.material, stat);
			}

			ExtraUtils2.proxy.run(new ClientRunnable() {
				@Override
				@SideOnly(Side.CLIENT)
				public void run() {
					mat.material.setRenderInfo(mat.createRenderInfo());
				}
			});

			if (mat.fluid != null && mat.oreDicSuffix != null) {
				TinkerSmeltery.registerOredictMeltingCasting(mat.fluid, mat.oreDicSuffix);
			}
			TinkerSmeltery.registerToolpartMeltingCasting(mat.material);
		}

	}

	private static class TraitChatty extends AbstractTrait {


		private final int TIME_BETWEEN_MESSAGES = 2 * 60 * 20;
		int timeSinceLastMessage = XURandom.rand.nextInt(TIME_BETWEEN_MESSAGES);

		public TraitChatty() {
			super("xu_whispering", TextFormatting.DARK_RED);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
			if (!isSelected || !world.isRemote) return;
			if (entity instanceof EntityPlayer) {
				if (Minecraft.getMinecraft().player != entity) return;

				EntityPlayer player = (EntityPlayer) entity;

				timeSinceLastMessage--;

				if (timeSinceLastMessage < 0) {
					RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
					if (objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && objectMouseOver.entityHit instanceof EntityPlayer) {
						sendMessage(tool, player, Lang.chat("Kill!"));
					} else {

						sendMessage(tool, player, CollectionHelper.getRandomElementMulti(
								Lang.chat("Time for death and destruction?"),
								Lang.chat("I'm hungry, feed me."),
								Lang.chat("Hey you, let's go kill everything!"),
								Lang.chat("Murder! Death! Kill!"),
								Lang.chat("Hack n' slash! Hack n' slash! Hack n' slash! Hack n' slash! Hack n' slash!"),
								Lang.chat("Feast on their blood."),
								Lang.chat("I feel... sharp."),
								Lang.chat("I'm ready and willing."),
								Lang.chat("Stabby stabby stab!."),
								Lang.chat("Let the essence of life and death flow freely."),
								Lang.chat("This world is filled with such life and beauty. Let's go destroy it all.")
						));


					}
				}

			}
		}

		private void sendMessage(ItemStack tool, EntityPlayer player, ITextComponent message) {
			TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.message.display.incoming", tool.getDisplayName(), message);
			textcomponenttranslation.getStyle().setColor(TextFormatting.GRAY).setItalic(Boolean.TRUE);
			player.sendMessage(textcomponenttranslation);


			timeSinceLastMessage = TIME_BETWEEN_MESSAGES << 2 + player.world.rand.nextInt(TIME_BETWEEN_MESSAGES << 2);
		}

	}
}
