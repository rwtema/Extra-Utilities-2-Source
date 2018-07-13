package com.rwtema.extrautils2.blocks;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.XUBlockStatic;
import com.rwtema.extrautils2.backend.model.*;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.ItemStackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class BlockRedOrchid extends XUBlockStatic implements IPlantable, IGrowable {
	public static final PropertyInteger GROWTH_STATE = PropertyInteger.create("growth", 0, 6);
	static float[] size_w = {1 / 16F, 1 / 16F, 2 / 16F, 3 / 16F, 3 / 16F, 4 / 16F, 5 / 16F};
	static float[] size_h = {5 / 16F, 8 / 16F, 9 / 16F, 11 / 16F, 12 / 16F, 13 / 16F, 16 / 16F};
	public final IBlockState FULLY_GROWN_STATE;
	EnumPlantType redstone = EnumPlantType.getPlantType("Redstone");
	HashSet<IBlockState> validStates = null;

	public BlockRedOrchid() {
		super(Material.PLANTS);
		setTickRandomly(true);
		setHardness(0);
		FULLY_GROWN_STATE = getDefaultState().withProperty(GROWTH_STATE, 6);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		int i = state.getValue(GROWTH_STATE);
		if (i >= 6 || (rand.nextInt(3) != 0)) return;
		worldIn.setBlockState(pos, state.withProperty(GROWTH_STATE, i + 1), 2);
		if ((i + 1) == 6) {
			if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.REDSTONE_ORE) {
				worldIn.setBlockState(pos.down(), Blocks.LIT_REDSTONE_ORE.getDefaultState());
			}
		}
	}

	@Override
	public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		int i = state.getValue(GROWTH_STATE);
		i += MathHelper.getInt(worldIn.rand, 2, 5);
		if (i >= 6) {
			i = 6;
			if (worldIn.getBlockState(pos.down()).getBlock() == Blocks.REDSTONE_ORE) {
				worldIn.setBlockState(pos.down(), Blocks.LIT_REDSTONE_ORE.getDefaultState());
			}
		}

		worldIn.setBlockState(pos, state.withProperty(GROWTH_STATE, i), 2);
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this, GROWTH_STATE);
	}

	@Override
	public BoxModel getModel(IBlockState state) {
		int value = state.getValue(GROWTH_STATE);
		BoxModel boxes = BoxModel.crossBoxModel().setTexture("plants/redorchid_" + value);
		boxes.addBoxI(0, 0, 0, 16, 0, 16, "plants/redorchid_base_" + value).setInvisible(1 | 4 | 8 | 16 | 32);
		for (Box box : boxes) {
			box.noCollide = true;
			box.setLayer(BlockRenderLayer.CUTOUT);
		}

		boxes.overrideBounds = new Box(0.5F - size_w[value], 0, .5F - size_w[value], 0.5F + size_w[value], size_h[value], .5F + size_w[value]);

		return boxes;
	}

	@Override
	public boolean canGrow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient) {
		return state.getValue(GROWTH_STATE) < 6;
	}

	@Override
	public boolean canUseBonemeal(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		return true;
	}

	@Override
	public void neighborChangedBase(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock) {
		if (!validLocation(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return;
		}

		super.neighborChangedBase(state, worldIn, pos, neighborBlock);

		if (state == FULLY_GROWN_STATE && worldIn.getBlockState(pos.down()).getBlock() == Blocks.REDSTONE_ORE) {
			worldIn.setBlockState(pos.down(), Blocks.LIT_REDSTONE_ORE.getDefaultState());
		}
	}

	@Override
	public boolean canReplaceBase(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side, ItemStack stack) {
		return validLocation(worldIn, pos) && super.canReplaceBase(worldIn, pos, side, stack);
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
		if (state == FULLY_GROWN_STATE)
			addAdditionalDrops(fortune, drops, world instanceof World ? ((World) world).rand : RANDOM);
		return drops;
	}

	protected List<ItemStack> addAdditionalDrops(int fortune, List<ItemStack> drops, Random random) {
		int n = 1 + random.nextInt(1 + fortune);
		for (int i = 0; i < n; i++) {
			drops.add(new ItemStack(Items.REDSTONE, 1));
		}

		int k = random.nextInt(3 + fortune) - random.nextInt(10);
		if (k > 0) {
			for (int i = 0; i < k; i++) {
				drops.add(new ItemStack(this, 1));
			}
		}
		return drops;
	}

	public boolean validLocation(World world, BlockPos pos) {
		if (validStates == null) {
			validStates = new HashSet<>();
			validStates.add(Blocks.REDSTONE_ORE.getDefaultState());
			validStates.add(Blocks.LIT_REDSTONE_ORE.getDefaultState());
			ItemStackHelper.addBlockStates("oreRedstone", validStates);
		}

		return validStates.contains(world.getBlockState(pos.down()));
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return redstone;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		if (world == null || pos == null) return getDefaultState();
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) return getDefaultState();
		return state;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public MutableModel createInventoryMutableModel() {
		return new PassthruModelItem.ModelLayer(Transforms.itemTransforms);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		super.registerTextures();
		Textures.register("plants/redorchid_seeds");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInventoryQuads(MutableModel result, ItemStack stack) {
		PassthruModelItem.ModelLayer layer = (PassthruModelItem.ModelLayer) result;
		layer.clear();
		layer.addSprite(Textures.sprites.get("plants/redorchid_seeds"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
		if (state.getValue(GROWTH_STATE) != 6) return;

		AxisAlignedBB bb = getSelectedBoundingBox(state, worldIn, pos);
		for (int i = 0; i < 2; ++i) {
			double x = MathHelper.nextDouble(rand, bb.minX, bb.maxX);
			double y = MathHelper.nextDouble(rand, bb.minY, bb.maxY);
			double z = MathHelper.nextDouble(rand, bb.minZ, bb.maxZ);

			worldIn.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return blockState == FULLY_GROWN_STATE ? 15 : 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return state == FULLY_GROWN_STATE;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return state == FULLY_GROWN_STATE;
	}

	@Override
	public boolean onBlockActivatedBase(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state != FULLY_GROWN_STATE) {
			return false;
		}

		worldIn.setBlockState(pos, getDefaultState());

		if (worldIn.isRemote)
			return true;

		List<ItemStack> drops = addAdditionalDrops(EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FORTUNE, playerIn), Lists.newArrayList(), worldIn.rand);

		for (ItemStack drop : drops) {
			spawnAsEntity(worldIn, pos, drop);
		}
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(Lang.translatePrefix("Plant on redstone ore."));
	}
}
