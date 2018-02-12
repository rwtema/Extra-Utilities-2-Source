package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import com.rwtema.extrautils2.utils.helpers.NullHelper;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class PassthruModelItem extends NullModel {
	public static WeakHashMap<TextureAtlasSprite, ImmutableList<BakedQuad>> quads2dcache = new WeakHashMap<TextureAtlasSprite, ImmutableList<BakedQuad>>() {
		@Override
		public ImmutableList<BakedQuad> get(Object key) {
			ImmutableList<BakedQuad> bakedQuads = super.get(key);
			if (bakedQuads == null) {
				TextureAtlasSprite sprite = (TextureAtlasSprite) key;

				if (sprite == null) {
					sprite = Textures.MISSING_SPRITE;
				}

				ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
				// front
				builder.add(QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), EnumFacing.SOUTH, -1,
						0, 0, 0.5f, sprite.getMinU(), sprite.getMaxV(),
						0, 1, 0.5f, sprite.getMinU(), sprite.getMinV(),
						1, 1, 0.5f, sprite.getMaxU(), sprite.getMinV(),
						1, 0, 0.5f, sprite.getMaxU(), sprite.getMaxV(), sprite
				));
				// back
				builder.add(QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), EnumFacing.NORTH, -1,
						0, 0, 0.5f, sprite.getMinU(), sprite.getMaxV(),
						1, 0, 0.5f, sprite.getMaxU(), sprite.getMaxV(),
						1, 1, 0.5f, sprite.getMaxU(), sprite.getMinV(),
						0, 1, 0.5f, sprite.getMinU(), sprite.getMinV(), sprite
				));
				bakedQuads = builder.build();

				put(sprite, bakedQuads);
			}
			return bakedQuads;
		}
	};

	public static WeakHashMap<TextureAtlasSprite, ImmutableList<BakedQuad>> pseudoQuads2dCache = new WeakHashMap<TextureAtlasSprite, ImmutableList<BakedQuad>>() {
		@Override
		public ImmutableList<BakedQuad> get(Object key) {
			ImmutableList<BakedQuad> bakedQuads = super.get(key);
			if (bakedQuads == null) {
				TextureAtlasSprite sprite = (TextureAtlasSprite) key;
				if (sprite == null) {
					sprite = Textures.MISSING_SPRITE;
				}

				ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
				// front
				builder.add(QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), EnumFacing.SOUTH, -1,
						0, 0, 7.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
						0, 1, 7.5f / 16f, sprite.getMinU(), sprite.getMinV(),
						1, 1, 7.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
						1, 0, 7.5f / 16f, sprite.getMaxU(), sprite.getMaxV(), sprite
				));
				// back
				builder.add(QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), EnumFacing.NORTH, -1,
						0, 0, 8.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
						1, 0, 8.5f / 16f, sprite.getMaxU(), sprite.getMaxV(),
						1, 1, 8.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
						0, 1, 8.5f / 16f, sprite.getMinU(), sprite.getMinV(), sprite
				));
				bakedQuads = builder.build();

				put(sprite, bakedQuads);
			}
			return bakedQuads;
		}
	};

	public static WeakHashMap<TextureAtlasSprite, ImmutableList<BakedQuad>> quads3dcache = new WeakHashMap<TextureAtlasSprite, ImmutableList<BakedQuad>>() {
		@Override
		public ImmutableList<BakedQuad> get(Object key) {
			ImmutableList<BakedQuad> bakedQuads = super.get(key);
			if (bakedQuads == null) {
				TextureAtlasSprite sprite = (TextureAtlasSprite) key;
				if (sprite == null) {
					sprite = Textures.MISSING_SPRITE;
				}
				bakedQuads = ItemLayerModel.getQuadsForSprite(-1, sprite, DefaultVertexFormats.ITEM, CompatHelper112.optionalOf(TRSRTransformation.identity()));
				put(sprite, bakedQuads);
			}
			return bakedQuads;
		}
	};
	public static TIntObjectHashMap<WeakHashMap<BakedQuad, BakedQuad>> tintedQuads = new TIntObjectHashMap<WeakHashMap<BakedQuad, BakedQuad>>() {
		@Override
		public WeakHashMap<BakedQuad, BakedQuad> get(final int key) {
			WeakHashMap<BakedQuad, BakedQuad> weakHashMap = super.get(key);
			if (weakHashMap != null)
				return weakHashMap;
			weakHashMap = new WeakHashMap<BakedQuad, BakedQuad>() {
				@Override
				public BakedQuad get(Object quadKey) {
					BakedQuad quadTint = super.get(quadKey);
					if (quadTint != null)
						return quadTint;

					BakedQuad quad = (BakedQuad) quadKey;
					quadTint = new BakedQuad(quad.getVertexData(), key, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
					put(quad, quadTint);
					return quadTint;
				}
			};
			put(key, weakHashMap);
			return weakHashMap;
		}
	};
	protected final IXUItem item;
	protected Supplier<ModelLayer> modelFactory;
	protected ItemOverrideList overrideList = new ItemOverrideList(ImmutableList.of()) {
		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entity) {
			try {
				ModelLayer model = modelFactory.get();
				item.addQuads(model, stack);
				return model;
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting model for itemstack");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being processed");
				crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(stack.getItem()));
				crashreportcategory.addCrashSection("Item data", stack.getMetadata());
				crashreportcategory.addDetail("Item name", stack::getDisplayName);
				throw new ReportedException(crashreport);
			}

		}
	};

	public PassthruModelItem(IXUItem item) {
		this(item, item.renderAsTool() ? Transforms.itemToolsTransforms : Transforms.itemTransforms);
	}

	public PassthruModelItem(IXUItem item, EnumMap<ItemCameraTransforms.TransformType, Matrix4f> transforms) {
		this(item, () -> new ModelLayer(transforms));
	}

	public PassthruModelItem(IXUItem item, Supplier<ModelLayer> model) {
		this.item = item;
		this.modelFactory = model;
	}

	public static boolean isTransparent(int[] pixels, int uMax, int vMax, int u, int v) {
		return (pixels[u + (vMax - 1 - v) * uMax] >> 24 & 0xFF) == 0;
	}

	public static Collection<? extends BakedQuad> trySplitQuad(BakedQuad quad, TextureAtlasSprite newsprite) {
		TextureAtlasSprite sprite = quad.getSprite();
		if (NullHelper.nullable(sprite) == null) return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));
		int uMax = sprite.getIconWidth();
		int vMax = sprite.getIconHeight();

		VertexFormat format = quad.getFormat();
		int[] pixels = sprite.getFrameTextureData(0)[0];
