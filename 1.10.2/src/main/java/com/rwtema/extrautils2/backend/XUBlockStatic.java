package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public abstract class XUBlockStatic extends XUBlock {

	public final HashMap<IBlockState, EnumSet<BlockRenderLayer>> layerMap = new HashMap<>(1, 0.2F);
	public final HashMap<IBlockState, BoxModel> cachedModels = new HashMap<IBlockState, BoxModel>() {
		@Override
		public BoxModel get(Object key) {
			BoxModel boxes = super.get(key);
			if (boxes == null || recalc_models()) {
				IBlockState state = key != null ? (IBlockState) key : getDefaultState();
				boxes = getModel(state);
				super.put(state, boxes);
			}
			return boxes;
		}
	};
	public final HashMap<IBlockState, BoxModel> cachedInvModels = new HashMap<IBlockState, BoxModel>() {
		@Override
		public BoxModel get(Object key) {
			BoxModel boxes = super.get(key);
			if (boxes == null || recalc_models()) {
				IBlockState state = key != null ? (IBlockState) key : getDefaultState();
				boxes = getModelInv(state);
				boxes.moveToCenterForInventoryRendering();
				super.put(state, boxes);
			}
			return boxes;
		}
	};

	public XUBlockStatic(Material materialIn, MapColor color) {
		super(materialIn, color);
	}

	public XUBlockStatic(Material materialIn) {
		super(materialIn);
	}

	public XUBlockStatic() {
		super();
	}

	public static boolean recalc_models() {
		return ExtraUtils2.deobf_folder;
	}

	public static <K, V> HashMap<K, V> dummyCreateHash(Object s) {
		return new HashMap<>();
	}

	@Override
	public void registerTextures() {
		for (IBlockState iBlockState : xuBlockState.getValidStates()) {
			cachedModels.get(iBlockState).registerTextures();
		}
	}

	@Override
	public void clearCaches() {
		cachedModels.clear();
		cachedInvModels.clear();
		layerMap.clear();
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		return cachedModels.get(state == null ? world.getBlockState(pos) : state);
	}

	@Override
	public BoxModel getGenericWorldModel(IBlockState state) {
		if (cachedModels == null) return getModel(state);
		return super.getGenericWorldModel(state);
	}

	@Nonnull
	@Override
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		return cachedInvModels.get(xuBlockState.getStateFromItemStack(item));
	}

	public BoxModel getModelInv(IBlockState state) {
		return getModel(state);
	}

	public abstract BoxModel getModel(IBlockState state);

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		@Nullable
		EnumSet<BlockRenderLayer> set = this.layerMap.get(state);
		if (set == null) {
			set = EnumSet.noneOf(BlockRenderLayer.class);

			for (Box box : cachedModels.get(state)) {
				set.add(box.layer);
			}

			this.layerMap.put(state, set);
		}
		return set.contains(layer);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxBase(IBlockState state, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos) {
		return cachedModels.get(state).getAABB(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public PassthruModelBlock createPassthruModel(IBlockState state, ModelResourceLocation location) {
		return new PassthruModelBlock(this, state, location) {
			HashMap<IBlockState, HashMap<EnumFacing, HashMap<BlockRenderLayer, List<BakedQuad>>>> cachedLists = new HashMap<>();

			@Nonnull
			@Override
			@SideOnly(Side.CLIENT)
			public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
				return cachedLists
						.computeIfAbsent(state, XUBlockStatic::dummyCreateHash)
						.computeIfAbsent(side, XUBlockStatic::dummyCreateHash)
						.computeIfAbsent(MinecraftForgeClient.getRenderLayer(),
								new Function<BlockRenderLayer, List<BakedQuad>>() {
									@Override
									@SideOnly(Side.CLIENT)
									public List<BakedQuad> apply(BlockRenderLayer layer) {
										MutableModel model = new MutableModel(Transforms.blockTransforms);
										cachedModels.get(state).loadIntoMutable(model, layer);
										return model.getQuads(state, side, rand);
									}
								}
						);
			}
		};
	}

	@Override
	public boolean allowOverride() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public MutableModel recreateNewInstance(MutableModel result) {
		return result;
	}
}
