package com.rwtema.extrautils2.transfernodes;

import com.rwtema.extrautils2.backend.XUBlock;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.tile.TilePower;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTransferHolder extends XUBlock {
	private static IBlockState holderState;

	public BlockTransferHolder() {
		super(Material.CLAY, MapColor.STONE);
		holderState = getDefaultState();
	}

	public static boolean placePipe(World world, BlockPos pos, EntityPlayer player) {
		IBlockState blockState = world.getBlockState(pos);

		if (blockState == holderState) {
			TileEntity tile = world.getTileEntity(pos);
			TileTransferHolder holder;
			if (!(tile instanceof TileTransferHolder)) {
				world.removeTileEntity(pos);
				tile = world.getTileEntity(pos);
			}
			holder = (TileTransferHolder) tile;
			if (!world.isRemote && !PowerManager.canUse(player, holder)) {
				return false;
			}
			if (holder.centerPipe != null) return false;
			holder.centerPipe = BlockTransferPipe.stateBuilder.defaultState;
			holder.markForUpdate();
			return true;
		}

		return blockState.getBlock().isReplaceable(world, pos) && world.setBlockState(pos, BlockTransferPipe.stateBuilder.defaultState);

	}

	public static boolean placeGrocket(EntityPlayer player, World world, BlockPos pos, Grocket grocket, EnumFacing facing) {
		IBlockState blockState = world.getBlockState(pos);
		if (blockState == holderState) {
			TileEntity tile = world.getTileEntity(pos);
			if (!(tile instanceof TileTransferHolder)) {
				world.removeTileEntity(pos);
				tile = world.getTileEntity(pos);
			}
			TileTransferHolder holder = (TileTransferHolder) tile;
			if (!world.isRemote && !PowerManager.canUse(player, holder)) {
				return false;
			}

			if (holder.grockets[facing.ordinal()] != null) return false;
			holder.addGrocket(player, grocket, facing);
			holder.markForUpdate();
			return true;
		}

		if (BlockTransferPipe.stateBuilder.genericPipeStates.contains(blockState)) {
			world.setBlockState(pos, holderState);
			world.removeTileEntity(pos);
			TileTransferHolder tileEntity = (TileTransferHolder) world.getTileEntity(pos);
			if (!world.isRemote)
				tileEntity.frequency = Freq.getBasePlayerFreq((EntityPlayerMP) player);
			tileEntity.addGrocket(player, grocket, facing);
			tileEntity.centerPipe = blockState;
			tileEntity.markForUpdate();
			return true;
		}

		if (blockState.getBlock().isReplaceable(world, pos)) {
			world.setBlockState(pos, holderState);
			TileTransferHolder tileEntity = (TileTransferHolder) world.getTileEntity(pos);
			if (!world.isRemote)
				tileEntity.frequency = Freq.getBasePlayerFreq((EntityPlayerMP) player);
			tileEntity.addGrocket(player, grocket, facing);
			tileEntity.markForUpdate();
			return true;
		}

		return false;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.build();
	}

	@Override
	public boolean isToolEffective(String type, @Nonnull IBlockState state) {
		return true;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
		return true;
	}

	@Nonnull
	@Override
	public BoxModel getWorldModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		if (world != null && pos != null) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileTransferHolder) {
				TileTransferHolder transferHolder = (TileTransferHolder) tileEntity;

				BoxModel worldModel = transferHolder.worldModel;

				if (worldModel != null) return worldModel;
				worldModel = new BoxModel();
				IBlockState centerPipe = transferHolder.centerPipe;
				if (centerPipe != null)
					worldModel.addAll(BlockTransferPipe.stateBuilder.mainBlock.getWorldModel(world, pos, centerPipe));

				Grocket[] grockets = transferHolder.grockets;
				for (EnumFacing facing : EnumFacing.values()) {
					Grocket grocket = grockets[facing.ordinal()];
					if (grocket != null) {
						worldModel.addAll(grocket.getWorldModel(facing));
					}
				}
//			transferHolder.worldModel = worldModel;
				return worldModel;
			}
		}
		return new BoxModel(0, 0, 0, 1, 1, 1);
	}

	@Override
	public BoxModel getRenderModel(IBlockAccess world, BlockPos pos, @Nullable IBlockState state) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileTransferHolder) {
			TileTransferHolder transferHolder = (TileTransferHolder) tileEntity;

			BoxModel worldModel = transferHolder.worldModel;

			if (worldModel != null) return worldModel;
			worldModel = new BoxModel();
			IBlockState centerPipe = transferHolder.centerPipe;
			if (centerPipe != null)
				worldModel.addAll(BlockTransferPipe.stateBuilder.mainBlock.getRenderModel(world, pos, centerPipe));

			Grocket[] grockets = transferHolder.grockets;
			for (EnumFacing facing : EnumFacing.values()) {
				Grocket grocket = grockets[facing.ordinal()];
				if (grocket != null) {
					worldModel.addAll(grocket.getWorldModel(facing));
				}
			}

			return worldModel;
		}
		return new BoxModel(0, 0, 0, 1, 1, 1);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileTransferHolder();
	}

	@Nonnull
	@Override
	public BoxModel getInventoryModel(@Nullable ItemStack item) {
		return BoxModel.newStandardBlock("transfernodes/pipes");
	}


	@Override
	public void harvestBlock(@Nonnull World worldIn, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
		if (te instanceof TileTransferHolder) {
			TileTransferHolder holder = (TileTransferHolder) te;

			if (holder.centerPipe != null)
				spawnAsEntity(worldIn, pos, XU2Entries.pipe.newStack());

			for (Grocket grocket : holder.grockets) {
				if (grocket != null)
					for (ItemStack itemStack : grocket.getDrops()) {
						if (StackHelper.isNonNull(itemStack))
							spawnAsEntity(worldIn, pos, itemStack);
					}
			}
		} else {
			super.harvestBlock(worldIn, player, pos, state, te, stack);
		}
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<>();
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileTransferHolder) {
			TileTransferHolder holder = (TileTransferHolder) tileEntity;

			if (holder.centerPipe != null)
				list.add(XU2Entries.pipe.newStack());

			for (Grocket grocket : holder.grockets) {
				if (grocket != null)
					list.addAll(grocket.getDrops());
			}
		}

		return list;
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.ExtraUtils2:pipe.pipe.container";
	}
}
