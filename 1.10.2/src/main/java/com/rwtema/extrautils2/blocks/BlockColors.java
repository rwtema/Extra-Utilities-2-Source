package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.textures.SpriteColorMask;
import com.rwtema.extrautils2.utils.helpers.ColorHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockColors extends XUBlock {

	final String tex;

	String spriteBase;
	String spriteMask;

	public BlockColors(Material rock, String tex) {
		super(rock);
		this.tex = tex;
	}

	@Override
	public void registerTextures() {
		spriteBase = SpriteColorMask.registerSupplier(tex, "base", false);
		spriteMask = SpriteColorMask.registerSupplier(tex, "mask", true);
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		return BoxModel.newStandardBlock();
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT || layer==BlockRenderLayer.SOLID;
	}

	@Override
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		BoxModel model = new BoxModel();
		model.renderAsNormalBlock = true;

		final float eps2 = 0;
		Box mask = new Box(eps2, eps2, eps2, 1-eps2, 1-eps2, 1-eps2);
		mask.texture = spriteMask;
		mask.setLayer(BlockRenderLayer.TRANSLUCENT);
		long positionRandom = MathHelper.getPositionRandom(pos);
		Random random = new Random(positionRandom);
		mask.tint = ColorHelper.color(random.nextInt(256),random.nextInt(256),random.nextInt(256),255 );
		model.add(mask);
//
		final float eps = 0F;
		Box under = new Box(eps, eps, eps, 1 - eps, 1 - eps, 1 - eps);
		for (EnumFacing side : EnumFacing.values()) {
			if (world.getBlockState(pos.offset(side)).doesSideBlockRendering(world, pos.offset(side), side.getOpposite())) {
				under.invisible[side.ordinal()] = true;
				mask.invisible[side.ordinal()] = true;
				switch (side) {
					case DOWN:
						under.minY = 0;
						break;
					case UP:
						under.maxY = 1;
						break;
					case NORTH:
						under.minZ = 0;
						break;
					case SOUTH:
						under.maxZ = 1;
						break;
					case WEST:
						under.minX = 0;
						break;
					case EAST:
						under.maxX = 1;
						break;
				}
			}
		}


		under.texture = spriteBase;

		model.add(under);
		return model;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, net.minecraft.client.renderer.color.BlockColors blockColors) {
		blockColors.registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
			return tintIndex;
		}, this);
	}

	@Nonnull
	@Override
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		return BoxModel.newStandardBlock(spriteBase);
	}

}
