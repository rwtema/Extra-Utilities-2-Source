package com.rwtema.extrautils2.backend.model;

import com.rwtema.extrautils2.backend.XUBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

@SideOnly(Side.CLIENT)
public class PassthruModelBlock extends NullModel {
	protected final XUBlock block;
	protected final XUBlockState xuBlockState;
	protected final ModelResourceLocation modelResourceLocation;

	public PassthruModelBlock(XUBlock block, IBlockState key, ModelResourceLocation modelResourceLocation) {
		this.block = block;
		this.xuBlockState = (XUBlockState) key;
		this.modelResourceLocation = modelResourceLocation;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return block.getInventoryModel(null).getTex();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if (state == null || !(state instanceof XUBlockState) || state.getBlock() != block) {
			return getQuadsNoWorld(state, side, rand);
		}
		ThreadLocal<MutableModel> result = ((XUBlockState) state).result;
		if (result == null) {
			return getQuadsNoWorld(state, side, rand);
		}
		MutableModel model = result.get();
		if (model == null) {
			return getQuadsNoWorld(state, side, rand);
		}
		return model.getQuads(state, side, rand);
	}

	@Nonnull
	private List<BakedQuad> getQuadsNoWorld(IBlockState state, EnumFacing side, long rand) {
		return block.getInventoryModel(null).loadIntoMutable(new MutableModel(Transforms.blockTransforms), MinecraftForgeClient.getRenderLayer()).getQuads(state, side, rand);
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID;
	}


}
