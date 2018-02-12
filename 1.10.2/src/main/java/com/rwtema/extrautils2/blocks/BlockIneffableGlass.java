package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockConnectedTextureBase;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.backend.model.BoxModel;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.textures.ConnectedTexture;
import com.rwtema.extrautils2.textures.ISolidWorldTexture;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class BlockIneffableGlass extends XUBlockConnectedTextureBase {
	public static final PropertyEnumSimple<DecorStates> decor = new PropertyEnumSimple<>(DecorStates.class);

	public BlockIneffableGlass() {
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

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, @Nonnull IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		for (DecorStates decorState : DecorStates.values()) {
			decorState.tex = new ConnectedTexture("strange_glass_" + decorState.toString(), xuBlockState.defaultState.withProperty(decor, decorState), BlockIneffableGlass.this);
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

//	@Override
//	public boolean isVisuallyOpaque() {
//		return false;
//	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return super.isBlockNormalCube(state);
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return super.isNormalCube(state);
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return super.isNormalCube(state, world, pos);
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
	public void addCollisionBoxToListBase(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		if (!state.getValue(decor).shouldAllowPassage(entityIn))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, Block.FULL_BLOCK_AABB);
	}

	@Override
	public boolean isDoubleSided(IBlockState state) {
		return true;
	}

	@Override
	protected BoxModel createBaseModel() {
		return new BoxModel(0, 0, 0, 1, 1, 1);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	public enum DecorStates implements IItemStackMaker {
		normal {
			@Override
			public boolean shouldAllowPassage(Entity entityIn) {
				return isNotSneakingPlayer(entityIn);
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("glass_ineffable_normal", newStack(8), "ggg", "gmg", "ggg", 'g', "blockGlass", 'm', ItemIngredients.Type.MOON_STONE);
			}
		},
		reverse {
			@Override
			public boolean shouldAllowPassage(Entity entityIn) {
				return !isNotSneakingPlayer(entityIn);
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("glass_ineffable_reverse", newStack(1), normal.newStack(), Blocks.REDSTONE_TORCH);
			}
		},
		clear {
			@Override
			public boolean shouldAllowPassage(Entity entityIn) {
				return isNotSneakingPlayer(entityIn);
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("glass_ineffable_clear", newStack(8), "ggg", "gmg", "ggg", 'g', BlockDecorativeGlass.DecorStates.glass, 'm', ItemIngredients.Type.MOON_STONE);
			}
		},
		dark {
			{
				layer = BlockRenderLayer.TRANSLUCENT;
				opacity = 255;
			}

			@Override
			public boolean shouldAllowPassage(Entity entityIn) {
				return isNotSneakingPlayer(entityIn);
			}

			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("glass_ineffable_dark", newStack(8), "ggg", "gmg", "ggg", 'g', BlockDecorativeGlass.DecorStates.darkglass, 'm', ItemIngredients.Type.MOON_STONE);
			}
		};

		public int opacity;
		@SideOnly(Side.CLIENT)
		public ISolidWorldTexture tex;
		public int light_level = 0;
		BlockRenderLayer layer = BlockRenderLayer.CUTOUT;

		private static boolean isNotSneakingPlayer(Entity entityIn) {
			return entityIn != null && !entityIn.isSneaking() && entityIn instanceof EntityPlayer;
		}

		public ItemStack newStack(int amount) {
			return XU2Entries.strangeGlass.newStack(amount, decor, this);
		}

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}

		public abstract boolean shouldAllowPassage(Entity entityIn);

		public abstract void addRecipes();
	}
}
