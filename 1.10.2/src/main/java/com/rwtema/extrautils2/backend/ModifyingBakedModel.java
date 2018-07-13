package com.rwtema.extrautils2.backend;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.compatibility.ICompatPerspectiveAwareModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.HashMap;
import java.util.List;

public class ModifyingBakedModel implements IBakedModel {
	IBakedModel base;
	IQuadReDesigner designer;
	HashMap<EnumFacing, List<BakedQuad>> quads = new HashMap<>();

	private ModifyingBakedModel(IBakedModel base, IQuadReDesigner designer) {
		this.base = base;
		this.designer = designer;
	}

	public static ModifyingBakedModel create(IBakedModel base, IQuadReDesigner designer) {
		if (base instanceof ICompatPerspectiveAwareModel) {
			return new ModifyingBakedModelPersp((ICompatPerspectiveAwareModel) base, designer);
		} else
			return new ModifyingBakedModel(base, designer);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return quads.computeIfAbsent(side, enumFacing -> designer.redesign(base.getQuads(state, enumFacing, rand), base, state, enumFacing, rand));
	}

	@Override
	public boolean isAmbientOcclusion() {
		return base.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return base.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return base.isBuiltInRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return base.getParticleTexture();
	}

	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms() {
		return base.getItemCameraTransforms();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		final ItemOverrideList overrides = base.getOverrides();
		return new ItemOverrideList(Lists.newArrayList()) {
			@Nonnull
			@Override
			public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entity) {
				IBakedModel iBakedModel = overrides.handleItemState(originalModel, stack, world, entity);
				if (iBakedModel == base) return ModifyingBakedModel.this;
				return create(iBakedModel, designer);
			}
		};
	}

	public interface IQuadReDesigner {
		@Nonnull
		List<BakedQuad> redesign(@Nonnull List<BakedQuad> original, IBakedModel base, IBlockState state, EnumFacing side, long rand);
	}

	private static class ModifyingBakedModelPersp extends ModifyingBakedModel implements ICompatPerspectiveAwareModel {

		private final ICompatPerspectiveAwareModel basePersp;

		private ModifyingBakedModelPersp(ICompatPerspectiveAwareModel base, IQuadReDesigner designer) {
			super(base, designer);
			basePersp = base;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
			Pair<? extends IBakedModel, Matrix4f> pair = basePersp.handlePerspective(cameraTransformType);
			IBakedModel model = pair.getLeft();
			ModifyingBakedModel m;
			if (model == basePersp) {
				m = this;
			} else {
				m = create(model, designer);
			}
			return Pair.of(m, pair.getRight());
		}
	}
}
