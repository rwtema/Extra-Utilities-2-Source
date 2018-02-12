package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockConnectedTextureBase;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.textures.ConnectedTexture;
import com.rwtema.extrautils2.textures.ISolidWorldTexture;
import com.rwtema.extrautils2.tile.TileResonator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockDecorativeGlass extends XUBlockConnectedTextureBase {
	public static final PropertyEnumSimple<DecorStates> decor = new PropertyEnumSimple<>(DecorStates.class);

	public BlockDecorativeGlass() {
		super(Material.GLASS);
	}

	@Override
	public boolean isTranslucent(IBlockState state) {
		return state.getValue(decor).opacity == 0;
	}

	@Override
	public float getAmbientOcclusionLightValue(IBlockState state) {
		return isTranslucent(state) ? 1.0F : 0.2F;
	}

	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, @Nonnull IBlockState state, EntityPlayer player) {
		return true;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		for (DecorStates decorState : DecorStates.values()) {
			decorState.tex = new ConnectedTexture(decorState.toString(), xuBlockState.defaultState.withProperty(decor, decorState), BlockDecorativeGlass.this);
		}
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this, false, decor);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ISolidWorldTexture getConnectedTexture(IBlockState state, EnumFacing side) {
		return state.getValue(decor).tex;
	}


	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getBlock() != this)
			return 0;

		return state.getValue(decor).opacity;
	}

	@Override
	public int getLightValue(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos) {
		if (state.getBlock() != this)
			return 0;
		return state.getValue(decor).light_level;
	}

	@Override
	public BlockRenderLayer renderLayer(IBlockState state) {
		return state.getValue(decor).layer;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return state.getBlock() == this && state.getValue(decor).redstone_level > 0;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		if (state == null) {
			if (worldIn instanceof World) {
				if (!((World) worldIn).isBlockLoaded(pos)) {
					return 0;
				}
			}

			state = worldIn.getBlockState(pos);
		}

		if (state.getBlock() != this)
			return 0;
		return state.getValue(decor).redstone_level;
	}

//	@Override
//	public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
//		if(state == null) {
//			if (worldIn instanceof World) {
//				if (!((World) worldIn).isBlockLoaded(pos)) {
//					return 0;
//				}
//			}
//
//			state = worldIn.getBlockState(pos);
//			if (state.getBlock() != this)
//				return 0;
//		}
//		return state.getValue(decor).redstone_level;
//	}

	public enum DecorStates implements IItemStackMaker {
		glass {
			@Override
			public void addRecipes() {
				FurnaceRecipes.instance().addSmeltingRecipe(BlockDecorativeSolid.DecorStates.sandy_glass.newStack(1), newStack(1), 0);
			}
		},
		glass_border {
			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("glass_border", newStack(4), "SS", "SS", 'S', glass.newStack(1));
			}
		},
		glass_diamonds {
			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("glass_diamonds", newStack(4), "SS", "SS", 'S', glass_border.newStack(1));
			}
		},
		darkglass {
			{
				layer = BlockRenderLayer.TRANSLUCENT;
				opacity = 255;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("glass_dark", newStack(2), glass.newStack(1), "dyeBlack", glass.newStack(1));
			}
		},
		glass_glowstone {
			{
				layer = BlockRenderLayer.TRANSLUCENT;
				light_level = 15;
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("glass_glowstone", newStack(2), glass.newStack(1), "dustGlowstone", glass.newStack(1));
			}
		},
		glass_redstone {
			{
				redstone_level = 15;
			}

			@Override
			public void addRecipes() {
				if (XU2Entries.resonator.isActive()) {
					TileResonator.register(glass.newStack(1), newStack(1), 100);
				} else
					CraftingHelper.addShapeless("glass_redstone", newStack(2), glass.newStack(1), "dustRedstone", glass.newStack(1));
			}
		};

		@SideOnly(Side.CLIENT)
		public ISolidWorldTexture tex;
		int opacity = 0;
		int light_level = 0;
		int redstone_level = 0;
		BlockRenderLayer layer = BlockRenderLayer.CUTOUT;

		public abstract void addRecipes();

		public ItemStack newStack(int amount) {
			return XU2Entries.decorativeGlass.newStack(amount, decor, this);
		}

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}
	}

}
