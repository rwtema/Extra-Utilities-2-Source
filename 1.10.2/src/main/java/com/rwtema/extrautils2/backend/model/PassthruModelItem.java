package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.IXUItem;
import com.rwtema.extrautils2.compatibility.CompatHelper112;
import com.rwtema.extrautils2.utils.helpers.QuadHelper;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
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
