package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.*;
import com.rwtema.extrautils2.utils.datastructures.ThreadLocalBoolean;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BlockAdvInteractor extends XUBlockStatic {
	private final String texture;

	public BlockAdvInteractor(String texture) {
		this.texture = texture;
	}

	public static EnumFacing func_185647_a(BlockPos pos, EntityLivingBase entity) {
		if (MathHelper.abs((float) entity.posX - (float) pos.getX()) < 2.0F && MathHelper.abs((float) entity.posZ - (float) pos.getZ()) < 2.0F) {
			double d0 = entity.posY + (double) entity.getEyeHeight();

			if (d0 - (double) pos.getY() > 2.0D) {
				return EnumFacing.UP;
			}

			if ((double) pos.getY() - d0 > 0.0D) {
				return EnumFacing.DOWN;
			}
		}

		return entity.getHorizontalFacing().getOpposite();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = getModel();
		model.rotateToSide(state.getValue(XUBlockStateCreator.ROTATION_ALL).getOpposite());
		return model;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_ALL, EnumFacing.UP)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing value = func_185647_a(pos, placer);
		return super.xuOnBlockPlacedBase(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(XUBlockStateCreator.ROTATION_ALL, value);
	}

	protected BoxModel getModel() {
		BoxModel model = BoxModel.newStandardBlock("interact_side");
		model.setTextures(EnumFacing.UP, texture);
		model.setTextures(EnumFacing.DOWN, "interact_back");
		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public abstract XUTile createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return true;
	}

	public static class Mine extends BlockAdvInteractor {
		public Mine() {
			super("interact_mine");
		}

		@Nonnull
		@Override
		public TileAdvInteractor createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
			return new TileMine();
		}
	}

	public static class Use extends BlockAdvInteractor {
		public static final ThreadLocalBoolean rayTraceFlag = new ThreadLocalBoolean(false);

		public Use() {
			super("interact_use");
		}

		@Nonnull
		@Override
		public TileAdvInteractor createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
			return new TileUse();
		}

		@Override
		@Nullable
		public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
			if (rayTraceFlag.get()) {
				return null;
			}
			return super.collisionRayTrace(blockState, worldIn, pos, start, end);
		}
	}

	public static class Scanner extends BlockAdvInteractor {
		public Scanner() {
			super("interact_scanner");
		}

		@Override
		public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
			XUTile tile = getTile(blockAccess, pos);
			return tile instanceof TileScanner && ((TileScanner) tile).isPowered() ? 15 : 0;
		}

		@Override
		public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
			return false;
		}

		@Override
		public boolean canProvidePower(IBlockState state) {
			return true;
		}

		@Nonnull
		@Override
		public XUTile createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
			return new TileScanner();
		}

		@Nonnull
		@Override
		protected XUBlockStateCreator createBlockState() {
			return new XUBlockStateCreator.Builder(this)
					.addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_ALL, EnumFacing.UP)
					.build();
		}
	}
}