//		boolean anyTransparent = false;
//		for (int color : pixels) {
//			int alpha = color >> 24 & 0xFF;
//			if (alpha <= 4) {
//				anyTransparent = true;
//				break;
//			}
//		}
//
//		if (!anyTransparent)
//			return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));

		int offset_position = format.getElements().indexOf(DefaultVertexFormats.POSITION_3F);
		int offset_color = format.getElements().indexOf(DefaultVertexFormats.COLOR_4UB);
		int offset_tex = format.getElements().indexOf(DefaultVertexFormats.TEX_2F);
		if (offset_position == -1 || offset_color == -1 || offset_tex == -1) {
			return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));
		}

		offset_position = format.getOffset(offset_position)/ 4;
		offset_color = format.getOffset(offset_color)/ 4;
		offset_tex = format.getOffset(offset_tex)/ 4;

		int[] vertexData = quad.getVertexData();


		UV[] vecs = new UV[4];
		float uh = 1 / (sprite.getMaxU() - sprite.getMinU());
		float vh = 1 / (sprite.getMaxV() - sprite.getMinV());
		for (int i = 0; i < 4; i++) {
			int nextOffset = format.getIntegerSize() * i;
			vecs[i] = new UV(
					Float.intBitsToFloat(vertexData[offset_position + nextOffset]),
					Float.intBitsToFloat(vertexData[offset_position + 1 + nextOffset]),
					Float.intBitsToFloat(vertexData[offset_position + 2 + nextOffset]),
					MathHelper.clamp((Float.intBitsToFloat(vertexData[offset_tex + nextOffset]) - sprite.getMinU()) * uh, 0, 1),
					MathHelper.clamp((Float.intBitsToFloat(vertexData[offset_tex + 1 + nextOffset]) - sprite.getMinV()) * vh, 0, 1)
			);
		}

		float umin = Float.POSITIVE_INFINITY;
		float vmin = Float.POSITIVE_INFINITY;
		float umax = Float.NEGATIVE_INFINITY;
		float vmax = Float.NEGATIVE_INFINITY;
		for (UV vec : vecs) {
			umin = Math.min(umin, vec.u);
			vmin = Math.min(vmin, vec.v);
			umax = Math.max(umax, vec.u);
			vmax = Math.max(vmax, vec.v);
		}

		int u_lower = MathHelper.floor(umin * 16);
		int u_upper = MathHelper.ceil(umax * 16);
		int v_lower = MathHelper.floor(vmin * 16);
		int v_upper = MathHelper.ceil(vmax * 16);

		if ((u_upper - (umax * 16)) < 0.001) umax = u_upper / 16F;
		if ((v_upper - (vmax * 16)) < 0.001) vmax = v_upper / 16F;

		if (u_lower == u_upper || v_lower == v_upper) return ImmutableList.of(new BakedQuadRetextured(quad, newsprite));

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		int i = Math.max(u_upper - u_lower, v_upper - v_lower);

		int u_m = MathHelper.ceil(Math.max(Math.abs(vecs[1].u - vecs[0].u), Math.abs(vecs[1].v - vecs[0].v)) * 16);
		int v_m = MathHelper.ceil(Math.max(Math.abs(vecs[2].u - vecs[3].u), Math.abs(vecs[2].v - vecs[3].v)) * 16);

		for (int du = 0; du < u_m; du++) {
			for (int dv = 0; dv < v_m; dv++) {
				float u0 = (float) du / (float) u_m;
				float v0 = (float) dv / (float) i;
				float u1 = (float) (du + 1) / (float) u_m;
				float v1 = (float) (dv + 1) / (float) v_m;

				UV[] uvs = {
						UV.interpolateQuad(vecs, u0, v0),
						UV.interpolateQuad(vecs, u1, v0),
						UV.interpolateQuad(vecs, u1, v1),
						UV.interpolateQuad(vecs, u0, v1),
				};

				UV center_uv = UV.interpolateQuad(uvs, 0.5F, 0.5F);

				int center_u = MathHelper.clamp(MathHelper.clamp(Math.round(center_uv.u * 16 -0.05F), u_lower, u_upper), 0, uMax - 1);
				int center_v = MathHelper.clamp(MathHelper.clamp(Math.round(center_uv.v * 16 -0.05F), v_lower, v_upper), 0, vMax - 1);

				int i1 = center_u + (center_v) * uMax;
				if(i1 < 0 || i1 >= pixels.length){
					pixels = pixels;
					continue;
				}
				int color = pixels[i1];

				int alpha = color >> 24 & 0xFF;
				if (alpha <= 4) continue;

				int b = (ColorHelper.brightness(color) * 120) / 256 + (256-120);
				int new_color = ColorHelper.color(b, b, b, alpha);
//				new_color = 0xffffffff;

				builder.add(
						QuadHelper.buildQuad(DefaultVertexFormats.ITEM, TRSRTransformation.identity(), quad.getFace(), quad.getTintIndex(),
								uvs[0], uvs[1], uvs[2], uvs[3],
								new_color, new_color, new_color, new_color,
								newsprite));


			}
		}

		return builder.build();
	}

	public static List<BakedQuad> buildFrontQuads(TextureAtlasSprite sprite, TextureAtlasSprite override) {
		int uMax = sprite.getIconWidth();
		int vMax = sprite.getIconHeight();


		VertexFormat format = DefaultVertexFormats.ITEM;

		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
		int[] pixels = sprite.getFrameTextureData(0)[0];
		for (int v = 0; v < vMax; v++) {
			for (int u = 0; u < uMax; u++) {
				int color = pixels[u + (vMax - 1 - v) * uMax];
				int alpha = color >> 24 & 0xFF;
				if (alpha == 0) continue;

				float u0 = ((float) u) / uMax;
				float u1 = ((float) u + 1) / uMax;
				float v0 = ((float) v) / vMax;
				float v1 = ((float) v + 1) / vMax;


				float ou0 = override.getInterpolatedU(16 * u0);
				float ou1 = override.getInterpolatedU(16 * u1);
				float ov0 = override.getInterpolatedV(16 * v0);
				float ov1 = override.getInterpolatedV(16 * v1);

				int b = 255;
				if (u != 0 && v != 0 && u != (uMax - 1) && v != (vMax - 1)) {
					lookForTransparent:
					for (int du = -1; du <= 1; du++) {
						for (int dv = -1; dv <= 1; dv++) {
							if (((pixels[(u + du) + (vMax - 1 - v + dv) * uMax] >> 24) & 0xFF) == 0) {
								if (du == 0 || dv == 0) {
									b = 120;
									break lookForTransparent;
								} else {
									b = 200;
								}
							}
						}
					}
				}

				b = (ColorHelper.brightness(color) * 40 + b * 216) / 256;
				color = ColorHelper.color(b, b, b, alpha);

				builder.add(QuadHelper.buildQuad(format, TRSRTransformation.identity(), EnumFacing.NORTH, -1,
						u0, v0, 7.5f / 16f, ou0, ov1,
						u1, v0, 7.5f / 16f, ou1, ov1,
						u1, v1, 7.5f / 16f, ou1, ov0,
						u0, v1, 7.5f / 16f, ou0, ov0, color, sprite
				));

				builder.add(QuadHelper.buildQuad(format, TRSRTransformation.identity(), EnumFacing.SOUTH, -1,
						u0, v0, 8.5f / 16f, ou0, ov1,
						u0, v1, 8.5f / 16f, ou0, ov0,
						u1, v1, 8.5f / 16f, ou1, ov0,
						u1, v0, 8.5f / 16f, ou1, ov1, color, sprite
				));
			}

		}

		return builder.build();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return overrideList;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return item.getBaseTexture();
	}


	public static class ModelLayer extends MutableModel {
		static ItemCameraTransforms.TransformType[] types = {
				ItemCameraTransforms.TransformType.GUI,
				ItemCameraTransforms.TransformType.NONE,
		};
		public MutableModel lowPolyVersion;

		public ModelLayer(EnumMap<ItemCameraTransforms.TransformType, Matrix4f> itemTransforms) {
			super(itemTransforms);
			isGui3D = false;
			ambientOcclusion = false;
			lowPolyVersion = new MutableModel(itemTransforms);
			for (ItemCameraTransforms.TransformType type : types) {
				transformMap.put(type, Pair.<IBakedModel, Matrix4f>of(lowPolyVersion, transformMap.get(type).getRight()));
			}
		}

		public void addBoxModel(BoxModel boxModel) {
			for (Box box : boxModel) {
				for (EnumFacing facing : EnumFacing.values()) {
					List<BakedQuad> quads = box.getQuads(facing);
					if (quads != null) this.addAllQuads(quads);
				}
				List<BakedQuad> quads = box.getQuads(null);
				if (quads != null) this.addAllQuads(quads);
			}
		}

		@Override
		public void clear() {
			super.clear();
			lowPolyVersion.clear();
		}

		public void addSprite(TextureAtlasSprite sprite) {
			generalQuads.addAll(quads3dcache.get(sprite));
			lowPolyVersion.generalQuads.addAll(quads2dcache.get(sprite));
		}

		public void addSprite(TextureAtlasSprite sprite, boolean draw3d) {
			if (tex == null) tex = sprite;
			generalQuads.addAll((draw3d ? quads3dcache : quads2dcache).get(sprite));
			lowPolyVersion.generalQuads.addAll(quads2dcache.get(sprite));
		}

		public void addTintedSprite(TextureAtlasSprite sprite, boolean draw3d, int tint) {
			if (tint == -1) {
				addSprite(sprite, draw3d);
			} else {
				for (BakedQuad bakedQuad : ((draw3d ? quads3dcache : quads2dcache).get(sprite))) {
					generalQuads.add(tintedQuads.get(tint).get(bakedQuad));
				}
				for (BakedQuad bakedQuad : quads2dcache.get(sprite)) {
					lowPolyVersion.generalQuads.add(tintedQuads.get(tint).get(bakedQuad));
				}
			}
		}

		public void addQuad(BakedQuad quad) {
			generalQuads.add(quad);
			lowPolyVersion.generalQuads.add(quad);
		}

		public void addAllQuads(Collection<BakedQuad> quads) {
			generalQuads.addAll(quads);
			lowPolyVersion.generalQuads.addAll(quads);
		}
	}
}
