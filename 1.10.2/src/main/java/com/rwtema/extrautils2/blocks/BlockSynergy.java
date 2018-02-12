package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.IMetaProperty;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.XUBlockState;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.tile.TileSynergyUnit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

import static com.rwtema.extrautils2.blocks.BlockPassiveGenerator.GeneratorType.*;

public class BlockSynergy extends XUBlockStatic {

	public final static IMetaProperty<SynergyType> TYPE = new IMetaProperty.WrapTile<SynergyType, TileSynergyUnit>(TileSynergyUnit.class, new PropertyEnumSimple<>(SynergyType.class), SynergyType.BLANK) {
		@Override
		public SynergyType getValue(TileSynergyUnit tile) {
			return tile.synergy_type.value;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public boolean addLocalization() {
			return true;
		}
	};

	public static SynergyType getMeta(int meta) {
		return meta >= 0 && meta < SynergyType.values().length ? SynergyType.values()[meta] : SynergyType.BLANK;
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return XUBlockStateCreator.builder(this).addMetaProperty(TYPE).build();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		TileSynergyUnit synergyUnit = new TileSynergyUnit();
		synergyUnit.synergy_type.value = state.getValue(TYPE);
		return synergyUnit;
	}

	@Override
	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < SynergyType.values().length; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Nonnull
	@Override
	public IBlockState xuOnBlockPlacedBase(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(TYPE, getMeta(meta));
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		BoxModel model = new BoxModel();
		model.addBoxI(0, 0, 0, 16, 2, 16);
		for (int i = 0; i < 4; i++) {
			model.addBoxI(1, 2, 1, 4, 14, 4).rotateY(i);
		}
		Box box = model.addBoxI(0, 14, 0, 16, 16, 16);
		model.setTextures("synergy/synergy_side", 0, "panel_base", 1, "panel_base");
		SynergyType value = state.getValue(TYPE);
		if (value != SynergyType.BLANK) {
			box.setTextureSides(1, "synergy/synergy_" + state.getValue(TYPE).name().toLowerCase(Locale.ENGLISH));
		}
		return model;
	}

	@Override
	public XUBlockState getStateFromItemStack(@Nullable ItemStack item) {
		if (StackHelper.isNull(item)) return xuBlockState.defaultState;
		return (XUBlockState) xuBlockState.defaultState.withProperty(TYPE, getMeta(item.getItemDamage()));
	}

	public enum SynergyType {
		BLANK(null, null),

		WITHER(DRAGON_EGG, FIRE),
		NETHER(DRAGON_EGG, LAVA),
		VOID(DRAGON_EGG, LUNAR),
		TEMPLE(DRAGON_EGG, SOLAR),
		GUARDIAN(DRAGON_EGG, WATER),
		END(DRAGON_EGG, WIND),

		BRIMSTONE(FIRE, LAVA),
		BLOODMOON(FIRE, LUNAR),
		INCANDESCENCE(FIRE, SOLAR),
		STEAM(FIRE, WATER),
		FIRESTORM(FIRE, WIND),

		MOONQUAKE(LAVA, LUNAR),
		PLASMA(LAVA, SOLAR),
		COBBLESTONE(LAVA, WATER),
		METEOR(LAVA, WIND),

		ECLIPSE(LUNAR, SOLAR),
		LOWTIDE(LUNAR, WATER),
		AURORA(LUNAR, WIND),

		HIGHTIDE(SOLAR, WATER),
		SOLARFLARE(SOLAR, WIND),

		RAIN(WATER, WIND);

		public BlockPassiveGenerator.GeneratorType a;
		public BlockPassiveGenerator.GeneratorType b;
		public float boost = 0.05F;

		SynergyType(BlockPassiveGenerator.GeneratorType a, BlockPassiveGenerator.GeneratorType b) {
			this.a = a;
			this.b = b;
		}

	}
}
