package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.IMetaProperty;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStaticRotation;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TilePlayerChest;
import com.rwtema.extrautils2.tile.TilePower;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockPlayerChest extends XUBlockStaticRotation {
	public static final IMetaProperty<Boolean> PLAYER_ONLINE = new IMetaProperty.WrapTile<Boolean, TilePlayerChest>(TilePlayerChest.class, PropertyBool.create("player_online")) {

		@Override
		public Boolean getValue(TilePlayerChest tile) {
			return tile.getOwnerPlayer() != null;
		}
	};

	public BlockPlayerChest() {
		super(Material.WOOD);
	}

	@Override
	protected BoxModel createBaseModel(IBlockState baseState) {
		BoxModel boxes = new BoxModel();
		boxes.addBoxI(1, 0, 1, 15, 14, 15, "player_chest_side").setTextureSides(0, "player_chest_bottom", 1, "player_chest_top", 3, "player_chest_front");
		return boxes;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TilePlayerChest();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this)
				.addWorldPropertyWithDefault(XUBlockStateCreator.ROTATION_HORIZONTAL, EnumFacing.SOUTH)
				.addMetaProperty(TilePower.ENABLED_STATE)
				.addMetaProperty(PLAYER_ONLINE)
				.build();
	}
}
