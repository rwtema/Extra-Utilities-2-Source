package com.rwtema.extrautils2.blocks;

import com.google.common.collect.ImmutableList;
import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.Box;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.backend.model.BoxSingleQuad;
import com.rwtema.extrautils2.backend.model.UV;
import com.rwtema.extrautils2.fluids.FluidColors;
import com.rwtema.extrautils2.tile.TileDrum;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

import static com.rwtema.extrautils2.tile.TileDrum.getFluidFromItemStack;

public class BlockDrum extends XUBlockStatic {
	public static final PropertyEnumSimple<Capacity> PROPERTY_CAPACITY = new PropertyEnumSimple<>(Capacity.class);

	@Override
	public void getSubBlocksBase(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < xuBlockState.dropmeta2state.length; i++) {
			ItemStack emptyStack = new ItemStack(itemIn, 1, i);
			list.add(emptyStack);
//			int capacity = xuBlockState.dropmeta2state[i].getValue(PROPERTY_CAPACITY).capacity;
//			for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
//				ItemStack copy = emptyStack.copy();
//				NBTTagCompound tankNBT = new NBTTagCompound();
//				FluidStack fluidStack = new FluidStack(fluid, capacity * 1000);
//				fluidStack.writeToNBT(tankNBT);
//				copy.setTagInfo("Fluid", tankNBT);
//				list.add(copy);
//			}
		}
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		return super.getComparatorInputOverride(blockState, worldIn, pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addItemColors(ItemColors itemColors, BlockColors blockColors) {
		itemColors.registerItemColorHandler((stack, tintIndex) -> {
			FluidStack fluidStack = getFluidFromItemStack(stack);
			return FluidColors.getColor(fluidStack);
		}, BlockDrum.this);

		blockColors.registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				if (tintIndex != 0 && world != null && pos != null) {
					TileEntity te = world.getTileEntity(pos);
					if (te instanceof TileDrum) {
						FluidStack fluid = ((TileDrum) te).tanks.getFluid();
						return FluidColors.getColor(fluid);
					}
				}

				return -1;
			}
		}, BlockDrum.this);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator.Builder(this).addDropProperties(PROPERTY_CAPACITY).build();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return state.getValue(PROPERTY_CAPACITY).createTile();
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileDrum) {
			return ImmutableList.of(((TileDrum) tile).createDropStack());
		}
		return super.getDrops(world, pos, state, fortune);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileDrum) {
				((TileDrum) te).ticked();
			}
		}
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		Capacity capacity = state.getValue(PROPERTY_CAPACITY);

		BoxModel model = new BoxModel();
		final int n = 8;
		float t[] = new float[n];
		float x[] = new float[n];
		float z[] = new float[n];
		for (int i = 0; i < n; i++) {
			double angle1 = ((0.5 + i) / (float) (n)) * 2 * Math.PI;
			x[i] = 0.5F + capacity.getWidth() * (float) Math.cos(angle1);
			z[i] = 0.5F + capacity.getWidth() * (float) Math.sin(angle1);
			t[i] = (i / (float) (n) * 2) % 1;
		}

		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			float u1 = t[i];
			float u2 = t[j];
			if (u2 < 1e-10) u2 = 1;

			model.add(new BoxSingleQuad(
					new UV(x[i], 0, z[i], u1, 0),
					new UV(x[i], 1, z[i], u1, 1),
					new UV(x[j], 1, z[j], u2, 1),
					new UV(x[j], 0, z[j], u2, 0)
			).setTexture("drum_center_colored").setLayer(BlockRenderLayer.CUTOUT).setTint(1));
			model.add(new BoxSingleQuad(
					new UV(x[i], 0, z[i], u1, 0),
					new UV(x[i], 1, z[i], u1, 1),
					new UV(x[j], 1, z[j], u2, 1),
					new UV(x[j], 0, z[j], u2, 0)
			).setTexture(capacity.texture_side));
		}

		int[][] vs = new int[][]{
				{0, 1, 2, 3},
				{4, 5, 6, 7},
				{0, 3, 4, 7},
		};

		for (int[] v : vs) {
			UV[] vals;
			vals = new UV[4];
			for (int i = 0; i < 4; i++) {
				int j = v[i];
				vals[i] = new UV(x[j], 1 / 16F, z[j], x[j], z[j]);
			}
			model.add(new BoxSingleQuad(vals).setTexture(capacity.texture_top));

			vals = new UV[4];
			for (int i = 0; i < 4; i++) {
				int j = v[3 - i];
				vals[i] = new UV(x[j], 15 / 16F, z[j], x[j], z[j]);
			}
			model.add(new BoxSingleQuad(vals).setTexture(capacity.texture_top));
		}

		model.overrideBounds = new Box(0, 0, 0, 1, 1, 1);

		return model;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		FluidStack fluidStack = getFluidFromItemStack(stack);
		if (fluidStack != null) {
			Fluid fluid = fluidStack.getFluid();
			String unlocalizedName;
			if (fluid == FluidRegistry.WATER) {
				unlocalizedName = "tile.water.name";
			} else if (fluid == FluidRegistry.LAVA) {
				unlocalizedName = "tile.lava.name";
			} else
				unlocalizedName = fluid.getUnlocalizedName(fluidStack);
			int capacity = xuBlockState.getStateFromDropMeta(stack.getMetadata()).getValue(PROPERTY_CAPACITY).capacity * 1000;

			tooltip.add(Lang.translateArgs("Drum: %s (%s / %s)", I18n.translateToLocal(unlocalizedName), StringHelper.format(fluidStack.amount), StringHelper.format(capacity)));
		}
	}

	public enum Capacity {
		DRUM_16("stone", 16) {
			@Override
			public TileDrum createTile() {
				return new TileDrum.Tank16();
			}
		},
		DRUM_256("iron", 256) {
			@Override
			public TileDrum createTile() {
				return new TileDrum.Tank256();
			}
		},
		DRUM_4096("highcapacity", 4096) {
			@Override
			public float getWidth() {
				return 0.4F;
//				return 0.484375f;
			}

			@Override
			public TileDrum createTile() {
				return new TileDrum.Tank4096();
			}
		},
		DRUM_65536("insane", 65536) {
			@Override
			public TileDrum createTile() {
				return new TileDrum.Tank65536();
			}

			@Override
			public float getWidth() {
//				return 0.4F;
				return 0.484375f;
			}
		},
		DRUM_CREATIVE("creative", 10000) {
			@Override
			public TileDrum createTile() {
				return new TileDrum.TankInf();
			}
		};

		public final String texture;
		public final String texture_side;
		public final String texture_top;
		public final int capacity;

		Capacity(String texture, int capacity) {
			this.texture = texture;
			this.capacity = capacity;
			texture_side = "drum_center_stripe_" + texture;
			texture_top = "drum_top_" + texture;
		}

		public abstract TileDrum createTile();

		public float getWidth() {
			return 0.4F;
		}
	}
}
