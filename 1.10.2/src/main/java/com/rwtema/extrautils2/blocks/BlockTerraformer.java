package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.tile.TileTerraformer;
import com.rwtema.extrautils2.tile.TileTerraformerClimograph;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class BlockTerraformer extends XUBlockStatic {
	public static final PropertyEnumSimple<Type> TYPE = new PropertyEnumSimple<Type>(Type.class);
	public static final List<Pair<Type, Type>> OPPOSITES;

	static {
		ImmutableList.Builder<Pair<Type, Type>> builder = ImmutableList.builder();
		builder.add(Pair.of(Type.HEATER, Type.COOLER));
		builder.add(Pair.of(Type.HUMIDIFIER, Type.DEHUMIDIFIER));
		builder.add(Pair.of(Type.MAGIC_ABSORBTION, Type.MAGIC_INFUSER));

		OPPOSITES = builder.build();
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addDropProperties(TYPE).build();
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		Type value = state.getValue(TYPE);
		BoxModel model;
		switch (value) {
			case ANTENNA:
				model = new BoxModel();
				model.addBoxI(2, 0, 2, 14, 1, 14, "terraformer/antenna_base");
				model.addBoxI(7, 1, 7, 9, 6, 9, "terraformer/antenna_side");
				model.addBoxI(4, 6, 6, 12, 10, 10, "terraformer/antenna_top");
				model.addBoxI(6, 6, 4, 10, 10, 12, "terraformer/antenna_top");
				for (int i = 0; i < 4; i++) {
					model.addBoxI(0, 5, 8 - 2, 4, 16, 8 + 2, "terraformer/antenna_side").setTextureSides(EnumFacing.UP, "terraformer/antenna_top", EnumFacing.DOWN, "terraformer/antenna_top").rotateY(i);
				}
				break;
			case CLIMOGRAPH_BASE:
				model = BoxModel.newStandardBlock("terraformer/terraformer_side").setTextures(EnumFacing.DOWN, "terraformer/terraformer_base");
				break;
			default:
				model = BoxModel.newStandardBlock("terraformer/" + value.name().toLowerCase(Locale.ENGLISH));
				model.setTextures(EnumFacing.UP, "terraformer/terraformer_side", EnumFacing.DOWN, "terraformer/terraformer_base");
				break;
		}
		return model;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(TYPE).te != null;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		Supplier<? extends XUTile> te = state.getValue(TYPE).te;
		return te != null ? te.get() : null;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		Type type = xuBlockState.getStateFromItemStack(stack).getValue(TYPE);
		if (type != Type.CONTROLLER && type != Type.ANTENNA && type != Type.CLIMOGRAPH_BASE) {
			tooltip.add(Lang.translate("Climograph"));
		}
	}

	public enum Type {
		CONTROLLER(TileTerraformer::new),
		ANTENNA(null),
		HUMIDIFIER(0xFF0191FF, 0xFF0E75FF, 0xFF1A5EFF),
		DEHUMIDIFIER(0xFFFFF9DD, 0xFFFFF4B8, 0xFFFFEA76),
		HEATER(0xFFFFD200, 0xFFFF9900, 0xFFFF6A00),
		COOLER(0xFFBDD4F5, 0xFF8EAAD5, 0xFF3C609E),
		DEHOSTILIFIER(0xFFC9F3F3, 0xFFA6CACA, 0xFF7D9999, 0xFF6D8686, 0xFFFDFFA8),
		MAGIC_ABSORBTION(0xFFCB00CF, 0xDE00AA, 0xFFFA0076),
		MAGIC_INFUSER(0xFFF700DF, 0xFFE000E9, 0xFFB200FF),
		CLIMOGRAPH_BASE(null);

		@Nullable
		public final Supplier<? extends XUTile> te;
		public final int[] colors;

		Type(int... colors) {
			this(TileTerraformerClimograph::new, colors);
		}

		Type(@Nullable Supplier<? extends XUTile> te, int... colors) {
			this.te = te;
			this.colors = colors;
		}
	}
}
