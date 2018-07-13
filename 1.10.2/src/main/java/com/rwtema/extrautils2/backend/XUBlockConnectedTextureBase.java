package com.rwtema.extrautils2.backend;

import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.textures.ISolidWorldTexture;
import com.rwtema.extrautils2.textures.TextureComponent;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public abstract class XUBlockConnectedTextureBase extends XUBlock {
	public final BoxModel worldModel;
	public HashMap<IBlockState, BoxModel> invModels = new HashMap<>();

	{
		worldModel = createBaseModel();
		if (!blockMaterial.blocksLight())
			worldModel.renderAsNormalBlock = false;
	}

	public XUBlockConnectedTextureBase(Material materialIn) {
		super(materialIn);
	}

	@Override
	public void clearCaches() {
		invModels.clear();
	}

	@Override
	public abstract boolean isOpaqueCube(IBlockState state);


	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
		IBlockState state = blockAccess.getBlockState(pos.offset(side));
		return state != blockState && !state.doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
	}

	@SideOnly(Side.CLIENT)
	public abstract ISolidWorldTexture getConnectedTexture(IBlockState state, EnumFacing side);

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		return worldModel;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		IBlockState state = xuBlockState.getStateFromItemStack(item);

		BoxModel model = invModels.get(state);
		if (model == null) {
			model = createBaseModel();
			for (EnumFacing facing : EnumFacing.values()) {
				model.forEach(box -> box.setTextureSides(facing, getConnectedTexture(state, facing).getItemTexture(facing)));
			}
			invModels.put(state, model);
		}
		return model;
	}

	public BlockRenderLayer renderLayer(IBlockState state) {
		return getBlockLayer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		IBlockState state1 = state == null ? world.getBlockState(pos) : state;
		BoxModel boxes = createBaseModel();
		boxes.clear();
		BlockRenderLayer layer = renderLayer(state);

		EnumFacing[] values = EnumFacing.values();
		for (int side_index = 0; side_index < 6; side_index++) {
			EnumFacing side = values[side_index];
			boolean renderSide = shouldSideBeRendered(state1, world, pos, side);
			if (renderSide) {
				ISolidWorldTexture texture = getConnectedTexture(state1, side);
				List<TextureComponent> composites = texture.getComposites(world, pos, side);

				for (TextureComponent composite : composites) {
					String texture1 = Textures.spritesInverse.get(composite.sprite);
					Box box;
					float[] tex = {composite.u0, composite.v1, composite.u1, composite.v0};
					switch (side) {
						case DOWN:
							box = boxes.addBoxI(composite.u0, 0, composite.v0, composite.u1, 0, composite.v1, texture1);
							break;
						case UP:
							box = boxes.addBoxI(composite.u0, 16, composite.v0, composite.u1, 16, composite.v1, texture1);

							break;
						case NORTH:
							box = boxes.addBoxI(composite.u0, 16 - composite.v0, 0, composite.u1, 16 - composite.v1, 0, texture1);

							break;
						case SOUTH:
							box = boxes.addBoxI(composite.u0, 16 - composite.v0, 16, composite.u1, 16 - composite.v1, 16, texture1);
							break;
						case WEST:
							box = boxes.addBoxI(0, 16 - composite.v0, composite.u0, 0, 16 - composite.v1, composite.u1, texture1);
							break;
						case EAST:
							box = boxes.addBoxI(16, 16 - composite.v0, composite.u0, 16, 16 - composite.v1, composite.u1, texture1);
							break;
						default:
							box = new Box(0, 0, 0, 0, 0, 0);
							break;
					}

					float[][] bbfloats = {null, null, null, null, null, null};

					bbfloats[side_index] = tex;
					box.setTextureBounds(bbfloats);

					box.layer = layer;
					box.invisible[side_index ^ 1] = true;
				}
			}
		}
		return boxes;
	}

	public boolean isDoubleSided(IBlockState state) {
		return false;
	}

	protected BoxModel createBaseModel() {
		return BoxModel.newStandardBlock();
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return super.shouldCheckWeakPower(state, world, pos, side);
	}
}
