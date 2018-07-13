package com.rwtema.extrautils2.backend.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.rwtema.extrautils2.utils.client.GLState;
import com.rwtema.extrautils2.utils.helpers.NullHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.rwtema.extrautils2.compatibility.ICompatPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class MutableModel implements ICompatPerspectiveAwareModel {

	public final List<BakedQuad> generalQuads = Lists.newArrayList();
	public final ImmutableList<ArrayList<BakedQuad>> sidedQuads = ImmutableList.of(
			Lists.newArrayList(),
			Lists.newArrayList(),
			Lists.newArrayList(),
			Lists.newArrayList(),
			Lists.newArrayList(),
			Lists.newArrayList());
	public final EnumMap<ItemCameraTransforms.TransformType, Pair<? extends IBakedModel, Matrix4f>> transformMap;
	public boolean ambientOcclusion;
	public TextureAtlasSprite tex;
	public boolean isGui3D;
	ItemOverrideList overrideList = new ItemOverrideList(ImmutableList.of()) {
		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entity) {
			return originalModel;
		}
	};
	private List<GLState<?>> states;

	public MutableModel(EnumMap<ItemCameraTransforms.TransformType, Matrix4f> type) {
		this.transformMap = Transforms.createMap(this, type);
	}

	public void clear() {
		tex = null;
		states = null;
		generalQuads.clear();
		for (ArrayList<BakedQuad> sidedQuad : sidedQuads) {
			sidedQuad.clear();
		}
		for (Pair<? extends IBakedModel, Matrix4f> pair : transformMap.values()) {
			IBakedModel model = pair.getKey();
			if (model instanceof MutableModel && model != this) {
				((MutableModel) model).clear();
			}
		}
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if (side == null) return ImmutableList.copyOf(generalQuads);
		return ImmutableList.copyOf(sidedQuads.get(side.getIndex()));
	}

	@Override
	public boolean isAmbientOcclusion() {
		return ambientOcclusion;
	}

	public void addGLState(GLState<?> state) {
		if (states == null) states = new ArrayList<>();
		states.add(state);
	}

	@Override
	public boolean isGui3d() {
		return isGui3D;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		TextureAtlasSprite tex = this.tex;
		if (tex == null) {
			for (BakedQuad quad : generalQuads) {
				if ((tex = NullHelper.nullable(quad.getSprite())) != null) return tex;
			}
			for (ArrayList<BakedQuad> sidedQuad : sidedQuads) {
				for (BakedQuad quad : sidedQuad) {
					if ((tex = NullHelper.nullable(quad.getSprite())) != null) return tex;
				}
			}
		}
		return tex == null ? Textures.MISSING_SPRITE : tex;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return overrideList;
	}

	@Override
	@SuppressWarnings("deprecation")
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		handleGLStates();
		return transformMap.get(cameraTransformType);
	}

	public void handleGLStates() {
		if (states != null && "Client thread".equals(Thread.currentThread().getName())) {
			for (GLState<?> state : states) {
				state.setValue();
			}
		}
	}


	public Iterable<BakedQuad> getAllQuads() {
		return Iterables.concat(generalQuads, Iterables.concat(sidedQuads));
	}

	public void rotateY(float x, float z, float t) {
		float c = MathHelper.cos(t);
		float s = MathHelper.sin(t);

		for (BakedQuad quad : getAllQuads()) {
			int[] data = quad.getVertexData();
			for (int i = 0; i < 28; i += 7) {
				float ax = Float.intBitsToFloat(data[i]) - x;
				float az = Float.intBitsToFloat(data[i + 2]) - z;

				data[i] = Float.floatToRawIntBits(x + ax * c - az * s);
				data[i + 2] = Float.floatToRawIntBits(z + ax * s + az * c);
			}
		}

	}
}
